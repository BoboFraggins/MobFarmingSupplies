package net.bobofraggins.mobfarmingsupplies.lawnornament;

import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import net.bobofraggins.mobfarmingsupplies.shared.compat.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.block.Block;

/** The Giant Player Lawn Ornament item: named after its owner once it has one. */
public class GiantPlayerOrnamentItem extends BlockItem {

    public GiantPlayerOrnamentItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        GameProfile owner = ItemComponents.get(stack, DataComponents.PROFILE);
        if (owner != null && owner.getName() != null && !owner.getName().isEmpty()) {
            return Component.translatable(getDescriptionId() + ".owned", owner.getName());
        }
        return super.getName(stack);
    }
}
