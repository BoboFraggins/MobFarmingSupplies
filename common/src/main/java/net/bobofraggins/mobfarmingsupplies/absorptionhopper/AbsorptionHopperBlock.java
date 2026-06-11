package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

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
import net.minecraft.world.InteractionResult;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

/**
 * Absorption Hopper — vacuums nearby item entities and XP orbs into internal storage,
 * then pushes items and XP fluid out to adjacent inventories and tanks.
 */
public class AbsorptionHopperBlock extends BaseEntityBlock {

    public static final MapCodec<AbsorptionHopperBlock> CODEC = simpleCodec(AbsorptionHopperBlock::new);

    // ── Push-side block state properties ────────────────────────────────────────
    // Mirror the pushSides bitmask in the block entity so the multipart model
    // can show/hide each connection pipe independently.

    public static final BooleanProperty PUSH_UP    = BooleanProperty.create("push_up");
    public static final BooleanProperty PUSH_DOWN  = BooleanProperty.create("push_down");
    public static final BooleanProperty PUSH_NORTH = BooleanProperty.create("push_north");
    public static final BooleanProperty PUSH_SOUTH = BooleanProperty.create("push_south");
    public static final BooleanProperty PUSH_EAST  = BooleanProperty.create("push_east");
    public static final BooleanProperty PUSH_WEST  = BooleanProperty.create("push_west");

    public AbsorptionHopperBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(this.stateDefinition.any()
                .setValue(PUSH_UP,    false)
                .setValue(PUSH_DOWN,  false)
                .setValue(PUSH_NORTH, false)
                .setValue(PUSH_SOUTH, false)
                .setValue(PUSH_EAST,  false)
                .setValue(PUSH_WEST,  false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PUSH_UP, PUSH_DOWN, PUSH_NORTH, PUSH_SOUTH, PUSH_EAST, PUSH_WEST);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ── Drops ───────────────────────────────────────────────────────────────────

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof AbsorptionHopperBlockEntity hopper) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem) {
                    TagValueOutput beOut = TagValueOutput.createWithContext(
                            ProblemReporter.DISCARDING,
                            params.getLevel().registryAccess());
                    hopper.saveCustomOnly(beOut);
                    BlockItem.setBlockEntityData(drop, hopper.getType(), beOut);
                }
            }
        }
        return drops;
    }

    // ── Block entity ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbsorptionHopperBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> beType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(beType, Registration.ABSORPTION_HOPPER_BE_TYPE.get(),
                AbsorptionHopperBlockEntity::serverTick);
    }

    // ── Interaction ─────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof AbsorptionHopperBlockEntity be
                    && player instanceof ServerPlayer sp) {
                MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
