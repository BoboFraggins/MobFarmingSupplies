package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Block-entity renderer for the Picnic Basket.
 *
 * <p>Renders the body and the two split lids as separate standalone models (loaded via
 * {@link ExtraBlockModels}, matching the pattern used by {@code FanBlockEntityRenderer}).
 * The lids rotate around the basket's local Z axis by up to 90° based on
 * {@link PicnicBasketBlockEntity#lidAngle}, then the whole assembly is yawed to match the
 * block's {@link PicnicBasketBlock#FACING}.
 */
public class PicnicBasketRenderer
        implements BlockEntityRenderer<PicnicBasketBlockEntity, PicnicBasketRenderer.State> {

    public static final Identifier BODY_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/picnic_basket_body");
    public static final Identifier LEFT_LID_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/picnic_basket_left_lid");
    public static final Identifier RIGHT_LID_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/picnic_basket_right_lid");

    // Scratch quaternions reused every frame (submit runs single-threaded).
    private static final Quaternionf YAW_QUAT = new Quaternionf();
    private static final Quaternionf LID_QUAT = new Quaternionf();

    @SuppressWarnings("unused")
    public PicnicBasketRenderer(BlockEntityRendererProvider.Context ctx) {}

    public static class State extends BlockEntityRenderState {
        float facingYRotDeg;
        float openFraction;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    private static float facingYRotDeg(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180f;
            case EAST -> 270f;
            case WEST -> 90f;
            default -> 0f;
        };
    }

    @Override
    public void extractRenderState(
            PicnicBasketBlockEntity be,
            State state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(PicnicBasketBlock.FACING);
        state.facingYRotDeg = facingYRotDeg(facing);
        state.openFraction = Mth.lerp(partialTick, be.prevLidAngle, be.lidAngle);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        BlockStateModelPart body = ExtraBlockModels.get(BODY_MODEL_ID);
        BlockStateModelPart leftLid = ExtraBlockModels.get(LEFT_LID_MODEL_ID);
        BlockStateModelPart rightLid = ExtraBlockModels.get(RIGHT_LID_MODEL_ID);

        int light = state.lightCoords;
        float yawRad = state.facingYRotDeg * Mth.DEG_TO_RAD;
        float openRad = state.openFraction * 90f * Mth.DEG_TO_RAD;

        if (body != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(YAW_QUAT.rotationY(yawRad));
            poseStack.translate(-0.5, -0.5, -0.5);
            submitPart(collector, poseStack, body, light);
            poseStack.popPose();
        }

        if (leftLid != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(YAW_QUAT.rotationY(yawRad));
            poseStack.mulPose(LID_QUAT.rotationZ(openRad));
            poseStack.translate(-0.5, -0.5, -0.5);
            submitPart(collector, poseStack, leftLid, light);
            poseStack.popPose();
        }

        if (rightLid != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(YAW_QUAT.rotationY(yawRad));
            poseStack.mulPose(LID_QUAT.rotationZ(-openRad));
            poseStack.translate(-0.5, -0.5, -0.5);
            submitPart(collector, poseStack, rightLid, light);
            poseStack.popPose();
        }
    }

    private static void submitPart(
            SubmitNodeCollector collector, PoseStack poseStack, BlockStateModelPart part, int light) {
        collector.submitBlockModel(
                poseStack,
                Sheets.cutoutBlockItemSheet(),
                List.of(part),
                new int[0],
                light,
                OverlayTexture.NO_OVERLAY,
                0);
    }
}
