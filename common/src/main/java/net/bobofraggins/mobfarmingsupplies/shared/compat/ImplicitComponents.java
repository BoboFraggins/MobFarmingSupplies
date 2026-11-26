package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.nbt.CompoundTag;

/**
 * A block entity that keeps some of its state on its item (1.20.5+'s implicit components). On
 * 1.20.1 the {@code mobfarmingsupplies:copy_components} loot function calls {@link #collectImplicitComponents}
 * and {@code BlockItemImplicitComponentsMixin} calls {@link #applyImplicitComponents} when the block is placed.
 */
public interface ImplicitComponents {

    void collectImplicitComponents(DataComponentMap.Builder components);

    void applyImplicitComponents(DataComponentInput input);

    /** Drops what {@link #collectImplicitComponents} wrote from a tag the item also keeps (1.20.5+'s hook of the same name). */
    default void removeComponentsFromTag(CompoundTag tag) {}
}
