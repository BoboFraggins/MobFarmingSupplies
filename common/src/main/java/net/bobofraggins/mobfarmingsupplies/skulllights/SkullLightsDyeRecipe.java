package net.bobofraggins.mobfarmingsupplies.skulllights;

import net.bobofraggins.mobfarmingsupplies.shared.recipe.IdentifiedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSStreamCodecs;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.SimpleRecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Recolours a String of Skull Lights: the string and a dye, anywhere in the grid, give the same
 * string (same skulls) glowing the dye's colour. A string that is half hung can't be recoloured.
 *
 * <p>Type {@code mobfarmingsupplies:skull_lights_dye} in JSON, with {@code lights} and {@code dye}
 * ingredients.
 */
public class SkullLightsDyeRecipe implements CraftingRecipe, IdentifiedRecipe {

    private ResourceLocation id = new ResourceLocation("mobfarmingsupplies", "unnamed");

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public static final MapCodec<SkullLightsDyeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                    .forGetter(SkullLightsDyeRecipe::category),
            MFSCodecs.INGREDIENT_NONEMPTY.fieldOf("lights").forGetter(r -> r.lights),
            MFSCodecs.INGREDIENT_NONEMPTY.fieldOf("dye").forGetter(r -> r.dye)
    ).apply(i, SkullLightsDyeRecipe::new));

    public static final StreamCodec<FriendlyByteBuf, SkullLightsDyeRecipe> STREAM_CODEC = StreamCodec.composite(
            MFSStreamCodecs.CRAFTING_BOOK_CATEGORY, SkullLightsDyeRecipe::category,
            MFSStreamCodecs.INGREDIENT, r -> r.lights,
            MFSStreamCodecs.INGREDIENT, r -> r.dye,
            SkullLightsDyeRecipe::new);

    public static final RecipeSerializer<SkullLightsDyeRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient lights;
    private final Ingredient dye;

    private final CraftingBookCategory category;

    public SkullLightsDyeRecipe(CraftingBookCategory category, Ingredient lights, Ingredient dye) {
        this.category = category;
        this.lights = lights;
        this.dye = dye;
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        return find(input, true) != null && find(input, false) != null && input.getItems().stream().filter(st -> !st.isEmpty()).count() == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registries) {
        ItemStack string = find(input, true);
        ItemStack dyeStack = find(input, false);
        if (string == null || dyeStack == null) return ItemStack.EMPTY;
        SkullLights old = ItemComponents.getOrDefault(string, Registration.SKULL_LIGHTS.get(), SkullLights.defaultLights());
        ItemStack recoloured = string.copyWithCount(1);
        DyeColor color = dyeStack.getItem() instanceof DyeItem dyeItem ? dyeItem.getDyeColor() : DyeColor.WHITE;
        ItemComponents.set(recoloured, Registration.SKULL_LIGHTS.get(), new SkullLights(old.skulls(), color));
        return recoloured;
    }

    /** The one string (not half hung), or the one dye, in the grid; null if there isn't exactly one. */
    private ItemStack find(CraftingContainer input, boolean string) {
        ItemStack found = null;
        for (ItemStack stack : input.getItems()) {
            if (stack.isEmpty()) continue;
            boolean isString = lights.test(stack) && !ItemComponents.has(stack, Registration.SKULL_LIGHTS_START.get());
            boolean match = string ? isString : dye.test(stack) && stack.getItem() instanceof DyeItem;
            if (!match) continue;
            if (found != null) return null;
            found = stack;
        }
        return found;
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, lights, dye);
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registries) {
        return new ItemStack(Registration.STRING_OF_SKULL_LIGHTS.get());
    }

    @Override
    public RecipeSerializer<SkullLightsDyeRecipe> getSerializer() {
        return Registration.SKULL_LIGHTS_DYE_SERIALIZER.get();
    }
}
