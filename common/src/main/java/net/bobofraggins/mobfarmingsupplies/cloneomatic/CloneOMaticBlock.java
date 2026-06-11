package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.InteractionResult;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

/**
 * Clone-O-Matic block.
 *
 * <p>Spawns mobs from its DNA slots when continuously powered by redstone.
 * Spawn attempts are gated by {@link net.bobofraggins.mobfarmingsupplies.MGRServerConfig#CLONE_O_MATIC_SPAWN_INTERVAL}.
 * The block has a single block-state property, {@link #POWERED}, that mirrors the
 * incoming redstone signal; actual spawn logic runs only when {@code POWERED=true}.
 *
 * <p>The block renders as a simple cube (no facing), using {@code cube_all} with the
 * {@code clone_o_matic_texture} texture.  A {@link CloneOMaticBlockEntityRenderer}
 * overlays a cycling mob entity display on top.
 */
public class CloneOMaticBlock extends BaseEntityBlock {

    public static final MapCodec<CloneOMaticBlock> CODEC = simpleCodec(CloneOMaticBlock::new);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public CloneOMaticBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    // ── Placement ───────────────────────────────────────────────────────────────

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        boolean powered = ctx.getLevel().hasNeighborSignal(ctx.getClickedPos());
        return defaultBlockState().setValue(POWERED, powered);
    }

    // ── Redstone ────────────────────────────────────────────────────────────────

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos,
            Block sourceBlock, Orientation orientation, boolean isMoving) {
        if (level.isClientSide()) return;
        boolean powered = level.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);
        if (powered != wasPowered) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
            if (powered && level.getBlockEntity(pos) instanceof CloneOMaticBlockEntity be) {
                be.onRisingEdge();
            }
        }
    }

    // ── Block entity ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CloneOMaticBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> beType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(beType, Registration.CLONE_O_MATIC_BE_TYPE.get(),
                CloneOMaticBlockEntity::serverTick);
    }

    // ── GUI ─────────────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof CloneOMaticBlockEntity be
                    && player instanceof ServerPlayer sp) {
                MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
