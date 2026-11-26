package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.world.item.ItemStack;

/** Stand-in for 1.20.5+'s {@code DataComponentInput}: the components of the item a block was placed from. */
public record DataComponentInput(ItemStack stack) {

    public <T> T get(DataComponentType<T> type) {
        return ItemComponents.get(stack, type);
    }

    public <T> T getOrDefault(DataComponentType<T> type, T fallback) {
        return ItemComponents.getOrDefault(stack, type, fallback);
    }
}
