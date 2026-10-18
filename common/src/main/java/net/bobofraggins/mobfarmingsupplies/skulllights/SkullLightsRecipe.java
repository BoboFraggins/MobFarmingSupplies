package net.bobofraggins.mobfarmingsupplies.skulllights;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * The String of Skull Lights recipe: shaped (skulls along the top, a dye in the middle), and the
 * string remembers the skulls in the order they were laid left to right and glows the dye's
 * colour.
 *
 * <p>Written like a shaped recipe in JSON, with type {@code mobfarmingsupplies:skull_lights}.
 */
public class SkullLightsRecipe extends ShapedRecipe implements ShapedRecipeCodecs.Shaped {

    public static final MapCodec<SkullLightsRecipe> MAP_CODEC = ShapedRecipeCodecs.mapCodec(SkullLightsRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, SkullLightsRecipe> STREAM_CODEC =
            ShapedRecipeCodecs.streamCodec(SkullLightsRecipe::new);

    private final ShapedRecipePattern pattern;
    private final ItemStack result;

    public SkullLightsRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                             ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
        this.pattern = pattern;
        this.result = result;
    }

    @Override
    public ShapedRecipePattern pattern() {
        return pattern;
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        // The grid is the 3x3 pattern exactly: skulls on row 0, the dye at the centre.
        List<ItemStack> skulls = new ArrayList<>(SkullLights.PATTERN_SIZE);
        for (int x = 0; x < SkullLights.PATTERN_SIZE; x++) skulls.add(input.getItem(x, 0));
        DyeColor color = input.getItem(1, 1).getItem() instanceof DyeItem dye ? dye.getDyeColor() : DyeColor.WHITE;
        ItemStack string = result.copy();
        string.set(Registration.SKULL_LIGHTS.get(), new SkullLights(skulls, color));
        return string;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.SKULL_LIGHTS_SERIALIZER.get();
    }
}
