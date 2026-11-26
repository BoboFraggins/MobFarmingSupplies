package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.world.item.ItemStack;

/** Stand-in for 1.20.5+'s one-item recipe input (furnace, anvil crushing). */
public record SingleRecipeInput(ItemStack item) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        if (index != 0) throw new IllegalArgumentException("No item for index " + index);
        return item;
    }

    @Override
    public int size() {
        return 1;
    }
}
