package net.bobofraggins.mobfarmingsupplies.shared.compat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** 1.20.1's {@code SimpleContainer} has no {@code getItems()}; this copies a container's stacks into a list. */
public final class Containers {

    private Containers() {}

    /** {@code container}'s own list (1.20.5+'s {@code SimpleContainer.getItems()}): changing it isn't a change. */
    public static net.minecraft.core.NonNullList<ItemStack> backing(net.minecraft.world.SimpleContainer container) {
        return ((net.bobofraggins.mobfarmingsupplies.mixin.SimpleContainerAccessor) container).mobfarmingsupplies$items();
    }

    public static List<ItemStack> items(Container container) {
        List<ItemStack> items = new ArrayList<>(container.getContainerSize());
        for (int i = 0; i < container.getContainerSize(); i++) items.add(container.getItem(i));
        return items;
    }
}
