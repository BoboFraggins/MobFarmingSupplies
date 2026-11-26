package net.bobofraggins.mobfarmingsupplies.crushing;

import net.bobofraggins.mobfarmingsupplies.shared.recipe.IdentifiedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSStreamCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ByteBufCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.bobofraggins.mobfarmingsupplies.shared.compat.SingleRecipeInput;
import net.minecraft.world.level.Level;

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
public class AnvilCrushingRecipe implements Recipe<SingleRecipeInput>, IdentifiedRecipe {

    private ResourceLocation id = new ResourceLocation("mobfarmingsupplies", "unnamed");

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public static final MapCodec<AnvilCrushingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(AnvilCrushingRecipe::getGroup),
            MFSCodecs.INGREDIENT_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
            MFSCodecs.RESULT_STACK.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, AnvilCrushingRecipe::new));

    public static final StreamCodec<FriendlyByteBuf, AnvilCrushingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AnvilCrushingRecipe::getGroup,
            MFSStreamCodecs.INGREDIENT, r -> r.ingredient,
            MFSStreamCodecs.ITEM_STACK, r -> r.result,
            AnvilCrushingRecipe::new);

    private final String group;
    private final Ingredient ingredient;
    private final ItemStack result;

    public AnvilCrushingRecipe(String group, Ingredient input, ItemStack result) {
        this.group = group;
        this.ingredient = input;
        this.result = result;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.ANVIL_CRUSHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Registration.ANVIL_CRUSHING_TYPE.get();
    }

    /** What the recipe crushes. */
    public Ingredient input() {
        return ingredient;
    }

    /** Number of result items produced per input item. */
    public int resultCountPerItem() {
        return result.getCount();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    /** Not a crafting-table recipe — keep it out of the recipe book. */
    @Override
    public boolean isSpecial() {
        return true;
    }
}
