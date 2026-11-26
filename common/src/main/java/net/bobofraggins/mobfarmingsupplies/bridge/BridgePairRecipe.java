package net.bobofraggins.mobfarmingsupplies.bridge;

import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs.ShapedRecipePattern;

/**
 * The shaped recipe that makes a linked pair of Einstein-Rosen Bridges: the result (two bridges)
 * gets a new random channel each time it's crafted.
 *
 * <p>Written like a shaped recipe in JSON, with type {@code mobfarmingsupplies:bridge_pair} (see
 * {@link net.bobofraggins.mobfarmingsupplies.tank.BridgePairRecipe} for why it can't extend
 * {@code ShapedRecipe}).
 */
public class BridgePairRecipe extends ShapedRecipe implements ShapedRecipeCodecs.Shaped {

    public static final RecipeSerializer<BridgePairRecipe> SERIALIZER = ShapedRecipeCodecs.serializer(BridgePairRecipe::new);

    private final ShapedRecipePattern pattern;
    private final ItemStack result;

    public BridgePairRecipe(ResourceLocation id, String group, CraftingBookCategory category, ShapedRecipePattern pattern,
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
        ItemStack bridges = result.copy();
        ItemComponents.set(bridges, Registration.BRIDGE_CHANNEL.get(), EinsteinRosenBridgeBlockEntity.randomChannel(RandomSource.create()));
        return bridges;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.BRIDGE_PAIR_SERIALIZER.get();
    }
}
