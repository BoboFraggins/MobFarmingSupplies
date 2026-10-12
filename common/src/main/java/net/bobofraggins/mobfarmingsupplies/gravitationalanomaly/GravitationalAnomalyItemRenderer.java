package net.bobofraggins.mobfarmingsupplies.gravitationalanomaly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * The Gravitational Anomaly's item: the same black hole as the placed block, without jets. The
 * disk is bent for the direction it's seen from, which the item model says how to find: in
 * inventories ({@code "view": "screen"}) the viewer is straight out of the screen; everywhere else
 * ({@code "camera"}) it's the camera, at the origin of the pose.
 */
public class GravitationalAnomalyItemRenderer implements SpecialModelRenderer<Void> {

    /** The disk's lensed images reach about 20 pixels from the centre. */
    private static final float REACH = 20f / 16f;

    private final boolean screen;

    public GravitationalAnomalyItemRenderer(boolean screen) {
        this.screen = screen;
        AccretionDisk.warmUp();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (int corner = 0; corner < 8; corner++) {
            output.accept(new Vector3f(0.5f + ((corner & 1) == 0 ? -REACH : REACH),
                    0.5f + ((corner & 2) == 0 ? -REACH : REACH), 0.5f + ((corner & 4) == 0 ? -REACH : REACH)));
        }
    }

    @Override
    @Nullable
    public Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void submit(@Nullable Void argument, PoseStack poseStack, SubmitNodeCollector collector, int light,
                       int overlay, boolean hasFoil, int tint) {
        Matrix4f toModel = new Matrix4f(poseStack.last().pose()).invert();
        Vector3f toViewer = screen
                ? toModel.transformDirection(new Vector3f(0, 0, 1))
                : toModel.transformPosition(new Vector3f()).sub(0.5f, 0.5f, 0.5f);
        float seconds = (Util.getMillis() % 3_600_000L) / 1000f;
        GravitationalAnomalyRenderer.submitBlackHole(toViewer, seconds, 0f, false, poseStack, collector);
    }

    public record Unbaked(String view) implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<Unbaked> MAP_CODEC =
                Codec.STRING.optionalFieldOf("view", "camera").xmap(Unbaked::new, Unbaked::view);

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new GravitationalAnomalyItemRenderer("screen".equals(view));
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Void>> type() {
            return MAP_CODEC;
        }
    }
}
