package net.bobofraggins.mobfarmingsupplies.mobhead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Draws a placed Mob Head where vanilla would draw a skull: vanilla's own skull transforms (on the
 * floor in 16 rotations, or hung on a wall), then the mob's head ({@link MobHeadModels}).
 */
public class MobHeadBlockEntityRenderer implements BlockEntityRenderer<MobHeadBlockEntity, MobHeadBlockEntityRenderer.State> {

    /** A head's size in skull space: half a block, as vanilla's. */
    private static final float HEAD_SIZE = 0.5f;
    /** How far the rear and base are pulled in (1/16 of a texel): enough to win the depth test. */
    private static final float INSET = 1f / 256f;
    private static final float INSET_SCALE = 1f - INSET / HEAD_SIZE;

    public static class State extends BlockEntityRenderState {
        @Nullable EntityType<?> mobType;
        Transformation transformation = Transformation.IDENTITY;
    }

    public MobHeadBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MobHeadBlockEntity be, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.mobType = be.getMobType();
        BlockState blockState = be.getBlockState();
        state.transformation = blockState.hasProperty(MobWallHeadBlock.FACING)
                ? SkullBlockRenderer.TRANSFORMATIONS.wallTransformation(blockState.getValue(MobWallHeadBlock.FACING))
                : SkullBlockRenderer.TRANSFORMATIONS.freeTransformations(blockState.getValue(MobHeadBlock.ROTATION));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.mobType == null) return;
        poseStack.pushPose();
        poseStack.mulPose(state.transformation);
        // The head's back sits flush on the wall and its base on the block below. Vanilla skulls
        // hide that with a z-offset render type, but ours use the mob's own (and the Enderman's
        // eyes pass), so pull the rear and base in by a hair, keeping the front and top in place.
        // Skull space has y down and the face toward -z: the top is at y = -HEAD_SIZE and the
        // front at z = -HEAD_SIZE / 2.
        poseStack.translate(0f, -HEAD_SIZE, -HEAD_SIZE / 2);
        poseStack.scale(1f, INSET_SCALE, INSET_SCALE);
        poseStack.translate(0f, HEAD_SIZE, HEAD_SIZE / 2);
        MobHeadModels.submit(state.mobType, poseStack, collector, state.lightCoords);
        poseStack.popPose();
    }
}
