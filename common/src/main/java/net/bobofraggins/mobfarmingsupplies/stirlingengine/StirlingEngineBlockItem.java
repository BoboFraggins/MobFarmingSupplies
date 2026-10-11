package net.bobofraggins.mobfarmingsupplies.stirlingengine;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** A Stirling Engine item; its tooltip gives the tier's output over each heat source. */
public class StirlingEngineBlockItem extends BlockItem {

    private final StirlingEngineTier tier;

    public StirlingEngineBlockItem(StirlingEngineBlock block, Properties properties) {
        super(block, properties);
        this.tier = block.tier();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay lines,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, tooltipAdder, flag);
        int m = tier.multiplier();
        tooltipAdder.accept(Component.translatable("item.mobfarmingsupplies.stirling_engine.tooltip",
                StirlingEngineBlockEntity.LAVA * m, StirlingEngineBlockEntity.MAGMA * m,
                StirlingEngineBlockEntity.CAMPFIRE * m).withStyle(ChatFormatting.GRAY));
    }
}
