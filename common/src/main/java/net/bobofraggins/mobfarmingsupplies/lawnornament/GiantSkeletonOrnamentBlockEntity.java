package net.bobofraggins.mobfarmingsupplies.lawnornament;

import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Where the Giant Skeleton Lawn Ornament's head is looking. Client only: every tick the head turns
 * part of the way towards the nearest player within {@value #WATCH_RANGE} blocks, and back to the
 * front when nobody is near. Nothing is saved.
 */
public class GiantSkeletonOrnamentBlockEntity extends BlockEntity {

    /** How near a player must be to be watched. */
    static final double WATCH_RANGE = 8.0;
    /** The skeleton is drawn twice life size; its eyes are this high above the block, in blocks. */
    static final double EYE_HEIGHT = 1.74 * GiantSkeletonOrnamentRenderer.SCALE;
    /** How far the head turns from the body at most, either way, in degrees. */
    private static final float MAX_HEAD_YAW = 75f;
    private static final float MAX_HEAD_PITCH = 40f;
    /** Share of the remaining turn made each tick. */
    private static final float TURN_PER_TICK = 0.25f;

    private float headYaw, headPitch;
    private float prevHeadYaw, prevHeadPitch;

    public GiantSkeletonOrnamentBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("giant_skeleton_lawn_ornament"), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, GiantSkeletonOrnamentBlockEntity be) {
        be.prevHeadYaw = be.headYaw;
        be.prevHeadPitch = be.headPitch;
        float targetYaw = 0f, targetPitch = 0f;
        double cx = pos.getX() + 0.5, eyeY = pos.getY() + EYE_HEIGHT, cz = pos.getZ() + 0.5;
        Player player = level.getNearestPlayer(cx, eyeY, cz, WATCH_RANGE, EntitySelector.NO_SPECTATORS);
        if (player != null) {
            double dx = player.getX() - cx, dy = player.getEyeY() - eyeY, dz = player.getZ() - cz;
            float bodyYaw = state.getValue(GiantSkeletonOrnamentBlock.FACING).toYRot();
            float worldYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            targetYaw = Mth.clamp(Mth.wrapDegrees(worldYaw - bodyYaw), -MAX_HEAD_YAW, MAX_HEAD_YAW);
            targetPitch = Mth.clamp((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))),
                    -MAX_HEAD_PITCH, MAX_HEAD_PITCH);
        }
        be.headYaw += Mth.wrapDegrees(targetYaw - be.headYaw) * TURN_PER_TICK;
        be.headPitch += (targetPitch - be.headPitch) * TURN_PER_TICK;
    }

    /** Head yaw relative to the body, in degrees, between ticks. */
    float headYaw(float partialTick) {
        return prevHeadYaw + Mth.wrapDegrees(headYaw - prevHeadYaw) * partialTick;
    }

    /** Head pitch in degrees (positive looks down), between ticks. */
    float headPitch(float partialTick) {
        return Mth.lerp(partialTick, prevHeadPitch, headPitch);
    }

    Direction facing() {
        return getBlockState().getValue(GiantSkeletonOrnamentBlock.FACING);
    }
}
