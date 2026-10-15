package net.bobofraggins.mobfarmingsupplies.omnihopper;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.bobofraggins.mobfarmingsupplies.shared.sides.OrientedBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/** Omnidirectional Hopper block item; adds a one-line description tooltip. */
public class OmniHopperBlockItem extends OrientedBlockItem {

    public OmniHopperBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext ctx, List<Component> consumerLines, TooltipFlag flag) {
        Consumer<Component> consumer = consumerLines::add;
        super.appendHoverText(stack, ctx, consumerLines, flag);
        consumer.accept(Component.translatable("item.mobfarmingsupplies.omnidirectional_hopper.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
