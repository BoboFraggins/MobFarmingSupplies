package net.bobofraggins.mobfarmingsupplies.skulllights;

import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSStreamCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ByteBufCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
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
            MFSCodecs.OPTIONAL_ITEM_STACK.listOf().flatXmap(
                    list -> list.isEmpty() || list.size() > PATTERN_SIZE
                            ? com.mojang.serialization.DataResult.<List<ItemStack>>error(() -> "1 to " + PATTERN_SIZE + " skulls")
                            : com.mojang.serialization.DataResult.success(list),
                    com.mojang.serialization.DataResult::success).fieldOf("skulls").forGetter(SkullLights::skulls),
            DyeColor.CODEC.fieldOf("color").forGetter(SkullLights::color)
    ).apply(i, SkullLights::new));

    public static final StreamCodec<FriendlyByteBuf, SkullLights> STREAM_CODEC = StreamCodec.composite(
            MFSStreamCodecs.ITEM_STACK.apply(ByteBufCodecs.list(PATTERN_SIZE)), SkullLights::skulls,
            MFSStreamCodecs.DYE_COLOR, SkullLights::color,
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
        if (!(o instanceof SkullLights other) || color != other.color || skulls.size() != other.skulls.size()) return false;
        for (int i = 0; i < skulls.size(); i++) {
            if (!ItemStack.matches(skulls.get(i), other.skulls.get(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (ItemStack stack : skulls) {
            hash = 31 * hash + (stack.isEmpty() ? 0 : 31 * stack.getItem().hashCode() + java.util.Objects.hashCode(stack.getTag()));
        }
        return 31 * hash + color.hashCode();
    }
}
