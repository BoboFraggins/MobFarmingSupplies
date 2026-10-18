package net.bobofraggins.mobfarmingsupplies.skulllights;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * What a String of Skull Lights is made of: the skulls it strings, in the order they repeat along
 * it, and the colour they glow ({@link DyeColor#BLACK}: they don't).
 */
public record SkullLights(List<ItemStack> skulls, DyeColor color) {

    /** How many skulls a string's pattern holds (the recipe's top row). */
    public static final int PATTERN_SIZE = 3;

    public static final Codec<SkullLights> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.CODEC.listOf(1, PATTERN_SIZE).fieldOf("skulls").forGetter(SkullLights::skulls),
            DyeColor.CODEC.fieldOf("color").forGetter(SkullLights::color)
    ).apply(i, SkullLights::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkullLights> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(PATTERN_SIZE)), SkullLights::skulls,
            DyeColor.STREAM_CODEC.<ByteBuf>cast(), SkullLights::color,
            SkullLights::new);

    public SkullLights {
        skulls = skulls.stream().map(stack -> stack.copyWithCount(1)).toList();
    }

    /** A string as it comes from the creative tab: the three vanilla skull-and-bone heads, orange. */
    public static SkullLights defaultLights() {
        return new SkullLights(List.of(new ItemStack(Items.SKELETON_SKULL), new ItemStack(Items.WITHER_SKELETON_SKULL),
                new ItemStack(Items.ZOMBIE_HEAD)), DyeColor.ORANGE);
    }

    /** The skull at {@code index} along the string: the pattern, repeated. */
    public ItemStack skullAt(int index) {
        return skulls.get(index % skulls.size());
    }

    public boolean lit() {
        return color != DyeColor.BLACK;
    }

    // ItemStack has no equals, so the record's own would compare the stacks by identity.
    @Override
    public boolean equals(Object o) {
        return o instanceof SkullLights other && color == other.color && ItemStack.listMatches(skulls, other.skulls);
    }

    @Override
    public int hashCode() {
        return 31 * ItemStack.hashStackList(skulls) + color.hashCode();
    }
}
