package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.bobofraggins.mobfarmingsupplies.shared.render.BlockEntityRenderState;
import net.bobofraggins.mobfarmingsupplies.shared.render.StateBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;


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
        extends StateBlockEntityRenderer<PicnicBasketBlockEntity, PicnicBasketRenderer.State> {

    public static final ResourceLocation BODY_MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/picnic_basket_body");
    public static final ResourceLocation LEFT_LID_MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/picnic_basket_left_lid");
    public static final ResourceLocation RIGHT_LID_MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/picnic_basket_right_lid");

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
    public void extractRenderState(PicnicBasketBlockEntity be, State state, float partialTick) {
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(PicnicBasketBlock.FACING);
        state.facingYRotDeg = facingYRotDeg(facing);
        state.openFraction = Mth.lerp(partialTick, be.prevLidAngle, be.lidAngle);
    }

    @Override
    public void submit(State state, PoseStack poseStack, MultiBufferSource buffers) {
        BakedModel body = ExtraBlockModels.get(BODY_MODEL_ID);
        BakedModel leftLid = ExtraBlockModels.get(LEFT_LID_MODEL_ID);
        BakedModel rightLid = ExtraBlockModels.get(RIGHT_LID_MODEL_ID);

        int light = state.lightCoords;
        float yawRad = state.facingYRotDeg * Mth.DEG_TO_RAD;
        float openRad = state.openFraction * 90f * Mth.DEG_TO_RAD;

        if (body != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(YAW_QUAT.rotationY(yawRad));
            poseStack.translate(-0.5, -0.5, -0.5);
            submitPart(buffers, poseStack, body, light);
            poseStack.popPose();
        }

        if (leftLid != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(YAW_QUAT.rotationY(yawRad));
            poseStack.mulPose(LID_QUAT.rotationZ(openRad));
            poseStack.translate(-0.5, -0.5, -0.5);
            submitPart(buffers, poseStack, leftLid, light);
            poseStack.popPose();
        }

        if (rightLid != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(YAW_QUAT.rotationY(yawRad));
            poseStack.mulPose(LID_QUAT.rotationZ(-openRad));
            poseStack.translate(-0.5, -0.5, -0.5);
            submitPart(buffers, poseStack, rightLid, light);
            poseStack.popPose();
        }
    }

    private static void submitPart(
            MultiBufferSource buffers, PoseStack poseStack, BakedModel part, int light) {
        ExtraBlockModels.render(poseStack, buffers, part, Sheets.cutoutBlockSheet(), light, OverlayTexture.NO_OVERLAY);
    }
}
