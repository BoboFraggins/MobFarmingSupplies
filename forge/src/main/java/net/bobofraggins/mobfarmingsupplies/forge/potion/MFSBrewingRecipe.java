package net.bobofraggins.mobfarmingsupplies.forge.potion;

import net.bobofraggins.mobfarmingsupplies.potion.MFSBrewing;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Our brewing stand recipes ({@link MFSBrewing}) as one NeoForge brewing recipe. */
public final class MFSBrewingRecipe implements IBrewingRecipe {

    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BrewingRecipeRegistry.addRecipe(new MFSBrewingRecipe()));
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
