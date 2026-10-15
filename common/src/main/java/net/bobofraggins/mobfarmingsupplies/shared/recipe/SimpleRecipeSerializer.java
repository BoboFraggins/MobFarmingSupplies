package net.bobofraggins.mobfarmingsupplies.shared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** A recipe serializer from a codec pair (1.21.1's {@link RecipeSerializer} is an interface). */
public record SimpleRecipeSerializer<T extends Recipe<?>>(MapCodec<T> codec,
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) implements RecipeSerializer<T> {}
