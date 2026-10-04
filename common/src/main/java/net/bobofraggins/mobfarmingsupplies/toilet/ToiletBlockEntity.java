package net.bobofraggins.mobfarmingsupplies.toilet;

import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Toilet block entity. Holds no data; it exists so {@link ToiletBlockEntityRenderer} can draw the
 * lid, which swings toward the {@link ToiletBlock#OPEN} state over a few ticks (client side only).
 */
public class ToiletBlockEntity extends BlockEntity {

    /** Lid angle when fully up, in degrees. */
    static final float OPEN_ANGLE = 90f;
    /** Degrees the lid moves per tick - fully up or down in 10 ticks (half a second). */
    private static final float SWING_PER_TICK = 9f;

    private float lidAngle;
    private float prevLidAngle;

    public ToiletBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("toilet"), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ToiletBlockEntity be) {
        be.prevLidAngle = be.lidAngle;
        float target = state.getValue(ToiletBlock.OPEN) ? OPEN_ANGLE : 0f;
        be.lidAngle = be.lidAngle < target
                ? Math.min(target, be.lidAngle + SWING_PER_TICK)
                : Math.max(target, be.lidAngle - SWING_PER_TICK);
    }

    /** Lid angle in degrees (0 = closed, {@value #OPEN_ANGLE} = up), smoothed between ticks. */
    public float getLidAngle(float partialTick) {
        return Mth.lerp(partialTick, prevLidAngle, lidAngle);
    }
}
