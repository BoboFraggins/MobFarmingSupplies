package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** The Experience Pylon item, which keeps the pylon's experience (as {@code tank_contents}) when broken. */
public class ExperiencePylonBlockItem extends BlockItem {

    public ExperiencePylonBlockItem(Block block, Item.Properties props) {
        super(block, props);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        TankContents contents = stack.getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY);
        if (!contents.isEmpty()) {
            tooltip.add(Component.translatable(
                            "item.mobfarmingsupplies.tank.tooltip",
                            contents.amount(),
                            ExperiencePylonBlockEntity.CAPACITY,
                            contents.storedFluid().getName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
