package net.bobofraggins.mobfarmingsupplies.crushing;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;

/**
 * {@code mobfarmingsupplies:anvil_crushing} — item entities lying where a falling anvil lands
 * are turned into the result, per item (a stack of 64 inputs gives 64 × the result count).
 * Same JSON shape as stonecutting:
 * <pre>{@code
 * { "type": "mobfarmingsupplies:anvil_crushing",
 *   "ingredient": "minecraft:quartz",
 *   "result": { "id": "mobfarmingsupplies:silicon_clump" } }
 * }</pre>
 * Performed by {@link AnvilCrushing}.
 */
public class AnvilCrushingRecipe extends SingleItemRecipe {

    public static final MapCodec<AnvilCrushingRecipe> MAP_CODEC = simpleMapCodec(AnvilCrushingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, AnvilCrushingRecipe> STREAM_CODEC =
            simpleStreamCodec(AnvilCrushingRecipe::new);

    public AnvilCrushingRecipe(CommonInfo commonInfo, Ingredient input, ItemStackTemplate result) {
        super(commonInfo, input, result);
    }

    /** Number of result items produced per input item. */
    public int resultCountPerItem() {
        return result().count();
    }

    @Override
    public RecipeType<AnvilCrushingRecipe> getType() {
        return Registration.ANVIL_CRUSHING_TYPE.get();
    }

    @Override
    public RecipeSerializer<AnvilCrushingRecipe> getSerializer() {
        return Registration.ANVIL_CRUSHING_SERIALIZER.get();
    }

    @Override
    public String group() {
        return "";
    }

    /** Not a crafting-table recipe — keep it out of the recipe book. */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
