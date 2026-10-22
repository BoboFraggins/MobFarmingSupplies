package net.bobofraggins.mobfarmingsupplies.skulllights;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
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
 * Recolours a String of Skull Lights: the string and a dye, anywhere in the grid, give the same
 * string (same skulls) glowing the dye's colour. A string that is half hung can't be recoloured.
 *
 * <p>Type {@code mobfarmingsupplies:skull_lights_dye} in JSON, with {@code lights} and {@code dye}
 * ingredients.
 */
public class SkullLightsDyeRecipe extends NormalCraftingRecipe {

    public static final MapCodec<SkullLightsDyeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            Ingredient.CODEC.fieldOf("lights").forGetter(r -> r.lights),
            Ingredient.CODEC.fieldOf("dye").forGetter(r -> r.dye)
    ).apply(i, SkullLightsDyeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkullLightsDyeRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.lights,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.dye,
            SkullLightsDyeRecipe::new);

    private final Ingredient lights;
    private final Ingredient dye;

    public SkullLightsDyeRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                Ingredient lights, Ingredient dye) {
        super(commonInfo, bookInfo);
        this.lights = lights;
        this.dye = dye;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return find(input, true) != null && find(input, false) != null && input.ingredientCount() == 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack string = find(input, true);
        ItemStack dyeStack = find(input, false);
        if (string == null || dyeStack == null) return ItemStack.EMPTY;
        SkullLights old = string.getOrDefault(Registration.SKULL_LIGHTS.get(), SkullLights.defaultLights());
        ItemStack recoloured = string.copyWithCount(1);
        recoloured.set(Registration.SKULL_LIGHTS.get(),
                new SkullLights(old.skulls(), dyeStack.getOrDefault(DataComponents.DYE, DyeColor.WHITE)));
        return recoloured;
    }

    /** The one string (not half hung), or the one dye, in the grid; null if there isn't exactly one. */
    private ItemStack find(CraftingInput input, boolean string) {
        ItemStack found = null;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            boolean isString = lights.test(stack) && !stack.has(Registration.SKULL_LIGHTS_START.get());
            boolean match = string ? isString : dye.test(stack) && stack.has(DataComponents.DYE);
            if (!match) continue;
            if (found != null) return null;
            found = stack;
        }
        return found;
    }

    @Override
    public RecipeSerializer<SkullLightsDyeRecipe> getSerializer() {
        return Registration.SKULL_LIGHTS_DYE_SERIALIZER.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(List.of(lights, dye));
    }

    /** Shown in the recipe book as a shapeless recipe: the string and a dye give the string. */
    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                List.of(lights.display(), dye.display()),
                new SlotDisplay.ItemSlotDisplay(Registration.STRING_OF_SKULL_LIGHTS.get()),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }
}
