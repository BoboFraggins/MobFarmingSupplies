package net.bobofraggins.mobfarmingsupplies.itemfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

/**
 * Data component stored on a scribed Item Filter: one {@link ItemMatcher} plus whether the
 * match is negated ("is not").
 */
public record ItemFilterData(ItemMatcher matcher, boolean negate) {

    public static final Codec<ItemFilterData> CODEC = RecordCodecBuilder.create(i -> i.group(
                    ItemMatcher.CODEC.fieldOf("matcher").forGetter(ItemFilterData::matcher),
                    Codec.BOOL.optionalFieldOf("negate", false).forGetter(ItemFilterData::negate))
            .apply(i, ItemFilterData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemFilterData> STREAM_CODEC = StreamCodec.composite(
            ItemMatcher.STREAM_CODEC, ItemFilterData::matcher,
            ByteBufCodecs.BOOL, ItemFilterData::negate,
            ItemFilterData::new);

    /** Whether {@code stack} passes this filter. Empty stacks never pass. */
    public boolean test(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        return !stack.isEmpty() && matcher.test(stack, registries) != negate;
    }

    /** "is a sword" / "is not enchantable". */
    public Component description() {
        return Component.translatable(
                negate ? "filter.mobfarmingsupplies.is_not" : "filter.mobfarmingsupplies.is",
                matcher.description());
    }
}
