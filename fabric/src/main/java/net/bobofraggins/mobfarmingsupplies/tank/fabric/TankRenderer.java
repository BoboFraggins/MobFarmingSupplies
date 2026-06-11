package net.bobofraggins.mobfarmingsupplies.tank.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.TankFluidGeometry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Fabric port of the NeoForge {@code TankRenderer}.
 *
 * <p>Renders the stored fluid as a translucent coloured cube that grows with the fill level,
 * using the shared {@link TankFluidGeometry} helper. Mirrors
 * {@code net.bobofraggins.mobfarmingsupplies.tank.TankRenderer} (NeoForge), but obtains the
 * fluid tint and light level via Fabric-safe APIs (no {@code FluidStackHooksForge}).
 */
@SuppressWarnings("UnstableApiUsage")
public class TankRenderer implements BlockEntityRenderer<TankBlockEntity, TankRenderer.TankState> {

    private static final float FLOOR = TankFluidGeometry.FLOOR;
    private static final float H = TankFluidGeometry.H;

    public TankRenderer(BlockEntityRendererProvider.Context ctx) {}

    // ── Render state ───────────────────────────────────────────────────────────

    public static class TankState extends BlockEntityRenderState {
        boolean hasFill;
        int fr, fg, fb, fa;
        float fillTop;
        float uL, uR, vT, vB;
        int fluidLight;
    }

    @Override
    public TankState createRenderState() {
        return new TankState();
    }

    @Override
    public void extractRenderState(
            TankBlockEntity be,
            TankState state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.hasFill = false;
        if (!be.isLocked()) return;

        FluidStack fluid = be.getStoredFluid();
        if (fluid.isEmpty()) return;

        float fillFrac = Math.max(0.01f, (float) be.getAmount() / (float) be.getCapacity());
        state.fillTop = FLOOR + fillFrac * H;

        var fluidModel = Minecraft.getInstance()
                .getModelManager()
                .getFluidStateModelSet()
                .get(fluid.getFluid().defaultFluidState());
        var sprite = fluidModel.stillMaterial().sprite();

        int tint = fluidModel.tintSource().color(fluid.getFluid().defaultFluidState().createLegacyBlock());
        state.fr = (tint >> 16) & 0xFF;
        state.fg = (tint >>  8) & 0xFF;
        state.fb = tint & 0xFF;
        state.fa = (tint >> 24) & 0xFF;
        if (state.fa == 0) state.fa = 77; // default semi-transparency for fluids without alpha

        int luminance = FluidVariantAttributes.getLuminance(FluidStackHooksFabric.toFabric(fluid.copyWithAmount(1)));
        state.fluidLight = luminance > 0 ? 0xF000F0 : state.lightCoords;

        state.uL = sprite.getU0();
        state.uR = sprite.getU1();
        state.vT = sprite.getV0();
        state.vB = Mth.lerp(fillFrac, sprite.getV0(), sprite.getV1());
        state.hasFill = true;
    }

    @Override
    public void submit(
            TankState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasFill) return;

        int overlay = OverlayTexture.NO_OVERLAY;
        float fillTop = state.fillTop;
        int r = state.fr, g = state.fg, b = state.fb, a = state.fa;
        int light = state.fluidLight;
        float uL = state.uL, uR = state.uR, vT = state.vT, vB = state.vB;

        poseStack.pushPose();
        collector.submitCustomGeometry(
                poseStack,
                Sheets.translucentBlockSheet(),
                (pose, vc) -> TankFluidGeometry.renderCubeFill(
                        vc, pose.pose(), r, g, b, a, light, overlay, uL, vT, uR, vB, fillTop));
        poseStack.popPose();
    }
}
