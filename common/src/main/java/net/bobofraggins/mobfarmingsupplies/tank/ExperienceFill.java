package net.bobofraggins.mobfarmingsupplies.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Right-clicking experience into a block that only uses it (the XP Powered Incubator, the XP
 * Powered Anvil): a bottle o' enchanting, a bucket (or other container) of experience, or a
 * bucket-mode tank or syringe holding experience empties into its tank. Nothing takes experience
 * out, so anything else (an empty bucket too) falls through, and the block opens its window.
 */
public final class ExperienceFill {

    private ExperienceFill() {}

    public static InteractionResult useItemOn(TankBlockEntity be, ItemStack stack, Level level, BlockPos pos,
                                              Player player, InteractionHand hand) {
        boolean fills = stack.is(Items.EXPERIENCE_BOTTLE)
                || (ExperienceItemTransfer.handles(stack) && ExperienceItemTransfer.holdsExperience(stack))
                || (!stack.is(Items.BUCKET) && !stack.is(Items.GLASS_BOTTLE) && !ExperienceItemTransfer.handles(stack));
        if (!fills) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (stack.is(Items.EXPERIENCE_BOTTLE)) {
            ItemStack result = TankBottleTransfer.tryTransfer(be, stack);
            if (result == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        if (ExperienceItemTransfer.handles(stack)) {
            ItemStack result = ExperienceItemTransfer.transfer(be, stack, false);
            if (result == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        // Buckets and other fluid containers. The tank only takes experience, and its handler
        // never gives any back, so only a container of experience does anything here.
        boolean success = TankBlockPlatform.handleFluidItemInteraction(player, hand, level, pos, false);
        return success ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
