package net.bobofraggins.mobfarmingsupplies.bridge;

import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSStreamCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ByteBufCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * The shapeless recipe that adds a bridge to an existing channel: a bridge plus the ingredients of a
 * new one gives two bridges on the original bridge's channel (a new random one if it had none yet).
 *
 * <p>Written like a shapeless recipe in JSON, with type {@code mobfarmingsupplies:bridge_link}.
 */
public class BridgeLinkRecipe extends ShapelessRecipe {

    public static final RecipeSerializer<BridgeLinkRecipe> SERIALIZER =
            ShapedRecipeCodecs.shapelessSerializer(BridgeLinkRecipe::new);

    private final ItemStack result;

    public BridgeLinkRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result,
                            NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
        this.result = result;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registries) {
        int channel = 0;
        for (ItemStack ingredient : input.getItems()) {
            channel = ItemComponents.getOrDefault(ingredient, Registration.BRIDGE_CHANNEL.get(), 0);
            if (channel != 0) break;
        }
        if (channel == 0) channel = EinsteinRosenBridgeBlockEntity.randomChannel(RandomSource.create());
        ItemStack bridges = result.copy();
        ItemComponents.set(bridges, Registration.BRIDGE_CHANNEL.get(), channel);
        return bridges;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.BRIDGE_LINK_SERIALIZER.get();
    }
}
