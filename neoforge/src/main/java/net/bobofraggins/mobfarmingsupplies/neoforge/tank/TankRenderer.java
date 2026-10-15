package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import net.bobofraggins.mobfarmingsupplies.neoforge.client.FluidSprites;
import net.bobofraggins.mobfarmingsupplies.shared.render.BlockEntityRenderState;
import net.bobofraggins.mobfarmingsupplies.shared.render.Geometry;
import net.bobofraggins.mobfarmingsupplies.shared.render.StateBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.TankFluidGeometry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/**
 * Block-entity special renderer for the Tank.
 *
 * <p>Renders the stored fluid as a translucent coloured cube that grows with the fill level.
 * The block model provides the glass frame; this renderer draws only the fluid fill.
 */
public class TankRenderer
        extends StateBlockEntityRenderer<TankBlockEntity, TankRenderer.TankState> {

    /** Floor and ceiling of the renderable interior, in block units. */
    static final float FLOOR = TankFluidGeometry.FLOOR;
    static final float H     = TankFluidGeometry.H;

    public TankRenderer(BlockEntityRendererProvider.Context ctx) {}

    // ── Render state ───────────────────────────────────────────────────────────

    public static class TankState extends BlockEntityRenderState {
        boolean hasFill;
        int fr, fg, fb, fa;
        float fillTop;
        float uL, uR, vT, vB, vMax;
        int fluidLight;
    }

    @Override
    public TankState createRenderState() {
        return new TankState();
    }

    @Override
    public void extractRenderState(TankBlockEntity be, TankState state, float partialTick) {
        state.hasFill = false;
        if (!be.isLocked()) return;

        FluidStack fluid = be.getStoredFluid();
        if (fluid.isEmpty()) return;

        float fillFrac = Math.min(1f, Math.max(0.01f, (float) be.getAmount() / (float) be.getCapacity()));
        state.fillTop = FLOOR + fillFrac * H;

        net.neoforged.neoforge.fluids.FluidStack neoFluid = FluidStackHooksForge.toForge(fluid.copyWithAmount(1));
        var sprite = FluidSprites.still(neoFluid);
        int tint = FluidSprites.tint(neoFluid);
        state.fr = (tint >> 16) & 0xFF;
        state.fg = (tint >>  8) & 0xFF;
        state.fb = tint & 0xFF;
        state.fa = (tint >> 24) & 0xFF;
        if (state.fa == 0) state.fa = 77; // default semi-transparency for fluids without alpha

        // getFluidType() is NeoForge-specific; convert via FluidStackHooksForge
        state.fluidLight = FluidStackHooksForge.toForge(fluid.copyWithAmount(1))
                .getFluidType().getLightLevel() > 0 ? 0xF000F0 : state.lightCoords;
        state.uL = sprite.getU0();
        state.uR = sprite.getU1();
        state.vT = sprite.getV0();
        state.vB = Mth.lerp(fillFrac, sprite.getV0(), sprite.getV1());
        state.vMax = sprite.getV1();
        state.hasFill = true;
    }

    @Override
    public void submit(TankState state, PoseStack poseStack, MultiBufferSource buffers) {
        if (!state.hasFill) return;

        int overlay = OverlayTexture.NO_OVERLAY;
        float fillTop = state.fillTop;
        int r = state.fr, g = state.fg, b = state.fb, a = state.fa;
        int light = state.fluidLight;
        float uL = state.uL, uR = state.uR, vT = state.vT, vB = state.vB, vMax = state.vMax;

        poseStack.pushPose();
        Geometry.custom(poseStack, buffers,
                Sheets.translucentCullBlockSheet(),
                (pose, vc) -> renderCubeFill(
                        vc, pose, r, g, b, a, light, overlay, uL, vT, uR, vB, vMax, fillTop));
        poseStack.popPose();
    }

    // ── Geometry ───────────────────────────────────────────────────────────────

    /**
     * Emits a five-faced translucent cube (no bottom) representing the fluid fill level.
     * The cube spans from {@link #FLOOR} on all sides to {@code fillTop} on the Y axis.
     */
    public static void renderCubeFill(
            VertexConsumer vc,
            PoseStack.Pose pose,
            int r, int g, int b, int a,
            int light, int overlay,
            float uL, float vT, float uR, float vB, float vMax,
            float fillTop) {
        TankFluidGeometry.renderCubeFill(vc, pose, r, g, b, a, light, overlay, uL, vT, uR, vB, vMax, fillTop);
    }
}
