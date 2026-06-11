package net.bobofraggins.mobfarmingsupplies.mobharvester;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;
import net.minecraft.core.Direction;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

/**
 * Mob Harvester block.
 *
 * <p>When powered by redstone, attacks all {@link net.minecraft.world.entity.LivingEntity}s
 * (excluding players) in a {@value MobHarvesterBlockEntity#KILL_WIDTH}×
 * {@value MobHarvesterBlockEntity#KILL_HEIGHT}×
 * {@value MobHarvesterBlockEntity#KILL_WIDTH} area above the block every
 * {@value MobHarvesterBlockEntity#ATTACK_INTERVAL_TICKS} ticks.
 * Attack damage scales with upgrades placed in the nine upgrade slots.
 *
 * <p>Mobs drop items and XP orbs naturally; pair with an {@link
 * net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlock}
 * to collect them.
 */
public class MobHarvesterBlock extends BaseEntityBlock {

    public static final MapCodec<MobHarvesterBlock> CODEC = simpleCodec(MobHarvesterBlock::new);

    public static final BooleanProperty          POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<Direction> FACING  = BlockStateProperties.HORIZONTAL_FACING;

    public MobHarvesterBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING);
    }

    // ── Shape ────────────────────────────────────────────────────────────────────

    /** Full two-block-tall hitbox (0–16 px wide/deep, 0–32 px tall). */
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 32, 16);

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                  BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ── Drops ────────────────────────────────────────────────────────────────────

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof MobHarvesterBlockEntity harvester) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem) {
                    TagValueOutput beOut = TagValueOutput.createWithContext(
                            ProblemReporter.DISCARDING,
                            params.getLevel().registryAccess());
                    harvester.saveCustomOnly(beOut);
                    BlockItem.setBlockEntityData(drop, harvester.getType(), beOut);
                }
            }
        }
        return drops;
    }

    // ── Placement ────────────────────────────────────────────────────────────────

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        boolean powered = ctx.getLevel().hasNeighborSignal(ctx.getClickedPos());
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(POWERED, powered).setValue(FACING, facing);
    }

    // ── Redstone ─────────────────────────────────────────────────────────────────

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos,
            Block sourceBlock, Orientation orientation, boolean isMoving) {
        if (level.isClientSide()) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        }
    }

    // ── Block entity ──────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MobHarvesterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> beType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(beType, Registration.MOB_HARVESTER_BE_TYPE.get(),
                MobHarvesterBlockEntity::serverTick);
    }

    // ── GUI ───────────────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof MobHarvesterBlockEntity be
                    && player instanceof ServerPlayer sp) {
                MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
