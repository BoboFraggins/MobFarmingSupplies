package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

/**
 * A shapeless recipe that uses up a tank, which must be empty so no fluid is lost (the Color
 * Workstation). Type {@code mobfarmingsupplies:empty_tank} in JSON, written like a shapeless
 * recipe. It can't simply extend {@link net.minecraft.world.item.crafting.ShapelessRecipe}: that
 * class pins its serializer's type, so a subclass couldn't name its own.
 */
public class EmptyTankRecipe extends NormalCraftingRecipe {

    public static final MapCodec<EmptyTankRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(r -> r.ingredients)
    ).apply(i, EmptyTankRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EmptyTankRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.ingredients,
            EmptyTankRecipe::new);

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;

    public EmptyTankRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                           ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo);
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != ingredients.size()) return false;
        for (ItemStack ingredient : input.items()) {
            TankContents contents = ingredient.get(Registration.TANK_CONTENTS.get());
            if (contents != null && !contents.isEmpty()) return false;
        }
        return input.stackedContents().canCraft(this, null);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return result.create();
    }

    @Override
    public RecipeSerializer<EmptyTankRecipe> getSerializer() {
        return Registration.EMPTY_TANK_SERIALIZER.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(ingredients);
    }

    /** Shown in the recipe book exactly like an ordinary shapeless recipe. */
    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                ingredients.stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemStackSlotDisplay(result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }
}
