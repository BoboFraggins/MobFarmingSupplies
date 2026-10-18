package net.bobofraggins.mobfarmingsupplies.headpike;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** Head Pike block item; adds a one-line description tooltip. */
public class HeadPikeBlockItem extends BlockItem {

    public HeadPikeBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display,
            Consumer<Component> consumer, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, display, consumer, flag);
        consumer.accept(Component.translatable("item.mobfarmingsupplies.head_pike.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
