package net.bobofraggins.mobfarmingsupplies.tank;

import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * Crafts a block from a tank and moves the tank's experience into it (the Experience Pylon, the
 * Incubator). Like {@link TankUpgradeRecipe}, but the tank must be empty or hold an experience
 * fluid (and no more than a pylon holds). Type {@code mobfarmingsupplies:experience_tank} in JSON,
 * shaped.
 */
public class ExperienceTankRecipe extends TankUpgradeRecipe {

    public static final RecipeSerializer<ExperienceTankRecipe> SERIALIZER = ShapedRecipeCodecs.serializer(ExperienceTankRecipe::new);

    public ExperienceTankRecipe(ResourceLocation id, String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                                ItemStack result, boolean showNotification) {
        super(id, group, category, pattern, result, showNotification);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        if (!super.matches(input, level)) return false;
        for (ItemStack ingredient : input.getItems()) {
            TankContents contents = ItemComponents.get(ingredient, Registration.TANK_CONTENTS.get());
            if (contents == null || contents.isEmpty()) continue;
            if (!ExperiencePylonBlockEntity.isExperience(contents.storedFluid())) return false;
            if (contents.amount() > ExperiencePylonBlockEntity.CAPACITY) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registries) {
        ItemStack crafted = result.copy();
        for (ItemStack ingredient : input.getItems()) {
            TankContents contents = ItemComponents.get(ingredient, Registration.TANK_CONTENTS.get());
            if (contents != null && !contents.isEmpty()) {
                ItemComponents.set(crafted, Registration.TANK_CONTENTS.get(), new TankContents(contents.storedFluid(), contents.amount()));
                break;
            }
        }
        return crafted;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.EXPERIENCE_TANK_SERIALIZER.get();
    }
}
