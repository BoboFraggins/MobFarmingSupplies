package net.bobofraggins.mobfarmingsupplies.itemfilter;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * An Item Filter scribed at the Filter Scribing Terminal. Its criterion lives in the
 * {@link Registration#ITEM_FILTER_DATA} component, and its name is derived from it,
 * e.g. "Item Filter (is a sword)" — computed on display rather than stored, so it follows
 * the player's language and an anvil rename still takes precedence.
 */
public class ItemFilterItem extends Item {

    public ItemFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemFilterData data = stack.get(Registration.ITEM_FILTER_DATA.get());
        if (data == null) return super.getName(stack);
        return Component.translatable("item.mobfarmingsupplies.item_filter.configured", data.description());
    }
}
