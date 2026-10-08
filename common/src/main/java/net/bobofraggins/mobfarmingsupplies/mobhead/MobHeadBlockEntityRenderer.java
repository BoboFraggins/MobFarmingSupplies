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
        MobHeadModels.submit(state.mobType, poseStack, collector, state.lightCoords);
        poseStack.popPose();
    }
}
