package net.bobofraggins.mobfarmingsupplies.bridge;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * The shaped recipe that makes a linked pair of Einstein-Rosen Bridges: the result (two bridges)
 * gets a new random channel each time it's crafted.
 *
 * <p>Written like a shaped recipe in JSON, with type {@code mobfarmingsupplies:bridge_pair} (see
 * {@link net.bobofraggins.mobfarmingsupplies.tank.BridgePairRecipe} for why it can't extend
 * {@code ShapedRecipe}).
 */
public class BridgePairRecipe extends ShapedRecipe implements ShapedRecipeCodecs.Shaped {

    public static final MapCodec<BridgePairRecipe> MAP_CODEC = ShapedRecipeCodecs.mapCodec(BridgePairRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, BridgePairRecipe> STREAM_CODEC =
            ShapedRecipeCodecs.streamCodec(BridgePairRecipe::new);

    private final ShapedRecipePattern pattern;
    private final ItemStack result;

    public BridgePairRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
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
        ItemStack bridges = result.copy();
        bridges.set(Registration.BRIDGE_CHANNEL.get(), EinsteinRosenBridgeBlockEntity.randomChannel(RandomSource.create()));
        return bridges;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.BRIDGE_PAIR_SERIALIZER.get();
    }
}
