package net.bobofraggins.mobfarmingsupplies.shared.compat;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** Stand-in for 1.20.5+'s {@code ByteBufCodecs}, over {@link FriendlyByteBuf}. See {@link StreamCodec}. */
public final class ByteBufCodecs {

    private ByteBufCodecs() {}

    public static final StreamCodec<FriendlyByteBuf, Boolean> BOOL =
            StreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);
    public static final StreamCodec<FriendlyByteBuf, Integer> INT =
            StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt);
    public static final StreamCodec<FriendlyByteBuf, Integer> VAR_INT =
            StreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
    public static final StreamCodec<FriendlyByteBuf, Long> VAR_LONG =
            StreamCodec.of(FriendlyByteBuf::writeVarLong, FriendlyByteBuf::readVarLong);
    public static final StreamCodec<FriendlyByteBuf, Float> FLOAT =
            StreamCodec.of(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat);
    public static final StreamCodec<FriendlyByteBuf, Double> DOUBLE =
            StreamCodec.of(FriendlyByteBuf::writeDouble, FriendlyByteBuf::readDouble);
    public static final StreamCodec<FriendlyByteBuf, String> STRING_UTF8 =
            StreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);
    public static final StreamCodec<FriendlyByteBuf, CompoundTag> COMPOUND_TAG =
            StreamCodec.of(FriendlyByteBuf::writeNbt, buf -> {
                CompoundTag tag = buf.readNbt();
                return tag == null ? new CompoundTag() : tag;
            });
    public static final StreamCodec<FriendlyByteBuf, ResourceLocation> RESOURCE_LOCATION =
            StreamCodec.of(FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::readResourceLocation);

    public static <B extends FriendlyByteBuf, V> StreamCodec<B, Optional<V>> optional(StreamCodec<? super B, V> codec) {
        return StreamCodec.of((buf, opt) -> {
            buf.writeBoolean(opt.isPresent());
            opt.ifPresent(v -> codec.encode(buf, v));
        }, buf -> buf.readBoolean() ? Optional.of(codec.decode(buf)) : Optional.empty());
    }

    /** A built-in registry entry, sent as its numeric id. */
    @SuppressWarnings("unchecked")
    public static <T> StreamCodec<FriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> key) {
        return StreamCodec.of(
                (buf, v) -> buf.writeVarInt(lookup(key).getId(v)),
                buf -> lookup(key).byIdOrThrow(buf.readVarInt()));
    }

    @SuppressWarnings("unchecked")
    static <T> Registry<T> lookup(ResourceKey<? extends Registry<T>> key) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
        if (registry == null) throw new IllegalStateException("Unknown registry " + key);
        return registry;
    }

    public static <T> StreamCodec<FriendlyByteBuf, T> idMapper(IntFunction<T> byId, ToIntFunction<T> toId) {
        return StreamCodec.of((buf, v) -> buf.writeVarInt(toId.applyAsInt(v)), buf -> byId.apply(buf.readVarInt()));
    }

    public static <B extends FriendlyByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int max) {
        return codec -> StreamCodec.of((buf, list) -> {
            if (list.size() > max) throw new IllegalStateException("List too long: " + list.size() + " > " + max);
            buf.writeVarInt(list.size());
            for (V v : list) codec.encode(buf, v);
        }, buf -> {
            int n = buf.readVarInt();
            if (n > max) throw new IllegalStateException("List too long: " + n + " > " + max);
            List<V> list = new java.util.ArrayList<>(n);
            for (int i = 0; i < n; i++) list.add(codec.decode(buf));
            return list;
        });
    }

    public static <B extends FriendlyByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(
            IntFunction<C> factory) {
        return codec -> StreamCodec.of((buf, c) -> {
            buf.writeVarInt(c.size());
            for (V v : c) codec.encode(buf, v);
        }, buf -> {
            int n = buf.readVarInt();
            C c = factory.apply(n);
            for (int i = 0; i < n; i++) c.add(codec.decode(buf));
            return c;
        });
    }
}
