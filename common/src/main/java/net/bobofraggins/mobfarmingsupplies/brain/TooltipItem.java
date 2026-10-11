package net.bobofraggins.mobfarmingsupplies.brain;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** An item with one line of flavour text, from its {@code .tooltip} lang key. */
public class TooltipItem extends Item {

    public TooltipItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay lines,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
}
