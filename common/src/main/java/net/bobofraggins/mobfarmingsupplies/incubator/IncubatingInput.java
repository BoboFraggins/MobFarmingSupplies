package net.bobofraggins.mobfarmingsupplies.incubator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * What an incubator row offers a recipe: the shared sample, the row's input, and where the
 * incubator is (a biome DNA pack's mobs depend on it).
 */
public record IncubatingInput(ItemStack sample, ItemStack input, BlockPos pos) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> sample;
            case 1 -> input;
            default -> throw new IllegalArgumentException("No item " + index + " in an incubating input");
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
