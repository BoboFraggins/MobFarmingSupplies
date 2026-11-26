package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 1.20.1 blocks have one {@code use} method; 1.20.5+ splits it into {@code useItemOn} (with the held
 * item, for each hand) and {@code useWithoutItem} (main hand only, when the item passed). The blocks
 * keep both methods and their {@code use} calls {@link #use} to run them the newer way.
 */
public final class BlockInteractions {

    private BlockInteractions() {}

    @FunctionalInterface
    public interface UseItemOn {
        ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                        InteractionHand hand, BlockHitResult hit);
    }

    @FunctionalInterface
    public interface UseWithoutItem {
        InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit);
    }

    public static InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                        BlockHitResult hit, UseItemOn useItemOn, UseWithoutItem useWithoutItem) {
        if (useItemOn != null) {
            ItemInteractionResult result = useItemOn.useItemOn(player.getItemInHand(hand), state, level, pos, player, hand, hit);
            if (result == ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION) return InteractionResult.PASS;
            if (result != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) return result.result();
        }
        if (useWithoutItem != null && hand == InteractionHand.MAIN_HAND) {
            return useWithoutItem.useWithoutItem(state, level, pos, player, hit);
        }
        return InteractionResult.PASS;
    }
}
