package net.bobofraggins.mobfarmingsupplies.biosludge;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Biosludge: slime, spider eye and rotten flesh, crafted together. Its tooltip says what it is. */
public class BiosludgeItem extends Item {

    public BiosludgeItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipAdderLines, TooltipFlag flag) {
        Consumer<Component> tooltipAdder = tooltipAdderLines::add;
        super.appendHoverText(stack, context, tooltipAdderLines, flag);
        tooltipAdder.accept(Component.translatable("item.mobfarmingsupplies.biosludge.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
