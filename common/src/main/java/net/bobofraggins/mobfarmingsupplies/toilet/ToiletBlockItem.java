package net.bobofraggins.mobfarmingsupplies.toilet;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/** Toilet block item; adds a one-line description tooltip. */
public class ToiletBlockItem extends BlockItem {

    public ToiletBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display,
            Consumer<Component> consumer, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, display, consumer, flag);
        consumer.accept(Component.translatable("item.mobfarmingsupplies.toilet.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
