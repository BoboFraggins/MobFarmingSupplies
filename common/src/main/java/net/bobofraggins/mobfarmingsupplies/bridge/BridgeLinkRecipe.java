package net.bobofraggins.mobfarmingsupplies.bridge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
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
 * The shapeless recipe that adds a bridge to an existing channel: a bridge plus the ingredients of a
 * new one gives two bridges on the original bridge's channel (a new random one if it had none yet).
 *
 * <p>Written like a shapeless recipe in JSON, with type {@code mobfarmingsupplies:bridge_link}.
 */
public class BridgeLinkRecipe extends NormalCraftingRecipe {

    public static final MapCodec<BridgeLinkRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(r -> r.ingredients)
    ).apply(i, BridgeLinkRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BridgeLinkRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list()), r -> r.ingredients,
            BridgeLinkRecipe::new);

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;

    public BridgeLinkRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                            ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo);
        this.result = result;
        this.ingredients = ingredients;
    }

    /** Same matching as a vanilla shapeless recipe. */
    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != ingredients.size()) return false;
        if (input.size() == 1 && ingredients.size() == 1) return ingredients.getFirst().test(input.getItem(0));
        return input.stackedContents().canCraft(this, null);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        int channel = 0;
        for (ItemStack ingredient : input.items()) {
            channel = ingredient.getOrDefault(Registration.BRIDGE_CHANNEL.get(), 0);
            if (channel != 0) break;
        }
        if (channel == 0) channel = EinsteinRosenBridgeBlockEntity.randomChannel(RandomSource.create());
        ItemStack bridges = result.create();
        bridges.set(Registration.BRIDGE_CHANNEL.get(), channel);
        return bridges;
    }

    @Override
    public RecipeSerializer<BridgeLinkRecipe> getSerializer() {
        return Registration.BRIDGE_LINK_SERIALIZER.get();
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
