package net.bobofraggins.mobfarmingsupplies.blockbreaker;

import dev.architectury.registry.menu.MenuRegistry;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlock;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Block Breaker — breaks the blocks at its INPUT sides and pushes the drops out of its OUTPUT
 * sides (see {@link BlockBreakerBlockEntity}). Like the Omnidirectional Hopper, each side is
 * unattached, an input or an output ({@link HopperSide}), and the block state's sides choose which
 * coloured connections the multipart model shows. An iron pickaxe floats in its glass core.
 */
public class BlockBreakerBlock extends BaseEntityBlock {


    public BlockBreakerBlock(BlockBehaviour.Properties props) {
        super(props);
        BlockState state = this.stateDefinition.any();
        for (EnumProperty<HopperSide> p : OmniHopperBlock.SIDES.values()) state = state.setValue(p, HopperSide.NONE);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        OmniHopperBlock.SIDES.values().forEach(builder::add);
    }


    /** Keeps its sides, Silk Touch and filters on the item, as the Omnidirectional Hopper does. */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof BlockBreakerBlockEntity breaker) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem) {
                    TagValueOutput beOut = TagValueOutput.createWithContext(
                            ProblemReporter.DISCARDING, params.getLevel().registryAccess());
                    breaker.saveCustomOnly(beOut);
                    BlockItem.setBlockEntityData(drop, breaker.getType(), beOut);
                }
            }
        }
        return drops;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockBreakerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> beType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(beType, Registration.BLOCK_BREAKER_BE_TYPE.get(), BlockBreakerBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()
                && level.getBlockEntity(pos) instanceof BlockBreakerBlockEntity be
                && player instanceof ServerPlayer sp) {
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }
}
