package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeads;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightsRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * The String of Skull Lights recipe in JEI's crafting page with every Mob Head in its skull slots.
 * The skull tag only gives JEI a Mob Head with no mob, which no real head matches (JEI tells Mob
 * Heads apart by their mob), so the recipe's + button didn't see the player's heads.
 */
public class SkullLightsCraftingExtension implements ICraftingCategoryExtension<SkullLightsRecipe> {

    @Override
    public void setRecipe(RecipeHolder<SkullLightsRecipe> holder, IRecipeLayoutBuilder builder,
                          ICraftingGridHelper grid, IFocusGroup focuses) {
        SkullLightsRecipe recipe = holder.value();
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) inputs.add(stacks(ingredient));
        grid.createAndSetInputs(builder, inputs, recipe.getWidth(), recipe.getHeight());
        var level = Minecraft.getInstance().level;
        grid.createAndSetOutputs(builder, List.of(level == null ? recipe.result().copy()
                : recipe.getResultItem(level.registryAccess())));
    }

    /** What a slot shows: the ingredient's items, a skull slot's Mob Head as one per mob. */
    private static List<ItemStack> stacks(Ingredient ingredient) {
        List<ItemStack> out = new ArrayList<>();
        boolean heads = false;
        for (ItemStack stack : ingredient.getItems()) {
            if (stack.is(Registration.MOB_HEAD_ITEM.get())) heads = true;
            else out.add(stack);
        }
        if (heads) {
            for (ItemStack head : MobHeads.allHeads()) {
                if (head.is(Registration.MOB_HEAD_ITEM.get())) out.add(head);
            }
        }
        return out;
    }

    @Override
    public int getWidth(RecipeHolder<SkullLightsRecipe> holder) {
        return holder.value().getWidth();
    }

    @Override
    public int getHeight(RecipeHolder<SkullLightsRecipe> holder) {
        return holder.value().getHeight();
    }
}
