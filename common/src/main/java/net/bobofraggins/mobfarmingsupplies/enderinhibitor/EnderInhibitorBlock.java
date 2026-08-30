package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An ender-teleport-suppression block that attaches to any face (floor, ceiling, walls).
 *
 * <p>The {@code facing} property records the direction the block <em>faces</em> —
 * i.e. the direction away from its supporting surface — so the model's base always
 * rests against the support block.  This mirrors the convention used by vanilla's
 * end rod and lightning rod.
 *
 * <p>Like a torch, the block pops off as a drop when its support is broken.
 *
 * <p>Suppression logic lives in {@link EnderInhibitorEvents}.
 */
public class EnderInhibitorBlock extends Block implements EntityBlock {

    public static final MapCodec<EnderInhibitorBlock> CODEC = simpleCodec(EnderInhibitorBlock::new);

    /** Direction the block points <em>away</em> from its support. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    // ── Collision / selection shapes ─────────────────────────────────────────────
    //
    // The model's base plate is only ~3–13 px wide (a 10×10 px footprint centred on
    // the mounting face), so an inset hitbox of the same size makes most clicks near
    // the base land on the support block instead — only the narrow column near the
    // prongs reliably registers. The shape is widened to the full mounting face
    // (0–16 px) so the whole base is clickable, while keeping the same depth (how
    // far the prongs reach out from the surface, ~6 px).

    private static final VoxelShape SHAPE_UP    = Block.box( 0,  0,  0, 16,  6, 16);
    private static final VoxelShape SHAPE_DOWN  = Block.box( 0, 10,  0, 16, 16, 16);
    private static final VoxelShape SHAPE_NORTH = Block.box( 0,  0,  0, 16, 16,  6);
    private static final VoxelShape SHAPE_SOUTH = Block.box( 0,  0, 10, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST  = Block.box(10,  0,  0, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST  = Block.box( 0,  0,  0,  6, 16, 16);

    // ── Construction ─────────────────────────────────────────────────────────────

    public EnderInhibitorBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public MapCodec<EnderInhibitorBlock> codec() {
        return CODEC;
    }

    // ── Block-state definition ───────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // ── Placement ────────────────────────────────────────────────────────────────

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The clicked face is the face of the support block the player aimed at;
        // that face direction is exactly what FACING should be.
        BlockState candidate = defaultBlockState().setValue(FACING, context.getClickedFace());
        if (candidate.canSurvive(context.getLevel(), context.getClickedPos())) {
            return candidate;
        }
        return null; // no valid placement here
    }

    // ── Survival ─────────────────────────────────────────────────────────────────

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing   = state.getValue(FACING);
        BlockPos  support  = pos.relative(facing.getOpposite());
        // Require a center-sturdy face on the side we attach to
        return Block.canSupportCenter(level, support, facing);
    }

    // ── Torch-like pop-off when support breaks ───────────────────────────────────

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random) {
        // Only react to changes on the side our base is attached to
        if (direction == state.getValue(FACING).getOpposite()
                && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduledTickAccess, pos,
                direction, neighborPos, neighborState, random);
    }

    // ── Ambient particles ────────────────────────────────────────────────────────

    /**
     * Emits two {@link ParticleTypes#PORTAL} particles per visual tick, scattered
     * around the prong volume.  The emission centre is pushed 0.3 blocks in the
     * {@link #FACING} direction so the cloud appears to hover around the prong tips
     * rather than inside the wall.  Velocity follows the same pattern the Enderman
     * uses: random horizontal spread ± 1, slight downward drift.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);

        // Centre of emission — block centre + 0.3 toward the prong tips
        double cx = pos.getX() + 0.5 + facing.getStepX() * 0.3;
        double cy = pos.getY() + 0.5 + facing.getStepY() * 0.3;
        double cz = pos.getZ() + 0.5 + facing.getStepZ() * 0.3;

        for (int i = 0; i < 2; i++) {
            // Spread across the ~10 px (0.625 block) prong cross-section
            double px = cx + (random.nextDouble() - 0.5) * 0.625;
            double py = cy + (random.nextDouble() - 0.5) * 0.625;
            double pz = cz + (random.nextDouble() - 0.5) * 0.625;

            // Enderman-style velocity: wide horizontal scatter, slight downward drift
            double vx = (random.nextDouble() - 0.5) * 2.0;
            double vy = -random.nextDouble();
            double vz = (random.nextDouble() - 0.5) * 2.0;

            level.addParticle(ParticleTypes.PORTAL, px, py, pz, vx, vy, vz);
        }
    }

    // ── VoxelShape ───────────────────────────────────────────────────────────────

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                   CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN  -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST  -> SHAPE_EAST;
            case WEST  -> SHAPE_WEST;
            default    -> SHAPE_UP;
        };
    }

    // ── Block entity ─────────────────────────────────────────────────────────────

    /** Creates the block entity that the BER uses to render the floating Ender Pearl. */
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderInhibitorBlockEntity(pos, state);
    }

    // ── Interaction ─────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof EnderInhibitorBlockEntity be
                    && player instanceof ServerPlayer sp) {
                MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.SUCCESS;
    }

    // ── Structure rotation / mirror support ──────────────────────────────────────

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
