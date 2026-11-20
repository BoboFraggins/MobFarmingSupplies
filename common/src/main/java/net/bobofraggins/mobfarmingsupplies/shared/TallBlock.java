package net.bobofraggins.mobfarmingsupplies.shared;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Shared behaviour for machines two blocks tall (the Experience Pylon and the Mob Harvester): the
 * lower half holds the block entity and draws the whole model; the upper half is a real block, so
 * hoppers and pipes on top find the machine and nothing can be placed inside it.
 *
 * <p>The tie is one-way, so blocks placed before these machines had an upper half keep working:
 * the upper half needs the lower one under it, and taking the upper half away (but for a
 * creative player) breaks the lower one, which drops the machine; the lower half doesn't depend on
 * the upper one. A lower half without its upper half grows one ({@link #growUpper}) once there is
 * room above.
 */
public final class TallBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    /** How often a lower half without its upper half looks for room above, in ticks. */
    private static final int GROW_INTERVAL = 20;

    private TallBlock() {}

    public static boolean isUpper(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    /** The lower half's position, from either half. */
    public static BlockPos lowerPos(BlockState state, BlockPos pos) {
        return isUpper(state) ? pos.below() : pos;
    }

    /** The lower half's block entity of type {@code type}, from either half, or null. */
    @Nullable
    public static <T> T lowerBlockEntity(BlockGetter level, BlockPos pos, BlockState state, Block block, Class<T> type) {
        if (!state.is(block)) return null;
        Object be = level.getBlockEntity(lowerPos(state, pos));
        return type.isInstance(be) ? type.cast(be) : null;
    }

    /** Whether the block above the clicked position can make way for the upper half. */
    public static boolean roomAbove(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        return pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context);
    }

    /** Places the upper half over a lower half just placed (from {@code setPlacedBy}). */
    public static void placeUpper(Level level, BlockPos lowerPos, BlockState lowerState) {
        level.setBlock(lowerPos.above(), lowerState.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    /** The upper half stands only on its own lower half. */
    public static boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!isUpper(state)) return true;
        BlockState below = level.getBlockState(pos.below());
        return below.is(state.getBlock()) && !isUpper(below);
    }

    /**
     * For {@code updateShape}: air when this is an upper half whose lower half has gone, otherwise
     * {@code state} unchanged.
     */
    public static BlockState updateShape(BlockState state, Direction direction, BlockState neighborState) {
        if (isUpper(state) && direction == Direction.DOWN
                && !(neighborState.is(state.getBlock()) && !isUpper(neighborState))) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    /**
     * For {@code playerWillDestroy}: a creative player breaking the upper half takes the lower half
     * without a drop (as with doors), where a survival player's break drops the machine from it.
     */
    public static void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide() || !player.isCreative() || !isUpper(state)) return;
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.is(state.getBlock()) && !isUpper(belowState)) {
            level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            level.levelEvent(player, 2001, below, Block.getId(belowState));
        }
    }

    /**
     * For {@code onRemove}: when the upper half goes (broken, blown up, replaced), the lower half
     * breaks and drops the machine with its contents.
     */
    public static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (level.isClientSide() || !isUpper(state) || newState.is(state.getBlock())) return;
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.is(state.getBlock()) && !isUpper(belowState)) level.destroyBlock(below, true);
    }

    /**
     * Gives a lower half placed before it had an upper half (or one whose space above was taken)
     * its upper half once there's room. Call from the block entity's server tick.
     */
    public static void growUpper(Level level, BlockPos pos, BlockState state) {
        // Spread over the interval, so many machines don't all look on the same tick.
        if (isUpper(state) || Math.floorMod(level.getGameTime() - pos.asLong(), GROW_INTERVAL) != 0) return;
        BlockPos above = pos.above();
        if (above.getY() >= level.getMaxBuildHeight()) return;
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(state.getBlock()) || !aboveState.canBeReplaced()) return;
        placeUpper(level, pos, state);
    }
}
