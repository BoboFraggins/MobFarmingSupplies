package net.bobofraggins.mobfarmingsupplies.oresight;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
 * A Bucket of Sight Essence and an ore, or what an ore drops (Raw Gold, a Diamond, Netherite Scrap,
 * ...), anywhere in the grid, give a Bucket of Oresight for that ore. The essence bucket becomes the
 * new bucket, so no empty bucket is left behind.
 *
 * <p>Type {@code mobfarmingsupplies:oresight} in JSON, with {@code essence} and {@code ore}
 * ingredients (the {@code ore} one is {@code #mobfarmingsupplies:oresight_inputs}). What the ore
 * ingredient stands for comes from {@link OresightTargets#oreFor}.
 */
public class OresightRecipe extends NormalCraftingRecipe {

    public static final MapCodec<OresightRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            Ingredient.CODEC.fieldOf("essence").forGetter(r -> r.essence),
            Ingredient.CODEC.fieldOf("ore").forGetter(r -> r.ore)
    ).apply(i, OresightRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, OresightRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.essence,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ore,
            OresightRecipe::new);

    private final Ingredient essence;
    private final Ingredient ore;

    public OresightRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                          Ingredient essence, Ingredient ore) {
        super(commonInfo, bookInfo);
        this.essence = essence;
        this.ore = ore;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return input.ingredientCount() == 2 && find(input, true) != null && find(input, false) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack block = find(input, false);
        if (block == null || find(input, true) == null) return ItemStack.EMPTY;
        return OresightBucketItem.of(OresightTargets.oreFor(block));
    }

    /** Nothing is left: the essence bucket became the Bucket of Oresight. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    /** The one essence bucket, or the one ore, in the grid; null if there isn't exactly one. */
    private ItemStack find(CraftingInput input, boolean wantEssence) {
        ItemStack found = null;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            boolean isEssence = essence.test(stack);
            boolean match = wantEssence ? isEssence
                    : !isEssence && ore.test(stack) && OresightTargets.oreFor(stack) != null;
            if (!match) continue;
            if (found != null) return null;
            found = stack;
        }
        return found;
    }

    @Override
    public RecipeSerializer<OresightRecipe> getSerializer() {
        return Registration.ORESIGHT_SERIALIZER.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(List.of(essence, ore));
    }

    /** The essence and any ore or ore drop; the result shown is the Diamond Ores one, as an example. */
    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                List.of(essence.display(), ore.display()),
                new SlotDisplay.ItemStackSlotDisplay(net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(
                        OresightBucketItem.of(net.minecraft.world.level.block.Blocks.DIAMOND_ORE))),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }
}
