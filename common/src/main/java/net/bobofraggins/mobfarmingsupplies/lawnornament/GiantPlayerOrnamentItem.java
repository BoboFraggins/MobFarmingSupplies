package net.bobofraggins.mobfarmingsupplies.lawnornament;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;

/** The Giant Player Lawn Ornament item: named after its owner once it has one. */
public class GiantPlayerOrnamentItem extends BlockItem {

    public GiantPlayerOrnamentItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResolvableProfile owner = stack.get(DataComponents.PROFILE);
        if (owner != null && owner.name().isPresent()) {
            return Component.translatable(getDescriptionId() + ".owned", owner.name().get());
        }
        return super.getName(stack);
    }
}
