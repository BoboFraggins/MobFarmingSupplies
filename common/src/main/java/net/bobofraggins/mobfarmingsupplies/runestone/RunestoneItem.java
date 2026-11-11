package net.bobofraggins.mobfarmingsupplies.runestone;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** A {@link Runestone}, worn as a charm. */
public class RunestoneItem extends Item {

    private final Runestone runestone;

    public RunestoneItem(Runestone runestone, Properties properties) {
        super(properties);
        this.runestone = runestone;
    }

    public Runestone runestone() {
        return runestone;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.mobfarmingsupplies." + runestone.itemId() + ".tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.mobfarmingsupplies.runestone.worn")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
