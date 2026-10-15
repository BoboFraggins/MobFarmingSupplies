package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.item;

import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.resource.Resource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** An item with its components, without a count. Immutable. */
public final class ItemResource implements Resource {

    public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);

    private final ItemStack prototype;

    private ItemResource(ItemStack prototype) {
        this.prototype = prototype;
    }

    public static ItemResource of(ItemStack stack) {
        return stack.isEmpty() ? EMPTY : new ItemResource(stack.copyWithCount(1));
    }

    public static ItemResource of(ItemLike item) {
        return of(new ItemStack(item));
    }

    @Override
    public boolean isEmpty() {
        return prototype.isEmpty();
    }

    public Item getItem() {
        return prototype.getItem();
    }

    /** A new stack of {@code count} of this item. */
    public ItemStack toStack(int count) {
        return prototype.copyWithCount(count);
    }

    /** Whether {@code stack} is this item with these components. */
    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(prototype, stack);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ItemResource other && ItemStack.isSameItemSameComponents(prototype, other.prototype);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(prototype);
    }

    @Override
    public String toString() {
        return "ItemResource[" + prototype + "]";
    }
}
