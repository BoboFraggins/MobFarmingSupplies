package net.bobofraggins.mobfarmingsupplies.toilet;

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
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Draws the toilet's lid ({@code block/toilet_lid}, the "lid" group of {@code block/toilet}),
 * swung up about its hinge by {@link ToiletBlockEntity#getLidAngle}. The block model itself is
 * the bowl only ({@code block/toilet_bowl}).
 */
public class ToiletBlockEntityRenderer
        implements BlockEntityRenderer<ToiletBlockEntity, ToiletBlockEntityRenderer.ToiletState> {

    public static final Identifier LID_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/toilet_lid");

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
    public void extractRenderState(
            ToiletBlockEntity be,
            ToiletState state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.facing = be.getBlockState().getValue(ToiletBlock.FACING);
        state.lidAngleDeg = be.getLidAngle(partialTick);
    }

    @Override
    public void submit(
            ToiletState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        BlockStateModelPart lid = ExtraBlockModels.get(LID_MODEL_ID);
        if (lid == null) return;

        poseStack.pushPose();
        // Turn to the block's facing about the block centre (the model faces north).
        poseStack.translate(0.5, 0.5, 0.5);
        switch (state.facing) {
            case EAST  -> poseStack.rotate(FACING_QUAT.rotationY(-(float) (Math.PI / 2)));
            case SOUTH -> poseStack.rotate(FACING_QUAT.rotationY( (float)  Math.PI));
            case WEST  -> poseStack.rotate(FACING_QUAT.rotationY( (float) (Math.PI / 2)));
            default    -> {} // NORTH: no rotation
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        // Swing the lid up about its hinge: the front edge rises, the top ends up facing the cistern.
        poseStack.translate(0, HINGE_Y, HINGE_Z);
        poseStack.rotate(LID_QUAT.rotationX(state.lidAngleDeg * Mth.DEG_TO_RAD));
        poseStack.translate(0, -HINGE_Y, -HINGE_Z);

        collector.submitBlockModel(
                poseStack,
                Sheets.cutoutBlockItemSheet(),
                List.of(lid),
                new int[0],
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0);
        poseStack.popPose();
    }
}
