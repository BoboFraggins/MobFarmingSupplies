package net.bobofraggins.mobfarmingsupplies.vectorplate;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A thin directional plate that applies a velocity impulse to any entity standing on it.
 *
 * <p>The block is 2px (2/16) tall so that the entity body extends into the block's 1×1×1
 * position cube, which is what causes {@code entityInside} to fire while the entity is
 * standing on top.
 *
 * <p>Placement: {@link #getStateForPlacement} stores {@code FACING} as the <em>opposite</em>
 * of the player's horizontal look direction, so if you place the block while looking south the
 * plate pushes entities north — i.e. away from you and toward the block.
 */
public class VectorPlateBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<VectorPlateBlock> CODEC = simpleCodec(VectorPlateBlock::new);

    /** Push distance added per tick in the facing direction (blocks/tick). */
    private static final double PUSH_SPEED = 0.5;

    /**
     * Fraction of the entity's offset from block-centre applied as a corrective
     * velocity on the axis perpendicular to {@link #FACING} each tick.
     * 0.1 = 10 % of the gap closed per tick, giving smooth lane-centering.
     */
    private static final double CENTERING = 0.1;

    /** Thin slab shape — 2 pixels tall, full width/depth. */
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    public VectorPlateBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<VectorPlateBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // ── Placement ──────────────────────────────────────────────────────────────

    /**
     * Stores the opposite of the player's look direction so the plate pushes
     * entities away from the placing player.
     *
     * <p>Example: player faces south → {@code FACING = NORTH} → entities go north.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    // ── Shape ──────────────────────────────────────────────────────────────────

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    // ── Entity interaction ─────────────────────────────────────────────────────

    /**
     * Called each tick while an entity's bounding box intersects the block's position cube.
     *
     * <ul>
     *   <li>Sneaking entities are not affected.</li>
     *   <li>Applies {@link #PUSH_SPEED} in the {@link #FACING} direction.</li>
     *   <li>Nudges the entity toward the block centre on the perpendicular axis
     *       so entities stay in the lane rather than drifting to one side.</li>
     *   <li>{@link Mob} entities are marked persistence-required so they do not
     *       despawn while being transported.</li>
     *   <li>{@link ItemEntity} entities are given unlimited lifetime so they
     *       cannot expire mid-transport.</li>
     * </ul>
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean flag) {
        if (level.isClientSide() || entity.isShiftKeyDown()) return;

        Direction dir = state.getValue(FACING);
        Vec3 motion = entity.getDeltaMovement();

        // Push in facing direction, capped at PUSH_SPEED so entities can't clip through walls
        int sx = dir.getStepX(), sz = dir.getStepZ();
        double current = motion.x * sx + motion.z * sz;
        double add = Math.max(0.0, PUSH_SPEED - current);
        double vx = motion.x + sx * add;
        double vz = motion.z + sz * add;

        // Centering nudge on the axis perpendicular to FACING
        if (dir.getAxis() == Direction.Axis.Z) {
            // Facing NORTH or SOUTH — nudge along X toward block centre
            vx += (pos.getX() + 0.5 - entity.getX()) * CENTERING;
        } else {
            // Facing EAST or WEST — nudge along Z toward block centre
            vz += (pos.getZ() + 0.5 - entity.getZ()) * CENTERING;
        }

        entity.setDeltaMovement(vx, motion.y, vz);
        entity.hurtMarked = true;

        // Mobs on the plate must not despawn mid-transport
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }

        // Items on the plate must not expire mid-transport
        if (entity instanceof ItemEntity ie) {
            ie.setUnlimitedLifetime();
        }
    }
}
