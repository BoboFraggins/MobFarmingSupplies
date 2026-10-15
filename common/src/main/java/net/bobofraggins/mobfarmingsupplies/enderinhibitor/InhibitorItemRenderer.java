package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.function.Supplier;
import net.bobofraggins.mobfarmingsupplies.shared.render.Geometry;
import net.bobofraggins.mobfarmingsupplies.shared.render.ItemModelResolver;
import net.bobofraggins.mobfarmingsupplies.shared.render.ItemStackRenderState;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialModelRenderer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;

/**
 * Item renderer for the Ender and Wither Inhibitors: the item that floats above the prongs (an
 * Ender Pearl or a Wither Skeleton Skull), turning as it does on the placed block, and the block's
 * particles. Particles only exist in the world, so these are drawn here as particle sprites that
 * move as the real ones do, scaled down to stay around the item. Draws the block model first.
 */
public class InhibitorItemRenderer implements SpecialModelRenderer<Void> {

    /** Where the floating item sits above the block centre, as on the placed block. */
    private static final float OFFSET = 0.225f;
    /** Where the block's particles start, above its centre, and how far either side. */
    private static final float EMIT_OFFSET = 0.3f, EMIT_SPREAD = 0.625f;
    private static final RenderType PARTICLE_TYPE = RenderType.entityCutout(TextureAtlas.LOCATION_PARTICLES);
    private static final int FULL_BRIGHT = 0xF000F0;

    /** Which of the blocks' particles to draw. */
    public enum Particles { NONE, PORTAL, SMOKE }

    private final Supplier<? extends Block> block;
    private final Item floating;
    private final Particles particles;
    /** The floating item's model, resolved on first use; its look never changes. */
    @Nullable private ItemStackRenderState floatingState;
    @Nullable private TextureAtlasSprite[] sprites;

    public InhibitorItemRenderer(Supplier<? extends Block> block, Item floating, Particles particles) {
        this.block = block;
        this.floating = floating;
        this.particles = particles;
    }

    @Nullable
    @Override
    public Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void submit(@Nullable Void argument, ItemDisplayContext context, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay, boolean hasFoil) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                block.get().defaultBlockState(), poseStack, buffers, packedLight, packedOverlay);
        if (floatingState == null) {
            floatingState = new ItemStackRenderState();
            ItemModelResolver.INSTANCE.updateForTopItem(
                    floatingState, new ItemStack(floating), ItemDisplayContext.FIXED, null, null, 0);
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f + OFFSET, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(EnderInhibitorBlockEntityRenderer.spinDegrees()));
        float scale = EnderInhibitorBlockEntityRenderer.ITEM_SCALE;
        poseStack.scale(scale, scale, scale);
        floatingState.submit(poseStack, buffers, packedLight, packedOverlay, 0);
        poseStack.popPose();

        if (particles != Particles.NONE) submitParticles(poseStack, buffers, packedLight, Util.getMillis() / 50f);
    }

    // ── Particles ─────────────────────────────────────────────────────────────────
    // Each particle slot replays one particle after another forever; everything about a particle
    // comes from a hash of its slot and its turn, so the picture is the same however often it is
    // drawn.

    private static final int PORTAL_SLOTS = 8, SMOKE_SLOTS = 3;
    /** Portal particles fly in from up to a block away; this keeps them around the item. */
    private static final float PORTAL_REACH = 0.3f;

    private void submitParticles(PoseStack poseStack, MultiBufferSource buffers, int light, float ticks) {
        if (sprites == null) {
            TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES);
            sprites = new TextureAtlasSprite[8];
            for (int i = 0; i < 8; i++) sprites[i] = atlas.getSprite(ResourceLocation.withDefaultNamespace("generic_" + i));
        }
        TextureAtlasSprite[] generic = sprites;
        Geometry.custom(poseStack, buffers, PARTICLE_TYPE, (pose, vc) -> {
            // Face the viewer: the screen's right and up, in the item's own space.
            Matrix3f toLocal = new Matrix3f(pose.pose()).invert();
            Vector3f right = toLocal.transform(new Vector3f(1, 0, 0)).normalize();
            Vector3f up = toLocal.transform(new Vector3f(0, 1, 0)).normalize();
            if (particles == Particles.PORTAL) {
                for (int slot = 0; slot < PORTAL_SLOTS; slot++) portal(vc, pose, right, up, generic, slot, ticks);
            } else {
                for (int slot = 0; slot < SMOKE_SLOTS; slot++) smoke(vc, pose, right, up, generic, slot, ticks, light);
            }
        });
    }

    /** As vanilla's PortalParticle: purple, flying in to where it was spawned while it grows. */
    private static void portal(VertexConsumer vc, PoseStack.Pose pose, Vector3f right, Vector3f up,
                               TextureAtlasSprite[] generic, int slot, float ticks) {
        int lifetime = 40 + (int) (rand(slot, -1, 0) * 10f);
        float t = ticks + rand(slot, -1, 1) * lifetime;
        int turn = (int) Math.floor(t / lifetime);
        float p = (t - turn * (float) lifetime) / lifetime;
        float pull = 1f - (-p + p * p * 2f);
        float x = 0.5f + (rand(slot, turn, 0) - 0.5f) * EMIT_SPREAD + (rand(slot, turn, 3) - 0.5f) * 2f * pull * PORTAL_REACH;
        float y = 0.5f + EMIT_OFFSET + (rand(slot, turn, 1) - 0.5f) * EMIT_SPREAD
                + (-rand(slot, turn, 4) * pull + (1f - p)) * PORTAL_REACH;
        float z = 0.5f + (rand(slot, turn, 2) - 0.5f) * EMIT_SPREAD + (rand(slot, turn, 5) - 0.5f) * 2f * pull * PORTAL_REACH;
        float grow = 1f - (1f - p) * (1f - p);
        float size = 0.1f * (rand(slot, turn, 6) * 0.2f + 0.5f) * grow;
        float br = rand(slot, turn, 7) * 0.6f + 0.4f;
        quad(vc, pose, right, up, generic[(int) (rand(slot, turn, 8) * 8)], x, y, z, size,
                br * 0.9f, br * 0.3f, br, FULL_BRIGHT);
    }

    /** As vanilla's smoke: dark grey, drifting up and slowing, its puff shrinking as it ages. */
    private static void smoke(VertexConsumer vc, PoseStack.Pose pose, Vector3f right, Vector3f up,
                              TextureAtlasSprite[] generic, int slot, float ticks, int light) {
        int lifetime = 24 + (int) (rand(slot, -1, 0) * 16f);
        float t = ticks + rand(slot, -1, 1) * lifetime;
        int turn = (int) Math.floor(t / lifetime);
        float age = t - turn * (float) lifetime;
        float x = 0.5f + (rand(slot, turn, 0) - 0.5f) * EMIT_SPREAD;
        float y = 0.5f + EMIT_OFFSET + (rand(slot, turn, 1) - 0.5f) * EMIT_SPREAD;
        float z = 0.5f + (rand(slot, turn, 2) - 0.5f) * EMIT_SPREAD;
        float vx = (rand(slot, turn, 3) - 0.5f) * 0.008f, vy = 0.02f, vz = (rand(slot, turn, 4) - 0.5f) * 0.008f;
        for (int i = 0; i < (int) age; i++) {
            vy += 0.004f; // smoke's upward "gravity"
            x += vx;
            y += vy;
            z += vz;
            vx *= 0.96f;
            vy *= 0.96f;
            vz *= 0.96f;
        }
        float size = 0.075f * (rand(slot, turn, 5) * 0.5f + 0.5f) * 2f * Math.min(1f, age / lifetime * 32f);
        float col = rand(slot, turn, 6) * 0.3f;
        // The puff shrinks with age: generic_7 down to generic_0.
        int sprite = 7 - Math.min(7, (int) (age * 7f / lifetime));
        quad(vc, pose, right, up, generic[sprite], x, y, z, size, col, col, col, light);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose, Vector3f right, Vector3f up,
                             TextureAtlasSprite sprite, float x, float y, float z, float size,
                             float r, float g, float b, int light) {
        float rx = right.x * size, ry = right.y * size, rz = right.z * size;
        float ux = up.x * size, uy = up.y * size, uz = up.z * size;
        vertex(vc, pose, x - rx - ux, y - ry - uy, z - rz - uz, sprite.getU1(), sprite.getV1(), r, g, b, light);
        vertex(vc, pose, x + rx - ux, y + ry - uy, z + rz - uz, sprite.getU0(), sprite.getV1(), r, g, b, light);
        vertex(vc, pose, x + rx + ux, y + ry + uy, z + rz + uz, sprite.getU0(), sprite.getV0(), r, g, b, light);
        vertex(vc, pose, x - rx + ux, y - ry + uy, z - rz + uz, sprite.getU1(), sprite.getV0(), r, g, b, light);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, float r, float g, float b, int light) {
        vc.addVertex(pose, x, y, z).setColor(r, g, b, 1f).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0f, 1f, 0f);
    }

    /** A number in [0, 1) fixed by slot, turn and which value of the particle it is. */
    private static float rand(int slot, int turn, int value) {
        long h = slot * 0x9E3779B97F4A7C15L ^ turn * 0xC2B2AE3D27D4EB4FL ^ value * 0x165667B19E3779F9L;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 40) / (float) (1 << 24);
    }
}
