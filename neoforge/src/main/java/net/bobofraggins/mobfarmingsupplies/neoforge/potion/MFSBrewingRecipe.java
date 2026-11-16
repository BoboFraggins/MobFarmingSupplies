package net.bobofraggins.mobfarmingsupplies.neoforge.potion;

import net.bobofraggins.mobfarmingsupplies.potion.MFSBrewing;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/** Our brewing stand recipes ({@link MFSBrewing}) as one NeoForge brewing recipe. */
public final class MFSBrewingRecipe implements IBrewingRecipe {

    public static void register(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addRecipe(new MFSBrewingRecipe());
    }

    @Override
    public boolean isInput(ItemStack input) {
        return MFSBrewing.isInput(input);
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return MFSBrewing.isIngredient(ingredient);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        return MFSBrewing.mix(input, ingredient);
    }
}
