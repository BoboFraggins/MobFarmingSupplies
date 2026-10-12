package net.bobofraggins.mobfarmingsupplies.gravitationalanomaly;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Draws translucent layers in the translucent particle pass, which comes after water, stained glass
 * and clouds. Drawn with the rest of a block entity, they'd come before those and be painted over
 * by them even where they're behind. Each layer has its own pipeline and texture; the vertices are
 * in the {@link DefaultVertexFormat#ENTITY} format, relative to the camera, like particles.
 */
public final class GlowPass implements SubmitNodeCollector.ParticleGroupRenderer {

    /**
     * Writes depth, and nothing else, where a glow is at least 20% opaque. Drawn under the glows,
     * it keeps clouds and weather, which come after them, from being drawn over their brighter
     * parts when they're behind; their faint edges are left out, so they don't cut holes in them.
     */
    public static final RenderPipeline DEPTH_ONLY = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "pipeline/black_hole_depth"))
            .withVertexShader("core/entity")
            .withFragmentShader(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "core/black_hole_depth"))
            .withShaderDefine("EMISSIVE")
            .withShaderDefine("NO_OVERLAY")
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withShaderDefine("DEPTH_CUTOUT", 0.2f)
            .withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
            .withColorTargetState(new ColorTargetState(Optional.empty(), ColorTargetState.WRITE_NONE))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
            .build();

    /** One layer: how it's drawn, and what draws its quads. */
    public record Layer(RenderPipeline pipeline, Identifier texture, SubmitNodeCollector.CustomGeometryRenderer quads) {}

    private final PoseStack.Pose pose;
    private final List<Layer> layers;
    /** Each layer's first vertex, vertex count and texture, filled in by {@link #prepare}. */
    private final int[] first, count;
    private final AbstractTexture[] textures;

    /** {@code pose} places the layers relative to the camera; it's copied. */
    public GlowPass(PoseStack.Pose pose, List<Layer> layers) {
        this.pose = pose.copy();
        this.layers = List.copyOf(layers);
        this.first = new int[layers.size()];
        this.count = new int[layers.size()];
        this.textures = new AbstractTexture[layers.size()];
    }

    @Override
    public boolean isEmpty() {
        return layers.isEmpty();
    }

    @Override
    public QuadParticleRenderState.PreparedBuffers prepare(ParticleFeatureRenderer.ParticleBufferCache cache,
                                                           boolean translucent) {
        if (!translucent || layers.isEmpty()) return null;
        // Looked up now: the first lookup uploads the texture, which can't happen during the pass.
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        for (int i = 0; i < layers.size(); i++) textures[i] = manager.getTexture(layers.get(i).texture());
        try (ByteBufferBuilder bytes = new ByteBufferBuilder(DefaultVertexFormat.ENTITY.getVertexSize() * 8192)) {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.ENTITY);
            CountingConsumer counter = new CountingConsumer(builder);
            for (int i = 0; i < layers.size(); i++) {
                first[i] = counter.vertices;
                layers.get(i).quads().render(pose, counter);
                count[i] = counter.vertices - first[i];
            }
            try (MeshData mesh = builder.build()) {
                if (mesh == null) return null;
                cache.write(mesh.vertexBuffer());
                int indices = mesh.drawState().indexCount();
                RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).getBuffer(indices);
                GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                        RenderSystem.getModelViewMatrix(), new Vector4f(1, 1, 1, 1), new Vector3f(), new Matrix4f());
                return new QuadParticleRenderState.PreparedBuffers(indices, transforms, Map.of());
            }
        }
    }

    @Override
    public void render(QuadParticleRenderState.PreparedBuffers prepared, ParticleFeatureRenderer.ParticleBufferCache cache,
                       RenderPass pass, TextureManager manager) {
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        pass.setVertexBuffer(0, cache.get());
        pass.setIndexBuffer(indices.getBuffer(prepared.indexCount()), indices.type());
        pass.setUniform("DynamicTransforms", prepared.dynamicTransforms());
        for (int i = 0; i < layers.size(); i++) {
            if (count[i] == 0) continue;
            Layer layer = layers.get(i);
            pass.setPipeline(layer.pipeline());
            pass.bindTexture("Sampler0", textures[i].getTextureView(), textures[i].getSampler());
            pass.drawIndexed(first[i], 0, count[i] / 4 * 6, 1);
        }
    }

    /** Passes vertices on, counting them. */
    private static final class CountingConsumer implements com.mojang.blaze3d.vertex.VertexConsumer {
        private final com.mojang.blaze3d.vertex.VertexConsumer target;
        int vertices;

        CountingConsumer(com.mojang.blaze3d.vertex.VertexConsumer target) {
            this.target = target;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer addVertex(float x, float y, float z) {
            vertices++;
            target.addVertex(x, y, z);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setColor(int r, int g, int b, int a) {
            target.setColor(r, g, b, a);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setColor(int argb) {
            target.setColor(argb);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setUv(float u, float v) {
            target.setUv(u, v);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setUv1(int u, int v) {
            target.setUv1(u, v);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setUv2(int u, int v) {
            target.setUv2(u, v);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setNormal(float x, float y, float z) {
            target.setNormal(x, y, z);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setLineWidth(float width) {
            target.setLineWidth(width);
            return this;
        }
    }
}
