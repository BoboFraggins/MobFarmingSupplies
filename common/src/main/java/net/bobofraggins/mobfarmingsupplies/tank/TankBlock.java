package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TankBlock extends BaseEntityBlock {

    public TankBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != Registration.TANK_BE_TYPE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<TankBlockEntity>) TankBlockEntity::serverTick;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TankBlockEntity be
                && player instanceof ServerPlayer sp) {
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        // Bottles (empty, water, or XP) — shared with the Tank UI's transfer slot
        if (stack.is(Items.GLASS_BOTTLE) || stack.is(Items.POTION) || stack.is(Items.EXPERIENCE_BOTTLE)) {
            if (!(level.getBlockEntity(pos) instanceof TankBlockEntity be))
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            ItemStack result = TankBottleTransfer.tryTransfer(be, stack);
            if (result == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
            level.playSound(
                    null, pos,
                    stack.is(Items.GLASS_BOTTLE) ? SoundEvents.BOTTLE_FILL : SoundEvents.BOTTLE_EMPTY,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // Bucket / modded fluid container — delegate to platform
        boolean filling;
        if (stack.is(Items.BUCKET)) {
            filling = true;
        } else if (stack.getItem() instanceof TankBlockItem) {
            TankContents tankContents = stack.getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY);
            filling = tankContents.amount() == 0;
        } else {
            filling = false;
        }

        boolean success = TankBlockPlatform.handleFluidItemInteraction(player, hand, level, pos, filling);
        return success ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
