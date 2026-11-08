package net.bobofraggins.mobfarmingsupplies.upgradestation;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * {@code mobfarmingsupplies:upgrading}: what the Upgrade Station makes, instantly, like a crafting
 * table. Taking the result uses up one {@code item} (the top slot) and one {@code input}.
 * <pre>{@code
 * { "type": "mobfarmingsupplies:upgrading",
 *   "item": "minecraft:iron_sword",
 *   "input": "mobfarmingsupplies:blank_upgrade",
 *   "result": { "id": "mobfarmingsupplies:harvester_upgrade_sharpness" } }
 * }</pre>
 */
public class UpgradingRecipe implements Recipe<UpgradingInput> {

    public static final MapCodec<UpgradingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("item").forGetter(UpgradingRecipe::item),
            Ingredient.CODEC.fieldOf("input").forGetter(UpgradingRecipe::input),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(UpgradingRecipe::result)
    ).apply(i, UpgradingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, UpgradingRecipe::item,
            Ingredient.CONTENTS_STREAM_CODEC, UpgradingRecipe::input,
            ItemStackTemplate.STREAM_CODEC, UpgradingRecipe::result,
            UpgradingRecipe::new);

    private final Ingredient item;
    private final Ingredient input;
    private final ItemStackTemplate result;

    public UpgradingRecipe(Ingredient item, Ingredient input, ItemStackTemplate result) {
        this.item = item;
        this.input = input;
        this.result = result;
    }

    public Ingredient item() {
        return item;
    }

    public Ingredient input() {
        return input;
    }

    public ItemStackTemplate result() {
        return result;
    }

    @Override
    public boolean matches(UpgradingInput in, Level level) {
        return item.test(in.item()) && input.test(in.input());
    }

    @Override
    public ItemStack assemble(UpgradingInput in) {
        return result.create();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<UpgradingRecipe> getSerializer() {
        return Registration.UPGRADING_SERIALIZER.get();
    }

    @Override
    public RecipeType<UpgradingRecipe> getType() {
        return Registration.UPGRADING_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    /** Not a crafting-table recipe; this only keeps the recipe book's bookkeeping happy. */
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
