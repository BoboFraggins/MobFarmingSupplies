package net.bobofraggins.mobfarmingsupplies.fan;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;

/**
 * The Fan block.
 *
 * <p>Placement follows the same convention as {@link net.bobofraggins.mobfarmingsupplies.vectorplate.VectorPlateBlock}:
 * {@code FACING} is stored as the <em>opposite</em> of the player's look direction, so placing
 * while facing south stores {@code FACING=NORTH} and the fan blows northward.
 *
 * <p>When {@code POWERED=true} (block receives a redstone signal), the turbine animates via
 * {@link FanBlockEntityRenderer}.  Entity-pushing and upgrade logic are deferred.
 */
public class FanBlock extends DirectionalBlock implements EntityBlock {

    public static final MapCodec<FanBlock> CODEC = simpleCodec(FanBlock::new);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public FanBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }

    @Override
    public MapCodec<FanBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    // ── Placement ──────────────────────────────────────────────────────────────

    /**
     * For floor/ceiling placement uses the clicked face (UP or DOWN).
     * For wall placement uses the opposite of the player's horizontal look direction.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        boolean powered = ctx.getLevel().hasNeighborSignal(ctx.getClickedPos());
        Direction facing = ctx.getNearestLookingDirection().getOpposite();
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWERED, powered);
    }

    // ── Redstone ───────────────────────────────────────────────────────────────

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   Block sourceBlock, Orientation orientation, boolean isMoving) {
        if (level.isClientSide()) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        }
    }

    // ── Drops ──────────────────────────────────────────────────────────────────

    /**
     * Saves the block entity's upgrade data into the dropped item so upgrades
     * are preserved when the fan is picked up and re-placed.
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof FanBlockEntity fan) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem) {
                    TagValueOutput beOut = TagValueOutput.createWithContext(
                            ProblemReporter.DISCARDING,
                            params.getLevel().registryAccess());
                    fan.saveCustomOnly(beOut);
                    BlockItem.setBlockEntityData(drop, fan.getType(), beOut);
                }
            }
        }
        return drops;
    }

    // ── BlockEntity ────────────────────────────────────────────────────────────

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FanBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != Registration.FAN_BE_TYPE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<FanBlockEntity>) FanBlockEntity::serverTick;
    }

    // ── GUI ────────────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof FanBlockEntity be
                    && player instanceof ServerPlayer sp) {
                MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
