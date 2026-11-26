package net.bobofraggins.mobfarmingsupplies.shared.compat;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

/** Stand-in for 1.20.5+'s component map, as far as block entities' implicit components need it. */
public final class DataComponentMap {

    private DataComponentMap() {}

    /** Collects the components a block entity puts on its item. */
    public static final class Builder {
        private final Map<DataComponentType<?>, Object> values = new LinkedHashMap<>();

        public <T> Builder set(DataComponentType<T> type, T value) {
            if (value == null) values.remove(type);
            else values.put(type, value);
            return this;
        }

        /** Writes what was collected onto {@code stack}, only the components {@code keep} accepts. */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void applyTo(ItemStack stack, java.util.function.Predicate<DataComponentType<?>> keep) {
            values.forEach((type, value) -> {
                if (keep.test(type)) ItemComponents.set(stack, (DataComponentType) type, value);
            });
        }
    }
}
