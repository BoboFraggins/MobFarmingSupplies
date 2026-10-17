package net.bobofraggins.mobfarmingsupplies.skulllights;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
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
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

/**
 * The String of Skull Lights recipe: shaped (skulls along the top, a dye in the middle), and the
 * string remembers the skulls in the order they were laid left to right and glows the dye's
 * colour.
 *
 * <p>Written like a shaped recipe in JSON, with type {@code mobfarmingsupplies:skull_lights}.
 */
public class SkullLightsRecipe extends NormalCraftingRecipe {

    public static final MapCodec<SkullLightsRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, SkullLightsRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkullLightsRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            SkullLightsRecipe::new);

    private final ShapedRecipePattern pattern;
    private final ItemStackTemplate result;

    public SkullLightsRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                             ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo);
        this.pattern = pattern;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return pattern.matches(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        // The grid is the 3x3 pattern exactly: skulls on row 0, the dye at the centre.
        List<ItemStack> skulls = new ArrayList<>(SkullLights.PATTERN_SIZE);
        for (int x = 0; x < SkullLights.PATTERN_SIZE; x++) skulls.add(input.getItem(x, 0));
        DyeColor color = input.getItem(1, 1).getOrDefault(DataComponents.DYE, DyeColor.WHITE);
        ItemStack string = result.create();
        string.set(Registration.SKULL_LIGHTS.get(), new SkullLights(skulls, color));
        return string;
    }

    @Override
    public RecipeSerializer<SkullLightsRecipe> getSerializer() {
        return Registration.SKULL_LIGHTS_SERIALIZER.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(pattern.ingredients());
    }

    /** Shown in the recipe book exactly like an ordinary shaped recipe. */
    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(
                pattern.width(),
                pattern.height(),
                pattern.ingredients().stream()
                        .map(slot -> slot.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE))
                        .toList(),
                new SlotDisplay.ItemStackSlotDisplay(result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }
}
