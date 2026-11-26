package net.bobofraggins.mobfarmingsupplies.skulllights;

import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs.ShapedRecipePattern;

/**
 * The String of Skull Lights recipe: shaped (skulls along the top, a dye in the middle), and the
 * string remembers the skulls in the order they were laid left to right and glows the dye's
 * colour.
 *
 * <p>Written like a shaped recipe in JSON, with type {@code mobfarmingsupplies:skull_lights}.
 */
public class SkullLightsRecipe extends ShapedRecipe implements ShapedRecipeCodecs.Shaped {

    public static final RecipeSerializer<SkullLightsRecipe> SERIALIZER = ShapedRecipeCodecs.serializer(SkullLightsRecipe::new);

    private final ShapedRecipePattern pattern;
    private final ItemStack result;

    public SkullLightsRecipe(ResourceLocation id, String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                             ItemStack result, boolean showNotification) {
        super(id, group, category, pattern.width(), pattern.height(), pattern.ingredients(), result, showNotification);
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
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registries) {
        // The grid is the 3x3 pattern exactly: skulls on row 0, the dye at the centre.
        List<ItemStack> skulls = new ArrayList<>(SkullLights.PATTERN_SIZE);
        for (int x = 0; x < SkullLights.PATTERN_SIZE; x++) skulls.add(input.getItem(x + 0 * input.getWidth()));
        DyeColor color = input.getItem(1 + 1 * input.getWidth()).getItem() instanceof DyeItem dye ? dye.getDyeColor() : DyeColor.WHITE;
        ItemStack string = result.copy();
        ItemComponents.set(string, Registration.SKULL_LIGHTS.get(), new SkullLights(skulls, color));
        return string;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.SKULL_LIGHTS_SERIALIZER.get();
    }
}
