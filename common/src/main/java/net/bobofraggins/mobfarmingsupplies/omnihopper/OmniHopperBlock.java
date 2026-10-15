package net.bobofraggins.mobfarmingsupplies.omnihopper;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.TransferSchedule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Omnidirectional Hopper — moves items, fluids, energy and Mekanism chemicals from its INPUT
 * neighbours to its OUTPUT neighbours. The block state mirrors each side's {@link HopperSide},
 * driving which coloured connector the multipart model shows.
 */
public class OmniHopperBlock extends BaseEntityBlock {

    public static final MapCodec<OmniHopperBlock> CODEC = simpleCodec(OmniHopperBlock::new);

    public static final Map<Direction, EnumProperty<HopperSide>> SIDES = new EnumMap<>(Direction.class);
    static {
        for (Direction d : Direction.values()) {
            SIDES.put(d, EnumProperty.create(d.getSerializedName(), HopperSide.class));
        }
    }

    public OmniHopperBlock(BlockBehaviour.Properties props) {
        super(props);
        BlockState state = this.stateDefinition.any();
        for (EnumProperty<HopperSide> p : SIDES.values()) state = state.setValue(p, HopperSide.NONE);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        SIDES.values().forEach(builder::add);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ── Drops — keep configuration and filters on the item ─────────────────────

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof OmniHopperBlockEntity hopper) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem) {
                    CompoundTag beOut = hopper.saveCustomOnly(params.getLevel().registryAccess());
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
        return new OmniHopperBlockEntity(pos, state);
    }

    @Nullable
    /** A neighbour changed: try moving things again straight away (see {@link TransferSchedule}). */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        TransferSchedule.resetAt(level, pos);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> beType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(beType, Registration.OMNI_HOPPER_BE_TYPE.get(), OmniHopperBlockEntity::serverTick);
    }

    // ── Interaction ─────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()
                && level.getBlockEntity(pos) instanceof OmniHopperBlockEntity be
                && player instanceof ServerPlayer sp) {
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    /** Drawn from its block model (1.21.1's {@code BaseEntityBlock} defaults to invisible). */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
