package net.bobofraggins.mobfarmingsupplies.shared.nbt;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

/**
 * Reads a block entity's or entity's save data. Minecraft 1.21.1 loads from a bare
 * {@link CompoundTag}; this wraps one with the calls the load code uses (the same as later
 * versions' {@code ValueInput}), so that code is shared with the other branches.
 */
public final class ValueInput {

    private final CompoundTag tag;
    private final HolderLookup.Provider registries;

    public ValueInput(CompoundTag tag, HolderLookup.Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    /** The tag being read. */
    public CompoundTag tag() {
        return tag;
    }

    public int getIntOr(String key, int fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : fallback;
    }

    public long getLongOr(String key, long fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getLong(key) : fallback;
    }

    public boolean getBooleanOr(String key, boolean fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : fallback;
    }

    public float getFloatOr(String key, float fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getFloat(key) : fallback;
    }

    public String getStringOr(String key, String fallback) {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : fallback;
    }

    /** The value under {@code key} read with {@code codec}, or empty if it's missing or doesn't parse. */
    public <T> Optional<T> read(String key, Codec<T> codec) {
        Tag value = tag.get(key);
        if (value == null) return Optional.empty();
        return codec.parse(registries.createSerializationContext(NbtOps.INSTANCE), value).result();
    }

    /** The list under {@code key} read with {@code codec}, or an empty list. */
    public <T> List<T> listOrEmpty(String key, Codec<T> codec) {
        return read(key, codec.listOf()).orElse(List.of());
    }
}
