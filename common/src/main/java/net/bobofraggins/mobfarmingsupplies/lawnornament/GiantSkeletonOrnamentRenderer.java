package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the Giant Skeleton Lawn Ornament: vanilla's skeleton model at {@value #SCALE} times life
 * size, so four blocks tall, standing on its block and facing the way it was placed.
 */
public class GiantSkeletonOrnamentRenderer
        implements BlockEntityRenderer<GiantSkeletonOrnamentBlockEntity, GiantSkeletonOrnamentRenderer.State> {

    /** A skeleton is two blocks tall; this makes it four. */
    static final float SCALE = 2.0f;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        GiantSkeletonModel.Pose pose = GiantSkeletonModel.Pose.FORWARD;
    }

    private final GiantSkeletonModel model;

    public GiantSkeletonOrnamentRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new GiantSkeletonModel(ctx.bakeLayer(ModelLayers.SKELETON));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GiantSkeletonOrnamentBlockEntity be, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.facing = be.facing();
        state.pose = new GiantSkeletonModel.Pose(be.headYaw(partialTick), be.headPitch(partialTick));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        submitSkeleton(model, state.pose, state.facing.toYRot(), SCALE, poseStack, collector, state.lightCoords);
        poseStack.popPose();
    }

    /**
     * Draws the skeleton standing on the pose's origin, {@code scale} times life size, facing
     * {@code bodyYaw} (degrees, as an entity's: 0 is south), the way a living entity's renderer
     * places its model.
     */
    static void submitSkeleton(GiantSkeletonModel model, GiantSkeletonModel.Pose pose, float bodyYaw, float scale,
                               PoseStack poseStack, SubmitNodeCollector collector, int light) {
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(180f - bodyYaw));
        poseStack.scale(scale, scale, scale);
        poseStack.scale(-1f, -1f, 1f);
        poseStack.translate(0f, -1.501f, 0f);
        collector.submitModel(model, pose, poseStack, GiantSkeletonModel.TEXTURE, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    /** It stands four blocks tall: keep drawing it while its base block is off screen. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    /**
     * The box the skeleton fits in. NeoForge culls a block entity's renderer by this box (its
     * loader renderer overrides {@code getRenderBoundingBox} with it), so the skeleton still draws
     * when only its head is on screen.
     */
    public static AABB renderBounds(GiantSkeletonOrnamentBlockEntity ornament) {
        return new AABB(ornament.getBlockPos()).expandTowards(0, 2 * SCALE, 0).inflate(SCALE / 2);
    }
}
