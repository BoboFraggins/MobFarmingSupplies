package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * Crafts a block from a tank and moves the tank's experience into it (the Experience Pylon, the
 * Incubator). Like {@link TankUpgradeRecipe}, but the tank must be empty or hold an experience
 * fluid (and no more than a pylon holds). Type {@code mobfarmingsupplies:experience_tank} in JSON,
 * shaped.
 */
public class ExperienceTankRecipe extends TankUpgradeRecipe {

    public static final MapCodec<ExperienceTankRecipe> MAP_CODEC = ShapedRecipeCodecs.mapCodec(ExperienceTankRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceTankRecipe> STREAM_CODEC =
            ShapedRecipeCodecs.streamCodec(ExperienceTankRecipe::new);

    public ExperienceTankRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                                ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!super.matches(input, level)) return false;
        for (ItemStack ingredient : input.items()) {
            TankContents contents = ingredient.get(Registration.TANK_CONTENTS.get());
            if (contents == null || contents.isEmpty()) continue;
            if (!ExperiencePylonBlockEntity.isExperience(contents.storedFluid())) return false;
            if (contents.amount() > ExperiencePylonBlockEntity.CAPACITY) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack crafted = result.copy();
        for (ItemStack ingredient : input.items()) {
            TankContents contents = ingredient.get(Registration.TANK_CONTENTS.get());
            if (contents != null && !contents.isEmpty()) {
                crafted.set(Registration.TANK_CONTENTS.get(), new TankContents(contents.storedFluid(), contents.amount()));
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
