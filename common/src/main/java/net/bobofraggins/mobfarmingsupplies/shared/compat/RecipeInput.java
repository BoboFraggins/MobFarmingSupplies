package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Stand-in for 1.20.5+'s read-only recipe input. 1.20.1 recipes match against a {@link Container},
 * so this is one, read-only, built from {@link #getItem} and {@link #size}.
 */
public interface RecipeInput extends Container {

    @Override
    ItemStack getItem(int index);

    int size();

    @Override
    default int getContainerSize() {
        return size();
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) if (!getItem(i).isEmpty()) return false;
        return true;
    }

    @Override
    default ItemStack removeItem(int index, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    default void setItem(int index, ItemStack stack) {}

    @Override
    default void setChanged() {}

    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default void clearContent() {}
}
