package net.bobofraggins.mobfarmingsupplies.shared.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * A recipe serializer from a codec pair, as the newer branches write them. 1.20.1 recipes carry
 * their id, so the recipe must be an {@link IdentifiedRecipe}: the id is set after decoding.
 */
public record SimpleRecipeSerializer<T extends Recipe<?> & IdentifiedRecipe>(MapCodec<T> codec,
        StreamCodec<FriendlyByteBuf, T> streamCodec) implements RecipeSerializer<T> {

    @Override
    public T fromJson(ResourceLocation id, JsonObject json) {
        T recipe = codec.codec().parse(JsonOps.INSTANCE, json)
                .getOrThrow(false, msg -> {})
                ;
        recipe.setId(id);
        return recipe;
    }

    @Override
    public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        T recipe = streamCodec.decode(buf);
        recipe.setId(id);
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, T recipe) {
        streamCodec.encode(buf, recipe);
    }
}
