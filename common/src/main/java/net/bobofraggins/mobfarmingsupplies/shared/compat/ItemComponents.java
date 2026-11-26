package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** {@code stack.get/set/has/remove(component)} for 1.20.1, over item NBT. See {@link DataComponentType}. */
public final class ItemComponents {

    private ItemComponents() {}

    public static <T> T get(ItemStack stack, DataComponentType<T> type) {
        return type.read(stack.getTag());
    }

    public static <T> T getOrDefault(ItemStack stack, DataComponentType<T> type, T fallback) {
        T value = get(stack, type);
        return value != null ? value : fallback;
    }

    public static boolean has(ItemStack stack, DataComponentType<?> type) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(type.key());
    }

    public static <T> void set(ItemStack stack, DataComponentType<T> type, T value) {
        if (value == null) {
            remove(stack, type);
            return;
        }
        type.write(stack.getOrCreateTag(), value);
    }

    public static void remove(ItemStack stack, DataComponentType<?> type) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        tag.remove(type.key());
        if (tag.isEmpty()) stack.setTag(null);
    }
}
