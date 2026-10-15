package net.bobofraggins.mobfarmingsupplies.toilet;

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
import org.joml.Quaternionf;

/**
 * Draws the toilet's lid ({@code block/toilet_lid}, the "lid" group of {@code block/toilet}),
 * swung up about its hinge by {@link ToiletBlockEntity#getLidAngle}. The block model itself is
 * the bowl only ({@code block/toilet_bowl}).
 */
public class ToiletBlockEntityRenderer
        extends StateBlockEntityRenderer<ToiletBlockEntity, ToiletBlockEntityRenderer.ToiletState> {

    public static final ResourceLocation LID_MODEL_ID =
            ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/toilet_lid");

    // Hinge, in north-facing model space: the lid's back top edge, just in front of the cistern.
    // Swinging 90 degrees about X stands the lid up at z 10-11, y 6-16.
    private static final float HINGE_Y = 6f / 16f;
    private static final float HINGE_Z = 11f / 16f;

    // Scratch quaternions reused every frame (submit runs single-threaded).
    private static final Quaternionf FACING_QUAT = new Quaternionf();
    private static final Quaternionf LID_QUAT = new Quaternionf();

    @SuppressWarnings("unused")
    public ToiletBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    public static class ToiletState extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        float lidAngleDeg;
    }

    @Override
    public ToiletState createRenderState() {
        return new ToiletState();
    }

    @Override
    public void extractRenderState(ToiletBlockEntity be, ToiletState state, float partialTick) {
        state.facing = be.getBlockState().getValue(ToiletBlock.FACING);
        state.lidAngleDeg = be.getLidAngle(partialTick);
    }

    @Override
    public void submit(ToiletState state, PoseStack poseStack, MultiBufferSource buffers) {
        BakedModel lid = ExtraBlockModels.get(LID_MODEL_ID);
        if (lid == null) return;

        poseStack.pushPose();
        // Turn to the block's facing about the block centre (the model faces north).
        poseStack.translate(0.5, 0.5, 0.5);
        switch (state.facing) {
            case EAST  -> poseStack.mulPose(FACING_QUAT.rotationY(-(float) (Math.PI / 2)));
            case SOUTH -> poseStack.mulPose(FACING_QUAT.rotationY( (float)  Math.PI));
            case WEST  -> poseStack.mulPose(FACING_QUAT.rotationY( (float) (Math.PI / 2)));
            default    -> {} // NORTH: no rotation
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        // Swing the lid up about its hinge: the front edge rises, the top ends up facing the cistern.
        poseStack.translate(0, HINGE_Y, HINGE_Z);
        poseStack.mulPose(LID_QUAT.rotationX(state.lidAngleDeg * Mth.DEG_TO_RAD));
        poseStack.translate(0, -HINGE_Y, -HINGE_Z);

        ExtraBlockModels.render(poseStack, buffers, lid, Sheets.cutoutBlockSheet(), state.lightCoords, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
