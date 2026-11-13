package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * A shaped recipe that uses up a tank, which must be empty so no fluid is lost (the Color
 * Workstation). Type {@code mobfarmingsupplies:empty_tank} in JSON, written like a shaped recipe.
 */
public class EmptyTankRecipe extends TankUpgradeRecipe {

    public static final MapCodec<EmptyTankRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, EmptyTankRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EmptyTankRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            EmptyTankRecipe::new);

    public EmptyTankRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                           ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!super.matches(input, level)) return false;
        for (ItemStack ingredient : input.items()) {
            TankContents contents = ingredient.get(Registration.TANK_CONTENTS.get());
            if (contents != null && !contents.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return result.create();
    }

    @Override
    public RecipeSerializer<EmptyTankRecipe> getSerializer() {
        return Registration.EMPTY_TANK_SERIALIZER.get();
    }
}
