package net.bobofraggins.mobfarmingsupplies.haltingplate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A thin plate (the Vector Plate's shape) that holds any mob or player standing on it in place:
 * no walking, no being pushed, no jumping. Turning is unaffected. A sneaking player can step off,
 * and anything falling onto it lands normally before being held.
 *
 * <p>Uses vanilla's cobweb mechanism ({@link Entity#makeStuckInBlock}), which scales the entity's
 * next movement and clears its velocity. That runs on both sides, so it also holds a player, whose
 * movement is decided by their own client. Items are not held.
 */
public class HaltingPlateBlock extends Block {

    /**
     * Movement multiplier while standing on the plate - next to nothing. Not zero: Entity#move only
     * applies a stuck multiplier whose squared length is above 1.0E-7.
     */
    private static final Vec3 HELD = new Vec3(0.001, 0.001, 0.001);

    /** Thin slab shape - 2 pixels tall, so an entity standing on it is inside this block's cube. */
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    public HaltingPlateBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean flag) {
        if (!(entity instanceof LivingEntity) || entity.isShiftKeyDown()) return;
        // Only once it has landed: the hold slows falling as much as anything, so a mob dropped
        // onto the plate would otherwise hang in the air just above it. Jumping is still stopped.
        if (!entity.onGround()) return;
        entity.makeStuckInBlock(state, HELD);
    }
}
