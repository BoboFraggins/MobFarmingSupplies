package net.bobofraggins.mobfarmingsupplies.colorworkstation;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import java.util.EnumMap;
import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The Color Workstation: a workbench base with a tank of Essence of Color on it, two blocks tall.
 * Like a door it fills both blocks; the lower half holds the buffer and the dyes
 * ({@link ColorWorkstationBlockEntity}) and draws the whole model. Use either half to open it, or
 * to pour in a bucket of Essence of Color; pipes can fill it, and pull dyes, at either half.
 * Its front faces whoever placed it.
 */
public class ColorWorkstationBlock extends BaseEntityBlock {

    public static final MapCodec<ColorWorkstationBlock> CODEC = simpleCodec(ColorWorkstationBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** The base, the raised back of the bench (to the south when facing north), and the tank's foot. */
    private static final Map<Direction, VoxelShape> LOWER = new EnumMap<>(Direction.class);
    private static final VoxelShape UPPER = Block.box(0, 0, 0, 16, 16, 16);

    static {
        VoxelShape base = Block.box(0, 0, 0, 16, 8, 16);
        VoxelShape tank = Block.box(0, 14, 0, 16, 16, 16);
        LOWER.put(Direction.NORTH, Shapes.or(base, Block.box(0, 8, 6, 16, 14, 16), tank));
        LOWER.put(Direction.SOUTH, Shapes.or(base, Block.box(0, 8, 0, 16, 14, 10), tank));
        LOWER.put(Direction.EAST, Shapes.or(base, Block.box(0, 8, 0, 10, 14, 16), tank));
        LOWER.put(Direction.WEST, Shapes.or(base, Block.box(6, 8, 0, 16, 14, 16), tank));
    }

    public ColorWorkstationBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING);
    }

    /** The lower half's position, from either half. */
    public static BlockPos lowerPos(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    /** The workstation at either half's position, or null. */
    @Nullable
    public static ColorWorkstationBlockEntity blockEntity(BlockGetter level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof ColorWorkstationBlock)) return null;
        return level.getBlockEntity(lowerPos(state, pos)) instanceof ColorWorkstationBlockEntity be ? be : null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER.get(state.getValue(FACING)) : UPPER;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxY() || !level.getBlockState(pos.above()).canBeReplaced(context)) return null;
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) return true;
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            // The other half went: so does this one.
            return neighborState.is(this) && neighborState.getValue(HALF) != half
                    ? state : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // A creative player breaking the top half shouldn't get a workstation from the bottom half.
        if (!level.isClientSide() && player.isCreative() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.is(this) && belowState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, below, Block.getId(belowState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && blockEntity(level, pos, state) instanceof ColorWorkstationBlockEntity be
                && player instanceof ServerPlayer sp) {
            BlockPos lower = be.getBlockPos();
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(lower));
        }
        return InteractionResult.SUCCESS;
    }

    /** A bucket (or other container) of Essence of Color pours in; anything else opens it. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.BUCKET)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        boolean poured = TankBlockPlatform.handleFluidItemInteraction(player, hand, level, pos, false);
        return poured ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ColorWorkstationBlockEntity(pos, state) : null;
    }
}
