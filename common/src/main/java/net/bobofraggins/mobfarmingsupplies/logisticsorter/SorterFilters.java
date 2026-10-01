package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemFilterData;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

/**
 * Evaluates a Logistic Sorter's Item Filters against an item. Lives outside the block entity
 * because {@code Registration} is shadowed by an inherited type inside BlockEntity subclasses.
 */
public final class SorterFilters {

    private SorterFilters() {}

    public static boolean isFilter(ItemStack stack) {
        return stack.is(Registration.ITEM_FILTER.get()) && stack.has(Registration.ITEM_FILTER_DATA.get());
    }

    public static boolean hasAnyFilter(Container filters) {
        for (int i = 0; i < filters.getContainerSize(); i++) {
            if (isFilter(filters.getItem(i))) return true;
        }
        return false;
    }

    /**
     * AND: the item must pass every installed filter. OR: it must pass at least one.
     * Empty slots are ignored; with no filters installed nothing matches.
     */
    public static boolean matches(Container filters, boolean and, ItemStack item,
                                  @Nullable HolderLookup.Provider registries) {
        boolean any = false;
        for (int i = 0; i < filters.getContainerSize(); i++) {
            ItemStack f = filters.getItem(i);
            if (!isFilter(f)) continue;
            any = true;
            ItemFilterData data = f.get(Registration.ITEM_FILTER_DATA.get());
            boolean pass = data.test(item, registries);
            if (and && !pass) return false;
            if (!and && pass) return true;
        }
        return and && any;
    }
}
