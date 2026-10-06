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
 * A thin plate (the Vector Plate's shape) that draws any mob or player standing on it to its
 * center and holds them there. Movement that brings them closer to the center is allowed; anything
 * else (walking away or sideways, being pushed, knockback, jumping) is not. Once at the center they
 * can't move at all. Turning is unaffected. A sneaking player can step off, and anything falling
 * onto it lands normally before being held.
 *
 * <p>Jumping and the final hold use vanilla's cobweb mechanism ({@link Entity#makeStuckInBlock}),
 * which scales the entity's next movement and clears its velocity on both sides. The "toward the
 * center only" rule is applied after each move by the side that decides the entity's movement -
 * the server for mobs, the player's own client for players - so it never fights the other side.
 * Items are not held.
 */
public class HaltingPlateBlock extends Block {

    /**
     * Movement multiplier while standing on the plate - next to nothing. Not zero: Entity#move only
     * applies a stuck multiplier whose squared length is above 1.0E-7.
     */
    private static final Vec3 HELD = new Vec3(0.001, 0.001, 0.001);

    /** Away from the center: horizontal movement is left to {@link #keepMovingInward}, jumping is blocked. */
    private static final Vec3 NO_JUMP = new Vec3(1.0, 0.001, 1.0);

    /** Within this horizontal distance of the center, the entity is fully held. */
    private static final double CENTER_RADIUS = 0.05;

    /** A larger move than this in one tick is a teleport, not walking - leave it alone. */
    private static final double MAX_STEP = 1.0;

    /** Height of the plate's top surface within its block. */
    private static final double TOP = 2.0 / 16.0;

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
        // Judged by where the entity was at the start of this tick, not after this tick's move:
        // a hard push can carry it past the edge (and off the ground) within that one move.
        // Only once it has landed: the hold slows falling as much as anything, so a mob dropped
        // onto the plate would otherwise hang in the air just above it. Jumping is still stopped.
        if (entity.yo > pos.getY() + TOP + 1.0E-3) return;
        // Only the plate its center was over applies, so neighbouring plates don't each pull
        // toward their own center and pin it on the seam.
        if (!BlockPos.containing(entity.xo, entity.yo, entity.zo).equals(pos)) return;

        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        if (entity.isLocalInstanceAuthoritative()) keepMovingInward(entity, cx, cz, pos.getY() + TOP);

        double dx = entity.getX() - cx;
        double dz = entity.getZ() - cz;
        entity.makeStuckInBlock(state, dx * dx + dz * dz <= CENTER_RADIUS * CENTER_RADIUS ? HELD : NO_JUMP);
    }

    /**
     * Undoes the part of this tick's horizontal move that didn't head toward the center: the
     * entity ends up at the furthest point along the line from where it started the tick to the
     * center that its move reached, never past the center.
     */
    private static void keepMovingInward(Entity entity, double cx, double cz, double top) {
        double moveX = entity.getX() - entity.xo;
        double moveZ = entity.getZ() - entity.zo;
        if (moveX * moveX + moveZ * moveZ > MAX_STEP * MAX_STEP) return;

        double inX = cx - entity.xo;
        double inZ = cz - entity.zo;
        double toCenter = Math.sqrt(inX * inX + inZ * inZ);
        double newX = entity.xo;
        double newZ = entity.zo;
        if (toCenter > 1.0E-6) {
            double along = Math.clamp((moveX * inX + moveZ * inZ) / toCenter, 0.0, toCenter);
            newX += inX / toCenter * along;
            newZ += inZ / toCenter * along;
        }
        if (Math.abs(newX - entity.getX()) > 1.0E-6 || Math.abs(newZ - entity.getZ()) > 1.0E-6) {
            // Back over the plate, so not below its surface even if it had started to drop off.
            entity.setPos(newX, Math.max(entity.getY(), top), newZ);
        }
    }
}
