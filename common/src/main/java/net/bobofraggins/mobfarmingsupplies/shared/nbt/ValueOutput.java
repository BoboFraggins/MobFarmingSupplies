package net.bobofraggins.mobfarmingsupplies.shared.nbt;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.Nullable;

/**
 * Writes a block entity's or entity's save data. Minecraft 1.21.1 saves into a bare
 * {@link CompoundTag}; this wraps one with the calls the save code uses (the same as later
 * versions' {@code ValueOutput}), so that code is shared with the other branches.
 */
public final class ValueOutput {

    private final CompoundTag tag;
    private final HolderLookup.Provider registries;

    public ValueOutput(CompoundTag tag, HolderLookup.Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    /** The tag being written. */
    public CompoundTag tag() {
        return tag;
    }

    public void putInt(String key, int value) {
        tag.putInt(key, value);
    }

    public void putLong(String key, long value) {
        tag.putLong(key, value);
    }

    public void putBoolean(String key, boolean value) {
        tag.putBoolean(key, value);
    }

    public void putString(String key, String value) {
        tag.putString(key, value);
    }

    public void putFloat(String key, float value) {
        tag.putFloat(key, value);
    }

    public void putDouble(String key, double value) {
        tag.putDouble(key, value);
    }

    public void putIntArray(String key, int[] value) {
        tag.putIntArray(key, value);
    }

    /** Writes {@code value} under {@code key} with {@code codec}; nothing is written if it fails to encode. */
    public <T> void store(String key, Codec<T> codec, T value) {
        codec.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), value)
                .ifSuccess(encoded -> tag.put(key, encoded));
    }

    /** As {@link #store}, writing nothing for a null value. */
    public <T> void storeNullable(String key, Codec<T> codec, @Nullable T value) {
        if (value != null) store(key, codec, value);
    }

    public void discard(String key) {
        tag.remove(key);
    }
}
