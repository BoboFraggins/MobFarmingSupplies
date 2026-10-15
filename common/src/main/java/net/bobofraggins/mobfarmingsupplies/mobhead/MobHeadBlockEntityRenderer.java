package net.bobofraggins.mobfarmingsupplies.mobhead;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bobofraggins.mobfarmingsupplies.shared.render.BlockEntityRenderState;
import net.bobofraggins.mobfarmingsupplies.shared.render.StateBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Draws a placed Mob Head where vanilla would draw a skull: vanilla's own skull transforms (on the
 * floor in 16 rotations, or hung on a wall), then the mob's head ({@link MobHeadModels}).
 */
public class MobHeadBlockEntityRenderer extends StateBlockEntityRenderer<MobHeadBlockEntity, MobHeadBlockEntityRenderer.State> {

    /** A head's size in skull space: half a block, as vanilla's. */
    private static final float HEAD_SIZE = 0.5f;
    /** How far the rear and base are pulled in (1/16 of a texel): enough to win the depth test. */
    private static final float INSET = 1f / 256f;
    private static final float INSET_SCALE = 1f - INSET / HEAD_SIZE;

    public static class State extends BlockEntityRenderState {
        @Nullable EntityType<?> mobType;
        /** Wall heads: the wall's facing; standing heads: null. */
        @Nullable Direction wallFacing;
        /** Turn about the vertical, in degrees, as vanilla's skulls. */
        float yRotDeg;
    }

    public MobHeadBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MobHeadBlockEntity be, State state, float partialTick) {
        state.mobType = be.getMobType();
        BlockState blockState = be.getBlockState();
        state.wallFacing = blockState.hasProperty(MobWallHeadBlock.FACING)
                ? blockState.getValue(MobWallHeadBlock.FACING) : null;
        state.yRotDeg = 22.5f * (state.wallFacing != null
                ? (2 + state.wallFacing.get2DDataValue()) * 4
                : blockState.getValue(MobHeadBlock.ROTATION));
    }

    @Override
    public void submit(State state, PoseStack poseStack, MultiBufferSource buffers) {
        if (state.mobType == null) return;
        poseStack.pushPose();
        // Into skull space, as vanilla's SkullBlockRenderer.renderSkull does.
        Direction wall = state.wallFacing;
        if (wall == null) {
            poseStack.translate(0.5f, 0.0f, 0.5f);
        } else {
            poseStack.translate(0.5f - wall.getStepX() * 0.25f, 0.25f, 0.5f - wall.getStepZ() * 0.25f);
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRotDeg));
        // The head's back sits flush on the wall and its base on the block below. Vanilla skulls
        // hide that with a z-offset render type, but ours use the mob's own (and the Enderman's
        // eyes pass), so pull the rear and base in by a hair, keeping the front and top in place.
        // Skull space has y down and the face toward -z: the top is at y = -HEAD_SIZE and the
        // front at z = -HEAD_SIZE / 2.
        poseStack.translate(0f, -HEAD_SIZE, -HEAD_SIZE / 2);
        poseStack.scale(1f, INSET_SCALE, INSET_SCALE);
        poseStack.translate(0f, HEAD_SIZE, HEAD_SIZE / 2);
        MobHeadModels.submit(state.mobType, poseStack, buffers, state.lightCoords);
        poseStack.popPose();
    }
}
