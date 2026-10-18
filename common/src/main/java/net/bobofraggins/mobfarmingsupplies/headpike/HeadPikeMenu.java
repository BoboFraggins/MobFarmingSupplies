package net.bobofraggins.mobfarmingsupplies.headpike;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The Head Pike's menu: its three skull slots in a column, top to bottom as on the pike, over the
 * player's inventory. Laid out like a dispenser's.
 */
public class HeadPikeMenu extends AbstractContainerMenu {

    public static final int SKULL_X = 80;
    public static final int SKULL_TOP = 17;
    private static final int INV_TOP = 84;
    private static final int HOTBAR_TOP = 142;

    private final Container pike;

    /** Client side: the slots are filled in from the server. */
    public HeadPikeMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(HeadPikeBlockEntity.SLOTS) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return stack.is(net.minecraft.tags.ItemTags.SKULLS);
            }
        });
    }

    public HeadPikeMenu(int containerId, Inventory inventory, Container pike) {
        super(Registration.HEAD_PIKE_MENU.get(), containerId);
        this.pike = pike;
        pike.startOpen(inventory.player);
        for (int i = 0; i < HeadPikeBlockEntity.SLOTS; i++) {
            addSlot(new Slot(pike, i, SKULL_X, SKULL_TOP + i * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return container.canPlaceItem(getContainerSlot(), stack);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, INV_TOP + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, HOTBAR_TOP));
    }

    @Override
    public boolean stillValid(Player player) {
        return pike.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        pike.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int pikeSlots = HeadPikeBlockEntity.SLOTS;
        if (index < pikeSlots) {
            if (!moveItemStackTo(stack, pikeSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, pikeSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }
}
