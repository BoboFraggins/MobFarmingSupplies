package net.bobofraggins.mobfarmingsupplies.shared.compat;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

/**
 * Minecraft 1.20.1 items have no data components; this stores the same values in the item's NBT
 * instead, one key per component ({@code "<modid>:<name>"}), written with the component's codec.
 * Read and write them with {@link ItemComponents}. The network codec is accepted and ignored: item
 * NBT is synced whole.
 */
public final class DataComponentType<T> {

    private final Codec<T> codec;
    private final java.util.function.Function<Tag, T> reader;
    private final java.util.function.Function<T, Tag> writer;
    private String key;

    private DataComponentType(Codec<T> codec) {
        this.codec = codec;
        this.reader = null;
        this.writer = null;
    }

    private DataComponentType(String key, java.util.function.Function<Tag, T> reader,
                              java.util.function.Function<T, Tag> writer) {
        this.codec = null;
        this.key = key;
        this.reader = reader;
        this.writer = writer;
    }

    /** A component kept under a fixed key in 1.20.1's own format (e.g. {@code SkullOwner}). */
    public static <T> DataComponentType<T> custom(String key, java.util.function.Function<Tag, T> reader,
                                                  java.util.function.Function<T, Tag> writer) {
        return new DataComponentType<>(key, reader, writer);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /** The NBT key this component is stored under. */
    public String key() {
        if (key == null) throw new IllegalStateException("Data component used before it was registered");
        return key;
    }

    public Codec<T> codec() {
        return codec;
    }

    /** The value stored in {@code tag} under this component's key, or null. */
    public T read(CompoundTag tag) {
        if (tag == null || !tag.contains(key())) return null;
        if (reader != null) return reader.apply(tag.get(key()));
        return codec.parse(NbtOps.INSTANCE, tag.get(key())).result().orElse(null);
    }

    /** Stores {@code value} in {@code tag} under this component's key. */
    public void write(CompoundTag tag, T value) {
        if (writer != null) {
            Tag t = writer.apply(value);
            if (t != null) tag.put(key(), t);
            return;
        }
        codec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent(t -> tag.put(key(), t));
    }

    public static final class Builder<T> {
        private Codec<T> codec;

        public Builder<T> persistent(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> networkSynchronized(Object ignored) {
            return this;
        }

        public DataComponentType<T> build() {
            if (codec == null) throw new IllegalStateException("A data component needs a persistent codec here");
            return new DataComponentType<>(codec);
        }
    }

    /** Names this mod's components (1.20.1 has no data component registry). */
    public static final class Register {
        private final String modid;

        public Register(String modid) {
            this.modid = modid;
        }

        public <T> Supplier<DataComponentType<T>> register(String name, Supplier<DataComponentType<T>> factory) {
            DataComponentType<T> type = factory.get();
            type.key = modid + ":" + name;
            return () -> type;
        }
    }
}
