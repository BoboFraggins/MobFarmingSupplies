package net.bobofraggins.mobfarmingsupplies.colorworkstation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankFluidGeometry;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the Color Workstation's Essence of Color as a tank draws its fluid: a cube filling with
 * the buffer, in the model's {@code liquid} space (14 pixels up, on top of the bench). Essences
 * are untinted, so the fluid's own still texture is all it needs and this works on both loaders.
 */
public class ColorWorkstationRenderer
        implements BlockEntityRenderer<ColorWorkstationBlockEntity, ColorWorkstationRenderer.State> {

    /** How far up the tank sits: the model's liquid cube starts at y 15, a tank's at y 1. */
    private static final float RAISE = 14f / 16f;

    public ColorWorkstationRenderer(BlockEntityRendererProvider.Context ctx) {}

    public static class State extends BlockEntityRenderState {
        boolean hasFill;
        float fillTop;
        float uL, uR, vT, vB, vMax;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ColorWorkstationBlockEntity be, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.hasFill = be.getAmount() > 0;
        if (!state.hasFill) return;
        float fillFrac = Math.min(1f, Math.max(0.01f, (float) be.getAmount() / be.getCapacity()));
        state.fillTop = TankFluidGeometry.FLOOR + fillFrac * TankFluidGeometry.H;
        var sprite = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(Registration.ESSENCE_SOURCES.get(Essence.COLOR).get().defaultFluidState())
                .stillMaterial().sprite();
        state.uL = sprite.getU0();
        state.uR = sprite.getU1();
        state.vT = sprite.getV0();
        state.vB = Mth.lerp(fillFrac, sprite.getV0(), sprite.getV1());
        state.vMax = sprite.getV1();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (!state.hasFill) return;
        float fillTop = state.fillTop;
        float uL = state.uL, uR = state.uR, vT = state.vT, vB = state.vB, vMax = state.vMax;
        int light = state.lightCoords;
        poseStack.pushPose();
        poseStack.translate(0f, RAISE, 0f);
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(),
                (pose, vc) -> TankFluidGeometry.renderCubeFill(vc, pose, 255, 255, 255, 255, light,
                        OverlayTexture.NO_OVERLAY, uL, vT, uR, vB, vMax, fillTop));
        poseStack.popPose();
    }

    /**
     * Both blocks: the fluid is in the upper one. NeoForge culls a block entity's renderer by this
     * box (its loader renderer overrides {@code getRenderBoundingBox} with it).
     */
    public static AABB renderBounds(ColorWorkstationBlockEntity be) {
        return new AABB(be.getBlockPos()).expandTowards(0, 1, 0);
    }
}
