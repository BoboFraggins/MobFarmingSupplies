package net.bobofraggins.mobfarmingsupplies.magnet;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The Superconducting Magnet. While it's switched on, anywhere in its owner's inventory or worn as a
 * charm, it collects items and experience within {@link MagnetPull#RANGE} blocks (see
 * {@link MagnetPull}). Using it switches it on or off.
 */
public class SuperconductingMagnetItem extends Item {

    public SuperconductingMagnetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand).copy();
            player.setItemInHand(hand, MagnetPull.toggle(serverPlayer, stack));
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        tooltip.accept(Component.translatable("item.mobfarmingsupplies.superconducting_magnet.tooltip",
                MagnetPull.RANGE).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(MagnetPull.isOn(stack)
                ? "item.mobfarmingsupplies.superconducting_magnet.on"
                : "item.mobfarmingsupplies.superconducting_magnet.off").withStyle(ChatFormatting.YELLOW));
    }
}
