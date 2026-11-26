package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.nbt.CompoundTag;

/** Stand-in for 1.20.5+'s {@code CustomData}: an immutable view of a compound tag. */
public final class CustomData {

    public static final CustomData EMPTY = new CustomData(new CompoundTag());

    private final CompoundTag tag;

    private CustomData(CompoundTag tag) {
        this.tag = tag;
    }

    public static CustomData of(CompoundTag tag) {
        return new CustomData(tag.copy());
    }

    public CompoundTag copyTag() {
        return tag.copy();
    }

    /** The tag itself; don't change it. */
    public CompoundTag getUnsafe() {
        return tag;
    }

    public boolean contains(String key) {
        return tag.contains(key);
    }

    public boolean isEmpty() {
        return tag.isEmpty();
    }
}
