package net.bobofraggins.mobfarmingsupplies.shearing;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
 * Cutting something with a tool in the crafting grid: the {@code tool} (Shears by default) and one
 * {@code ingredient}, anywhere in the grid, make the result. The tool isn't used up; it comes back
 * worn by one point of durability, or breaks if that was its last.
 *
 * <pre>{"type": "mobfarmingsupplies:shearing", "ingredient": "#c:ingots/tritanium_alloy",
 *  "result": {"id": "mobfarmingsupplies:tritanium_alloy_plate", "count": 5}}</pre>
 */
public class ShearingRecipe extends NormalCraftingRecipe {

    private static final Ingredient SHEARS = Ingredient.of(Items.SHEARS);

    public static final MapCodec<ShearingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            Ingredient.CODEC.optionalFieldOf("tool", SHEARS).forGetter(r -> r.tool),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, ShearingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShearingRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.tool,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ShearingRecipe::new);

    private final Ingredient tool;
    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    public ShearingRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                          Ingredient tool, Ingredient ingredient, ItemStackTemplate result) {
        super(commonInfo, bookInfo);
        this.tool = tool;
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 2) return false;
        boolean hasTool = false;
        boolean hasIngredient = false;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (!hasTool && tool.test(stack)) hasTool = true;
            else if (!hasIngredient && ingredient.test(stack)) hasIngredient = true;
            else return false;
        }
        return hasTool && hasIngredient;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return result.create();
    }

    /** The tool stays in the grid, one point more worn; anything else is left as normal. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = CraftingRecipe.defaultCraftingReminder(input);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (!tool.test(stack)) continue;
            ItemStack worn = stack.copyWithCount(1);
            if (worn.isDamageableItem()) {
                worn.setDamageValue(worn.getDamageValue() + 1);
                if (worn.getDamageValue() >= worn.getMaxDamage()) worn = ItemStack.EMPTY;
            }
            remaining.set(slot, worn);
            break;
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<ShearingRecipe> getSerializer() {
        return Registration.SHEARING_SERIALIZER.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(List.of(tool, ingredient));
    }

    /** Shown in the recipe book and JEI as an ordinary shapeless recipe. */
    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                List.of(tool.display(), ingredient.display()),
                new SlotDisplay.ItemStackSlotDisplay(result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }
}
