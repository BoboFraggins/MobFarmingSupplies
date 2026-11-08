package net.bobofraggins.mobfarmingsupplies.upgradestation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** What an Upgrade Station offers a recipe: the item in its top slot, and its input. */
public record UpgradingInput(ItemStack item, ItemStack input) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> item;
            case 1 -> input;
            default -> throw new IllegalArgumentException("No item " + index + " in an upgrading input");
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
