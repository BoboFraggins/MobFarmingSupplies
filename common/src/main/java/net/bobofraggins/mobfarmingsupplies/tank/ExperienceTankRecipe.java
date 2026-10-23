package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity;
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
 * Crafts a block from a tank and moves the tank's experience into it (the Experience Pylon, the
 * Incubator). Like {@link TankUpgradeRecipe}, but the tank must be empty or hold an experience
 * fluid (and no more than a pylon holds). Type {@code mobfarmingsupplies:experience_tank} in JSON,
 * shaped.
 */
public class ExperienceTankRecipe extends TankUpgradeRecipe {

    public static final MapCodec<ExperienceTankRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, ExperienceTankRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceTankRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ExperienceTankRecipe::new);

    public ExperienceTankRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                 ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
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
    public ItemStack assemble(CraftingInput input) {
        ItemStack crafted = result.create();
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
    public RecipeSerializer<ExperienceTankRecipe> getSerializer() {
        return Registration.EXPERIENCE_TANK_SERIALIZER.get();
    }
}
