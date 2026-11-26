package net.bobofraggins.mobfarmingsupplies.shared.recipe;

import net.minecraft.resources.ResourceLocation;

/** A recipe whose id is set by {@link SimpleRecipeSerializer} after it's decoded (1.20.1 recipes know their id). */
public interface IdentifiedRecipe {
    void setId(ResourceLocation id);
}
