package net.bobofraggins.mobfarmingsupplies.bridge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
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

    public static final MapCodec<BridgeLinkRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                    .forGetter(ShapelessRecipe::category),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(
                    list -> list.isEmpty() || list.size() > 9
                            ? DataResult.<NonNullList<Ingredient>>error(() -> "A bridge link needs 1 to 9 ingredients")
                            : DataResult.success(NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new))),
                    DataResult::success).forGetter(ShapelessRecipe::getIngredients)
    ).apply(i, BridgeLinkRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BridgeLinkRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ShapelessRecipe::getGroup,
            CraftingBookCategory.STREAM_CODEC, ShapelessRecipe::category,
            ItemStack.STREAM_CODEC, r -> r.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)),
            ShapelessRecipe::getIngredients,
            BridgeLinkRecipe::new);

    private final ItemStack result;

    public BridgeLinkRecipe(String group, CraftingBookCategory category, ItemStack result,
                            NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
        this.result = result;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        int channel = 0;
        for (ItemStack ingredient : input.items()) {
            channel = ingredient.getOrDefault(Registration.BRIDGE_CHANNEL.get(), 0);
            if (channel != 0) break;
        }
        if (channel == 0) channel = EinsteinRosenBridgeBlockEntity.randomChannel(RandomSource.create());
        ItemStack bridges = result.copy();
        bridges.set(Registration.BRIDGE_CHANNEL.get(), channel);
        return bridges;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.BRIDGE_LINK_SERIALIZER.get();
    }
}
