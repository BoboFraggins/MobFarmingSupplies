package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.bobofraggins.mobfarmingsupplies.tank.TankUpgradeRecipe;
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
 * Crafts an Experience Pylon from a tank, moving the tank's experience into it. Like
 * {@link TankUpgradeRecipe}, but the tank must be empty or hold an experience fluid (and no more
 * than the pylon holds). Type {@code mobfarmingsupplies:experience_pylon} in JSON, shaped.
 */
public class ExperiencePylonRecipe extends TankUpgradeRecipe {

    public static final MapCodec<ExperiencePylonRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, ExperiencePylonRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExperiencePylonRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ExperiencePylonRecipe::new);

    public ExperiencePylonRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
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
        ItemStack pylon = result.create();
        for (ItemStack ingredient : input.items()) {
            TankContents contents = ingredient.get(Registration.TANK_CONTENTS.get());
            if (contents != null && !contents.isEmpty()) {
                pylon.set(Registration.TANK_CONTENTS.get(), new TankContents(contents.storedFluid(), contents.amount()));
                break;
            }
        }
        return pylon;
    }

    @Override
    public RecipeSerializer<ExperiencePylonRecipe> getSerializer() {
        return Registration.EXPERIENCE_PYLON_SERIALIZER.get();
    }
}
