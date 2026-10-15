package net.bobofraggins.mobfarmingsupplies.shared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * Codecs for this mod's {@link ShapedRecipe} subclasses: the same JSON and network form as a
 * vanilla shaped recipe, built by the subclass's own constructor.
 */
public final class ShapedRecipeCodecs {

    private ShapedRecipeCodecs() {}

    /** A shaped recipe subclass that keeps its pattern and result (vanilla's are package-private). */
    public interface Shaped {
        ShapedRecipePattern pattern();

        ItemStack result();
    }

    @FunctionalInterface
    public interface Factory<T> {
        T create(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result,
                 boolean showNotification);
    }

    public static <T extends ShapedRecipe & Shaped> MapCodec<T> mapCodec(Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                        .forGetter(ShapedRecipe::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(Shaped::pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(Shaped::result),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification)
        ).apply(i, factory::create));
    }

    public static <T extends ShapedRecipe & Shaped> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(
            Factory<T> factory) {
        return StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ShapedRecipe::getGroup,
                CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
                ShapedRecipePattern.STREAM_CODEC, Shaped::pattern,
                ItemStack.STREAM_CODEC, Shaped::result,
                ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
                factory::create);
    }
}
