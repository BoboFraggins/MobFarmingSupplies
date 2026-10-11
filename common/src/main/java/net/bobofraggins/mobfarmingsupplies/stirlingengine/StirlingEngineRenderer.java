package net.bobofraggins.mobfarmingsupplies.stirlingengine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the Stirling Engine: its body, a flywheel turning while it's heated (tinted the tier's
 * colour), a piston driven by it, and the connecting rod between them.
 */
public class StirlingEngineRenderer
        implements BlockEntityRenderer<StirlingEngineBlockEntity, StirlingEngineRenderer.State> {

    public static final Identifier BODY_MODEL_ID = id("block/stirling_engine_body");
    public static final Identifier FLYWHEEL_MODEL_ID = id("block/stirling_engine_flywheel");
    public static final Identifier PISTON_MODEL_ID = id("block/stirling_engine_piston");
    public static final Identifier ROD_MODEL_ID = id("block/stirling_engine_rod");

    // In block units: the flywheel's axle, the piston's travel, and the rod's pivots and length.
    private static final float FLYWHEEL_X = 1.5f / 16f;
    private static final float FLYWHEEL_Y = 7.0f / 16f;
    private static final float FLYWHEEL_Z = 8.0f / 16f;
    private static final float PISTON_TRAVEL = 4.0f / 16f;
    private static final float CRANK_RADIUS = 3.0f / 16f;
    private static final float ROD_X = 4.5f / 16f;
    private static final float ROD_Y = 7.0f / 16f;
    private static final float ROD_Z = 0.5f / 16f;
    private static final float ROD_LENGTH = 10.5f / 16f;

    public static class State extends BlockEntityRenderState {
        /** Fraction of a flywheel turn, 0 to 1. */
        float turn;
        int flywheelColour;
    }

    @SuppressWarnings("unused")
    public StirlingEngineRenderer(BlockEntityRendererProvider.Context ctx) {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, path);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StirlingEngineBlockEntity be, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.turn = be.isHeated()
                ? (be.animationTicks + partialTick) / StirlingEngineBlockEntity.CYCLE_TICKS : 0f;
        state.flywheelColour = 0xFF000000 | be.tier().flywheelColour();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float angle = Mth.TWO_PI * state.turn;
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        float pistonZ = PISTON_TRAVEL * (1f + cos) / 2f;

        submit(collector, poseStack, BODY_MODEL_ID, state, null);

        poseStack.pushPose();
        poseStack.translate(FLYWHEEL_X, FLYWHEEL_Y, FLYWHEEL_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(360f * state.turn));
        poseStack.translate(-FLYWHEEL_X, -FLYWHEEL_Y, -FLYWHEEL_Z);
        submit(collector, poseStack, FLYWHEEL_MODEL_ID, state, new int[] {state.flywheelColour});
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0f, 0f, pistonZ);
        submit(collector, poseStack, PISTON_MODEL_ID, state, null);
        poseStack.popPose();

        // The rod runs from the piston to the crank pin on the flywheel, stretched to fit.
        float pistonEndZ = ROD_Z + pistonZ;
        float crankY = FLYWHEEL_Y - CRANK_RADIUS * sin;
        float crankZ = FLYWHEEL_Z + CRANK_RADIUS * cos;
        float dy = crankY - ROD_Y;
        float dz = crankZ - pistonEndZ;
        float length = Mth.sqrt(dy * dy + dz * dz);
        if (length > 1e-4f) {
            poseStack.pushPose();
            poseStack.translate(ROD_X, ROD_Y, pistonEndZ);
            poseStack.mulPose(Axis.XP.rotation((float) Math.atan2(-dy, dz)));
            poseStack.scale(1f, 1f, length / ROD_LENGTH);
            poseStack.translate(-ROD_X, -ROD_Y, -ROD_Z);
            submit(collector, poseStack, ROD_MODEL_ID, state, null);
            poseStack.popPose();
        }
    }

    private static void submit(SubmitNodeCollector collector, PoseStack poseStack, Identifier modelId, State state,
                               int[] tints) {
        BlockStateModelPart part = ExtraBlockModels.get(modelId);
        if (part == null) return;
        collector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), List.of(part),
                tints == null ? new int[0] : tints, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }
}
