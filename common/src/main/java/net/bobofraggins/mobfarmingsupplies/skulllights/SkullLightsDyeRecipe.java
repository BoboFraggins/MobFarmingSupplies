package net.bobofraggins.mobfarmingsupplies.skulllights;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
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
public class SkullLightsDyeRecipe extends CustomRecipe {

    public static final MapCodec<SkullLightsDyeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                    .forGetter(SkullLightsDyeRecipe::category),
            Ingredient.CODEC_NONEMPTY.fieldOf("lights").forGetter(r -> r.lights),
            Ingredient.CODEC_NONEMPTY.fieldOf("dye").forGetter(r -> r.dye)
    ).apply(i, SkullLightsDyeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkullLightsDyeRecipe> STREAM_CODEC = StreamCodec.composite(
            CraftingBookCategory.STREAM_CODEC, SkullLightsDyeRecipe::category,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.lights,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.dye,
            SkullLightsDyeRecipe::new);

    private final Ingredient lights;
    private final Ingredient dye;

    public SkullLightsDyeRecipe(CraftingBookCategory category, Ingredient lights, Ingredient dye) {
        super(category);
        this.lights = lights;
        this.dye = dye;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return find(input, true) != null && find(input, false) != null && input.ingredientCount() == 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack string = find(input, true);
        ItemStack dyeStack = find(input, false);
        if (string == null || dyeStack == null) return ItemStack.EMPTY;
        SkullLights old = string.getOrDefault(Registration.SKULL_LIGHTS.get(), SkullLights.defaultLights());
        ItemStack recoloured = string.copyWithCount(1);
        DyeColor color = dyeStack.getItem() instanceof DyeItem dyeItem ? dyeItem.getDyeColor() : DyeColor.WHITE;
        recoloured.set(Registration.SKULL_LIGHTS.get(), new SkullLights(old.skulls(), color));
        return recoloured;
    }

    /** The one string (not half hung), or the one dye, in the grid; null if there isn't exactly one. */
    private ItemStack find(CraftingInput input, boolean string) {
        ItemStack found = null;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            boolean isString = lights.test(stack) && !stack.has(Registration.SKULL_LIGHTS_START.get());
            boolean match = string ? isString : dye.test(stack) && stack.getItem() instanceof DyeItem;
            if (!match) continue;
            if (found != null) return null;
            found = stack;
        }
        return found;
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
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(Registration.STRING_OF_SKULL_LIGHTS.get());
    }

    @Override
    public RecipeSerializer<SkullLightsDyeRecipe> getSerializer() {
        return Registration.SKULL_LIGHTS_DYE_SERIALIZER.get();
    }
}
