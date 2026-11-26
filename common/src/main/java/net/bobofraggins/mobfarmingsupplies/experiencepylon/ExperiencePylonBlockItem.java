package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
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
            @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level context,
            List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        TankContents contents = ItemComponents.getOrDefault(stack, Registration.TANK_CONTENTS.get(), TankContents.EMPTY);
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
