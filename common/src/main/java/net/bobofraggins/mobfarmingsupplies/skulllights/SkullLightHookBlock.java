package net.bobofraggins.mobfarmingsupplies.skulllights;

import java.util.EnumMap;
import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * One end of a String of Skull Lights: a small hook on the face of a solid block. Hooks come in
 * pairs, placed together when a string is hung, and have no item of their own: breaking either
 * one takes the string down and drops it (see {@link SkullLightHookBlockEntity}).
 */
public class SkullLightHookBlock extends Block implements EntityBlock {

    /** The way the hook sticks out of its wall: the face of the block it's on. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Map.of(
            Direction.UP, Block.box(6, 0, 6, 10, 3, 10),
            Direction.DOWN, Block.box(6, 13, 6, 10, 16, 10),
            Direction.NORTH, Block.box(6, 6, 13, 10, 10, 16),
            Direction.SOUTH, Block.box(6, 6, 0, 10, 10, 3),
            Direction.WEST, Block.box(13, 6, 6, 16, 10, 10),
            Direction.EAST, Block.box(0, 6, 6, 3, 10, 10)));

    public SkullLightHookBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** Whether a hook facing {@code facing} can hang at {@code pos}: the block behind it has a solid face there. */
    public static boolean canHang(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canHang(level, pos, state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // A creative player taking a string down doesn't get it back, as with any block.
        if (player.isCreative() && level.getBlockEntity(pos) instanceof SkullLightHookBlockEntity hook) hook.dropNothing();
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(Registration.STRING_OF_SKULL_LIGHTS.get());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkullLightHookBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != Registration.SKULL_LIGHT_HOOK_BE_TYPE.get()) return null;
        return (lvl, pos, st, be) -> ((SkullLightHookBlockEntity) be).serverTick((ServerLevel) lvl);
    }
}
