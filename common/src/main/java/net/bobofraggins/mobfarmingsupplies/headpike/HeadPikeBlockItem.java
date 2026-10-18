package net.bobofraggins.mobfarmingsupplies.headpike;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** Head Pike block item; adds a one-line description tooltip. */
public class HeadPikeBlockItem extends BlockItem {

    public HeadPikeBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, lines, flag);
        lines.add(Component.translatable("item.mobfarmingsupplies.head_pike.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
