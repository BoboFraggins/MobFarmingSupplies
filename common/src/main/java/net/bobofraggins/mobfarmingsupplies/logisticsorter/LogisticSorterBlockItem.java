package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/** Logistic Sorter block item; adds a one-line description tooltip. */
public class LogisticSorterBlockItem extends BlockItem {

    public LogisticSorterBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display,
            Consumer<Component> consumer, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, display, consumer, flag);
        consumer.accept(Component.translatable("item.mobfarmingsupplies.logistic_sorter.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
