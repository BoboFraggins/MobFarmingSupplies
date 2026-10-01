package net.bobofraggins.mobfarmingsupplies.filterscribingterminal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.bobofraggins.mobfarmingsupplies.shared.menu.ExtendedMenus;

/**
 * A desk terminal (model ported from TremendousStorage's Storage Access Terminal) that opens
 * the filter-scribing UI when right-clicked. It needs no power and joins no network.
 *
 * <p>{@link #ACTIVE} only drives the model's screen: it is on while at least one player has
 * this terminal's menu open. There's no stored counter — whether anyone is viewing is
 * recomputed from the players' open menus ({@link #hasViewers}), so it can't drift. The block
 * entity only holds the Blank Filter input so it persists between uses.
 * While active, a scheduled tick re-checks every second, which also recovers a terminal left
 * on by a crash or an unexpected disconnect (scheduled ticks persist with the chunk).
 */
public class FilterScribingTerminalBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final int RECHECK_TICKS = 20;

    // The model is authored facing north (screen and keyboard toward -Z). Each shape covers
    // the body plus the keyboard/mouse strip in front of it, so the whole desk is clickable.
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(4, 0, 5, 14, 14, 16),   // foot + body
            Block.box(1, 0, 0, 15, 1, 5));    // keyboard + mouse
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(2, 0, 0, 12, 14, 11),
            Block.box(1, 0, 11, 15, 1, 16));
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(0, 0, 4, 11, 14, 14),
            Block.box(11, 0, 1, 16, 1, 15));
    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(5, 0, 2, 16, 14, 12),
            Block.box(0, 0, 1, 5, 1, 15));

    public FilterScribingTerminalBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    // ── Placement ────────────────────────────────────────────────────────────────

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Screen faces the player who placed it.
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    // ── Interaction ──────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp
                && level.getBlockEntity(pos) instanceof FilterScribingTerminalBlockEntity be) {
            ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
            ExtendedMenus.openAt(sp,
                    new SimpleMenuProvider(
                            (syncId, inv, p) -> new FilterScribingTerminalMenu(syncId, inv, be, access),
                            Component.translatable("block.mobfarmingsupplies.filter_scribing_terminal")), pos);
            if (sp.containerMenu instanceof FilterScribingTerminalMenu) {
                setActive(level, pos, true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FilterScribingTerminalBlockEntity(pos, state);
    }

    // ── In-use tracking ──────────────────────────────────────────────────────────

    /** Called when a player's terminal menu closes; {@code closing} is excluded from the count. */
    static void onMenuClosed(ServerLevel level, BlockPos pos, AbstractContainerMenu closing) {
        if (!hasViewers(level, pos, closing)) setActive(level, pos, false);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) return;
        if (hasViewers(level, pos, null)) {
            level.scheduleTick(pos, this, RECHECK_TICKS);
        } else {
            setActive(level, pos, false);
        }
    }

    private static boolean hasViewers(ServerLevel level, BlockPos pos, AbstractContainerMenu ignore) {
        for (ServerPlayer p : level.players()) {
            if (p.containerMenu != ignore
                    && p.containerMenu instanceof FilterScribingTerminalMenu m
                    && m.getPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private static void setActive(Level level, BlockPos pos, boolean active) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof FilterScribingTerminalBlock block)) return;
        if (state.getValue(ACTIVE) != active) {
            level.setBlock(pos, state.setValue(ACTIVE, active), Block.UPDATE_ALL);
        }
        if (active) level.scheduleTick(pos, block, RECHECK_TICKS);
    }

    // ── Structure rotation / mirror support ──────────────────────────────────────

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
