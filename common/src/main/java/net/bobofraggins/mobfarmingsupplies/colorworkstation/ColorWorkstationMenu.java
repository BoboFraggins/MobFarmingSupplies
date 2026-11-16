package net.bobofraggins.mobfarmingsupplies.colorworkstation;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the Color Workstation: the Essence of Color gauge, then the sixteen dye outputs in a
 * 4×4 grid (black top left, white bottom right).
 *
 * <p>Slots: 0-15 dyes, 16-42 player inventory, 43-51 hotbar.
 */
public class ColorWorkstationMenu extends AbstractContainerMenu {

    public static final int GRID_Y = 20;
    public static final int GRID_SIZE = 4 * 18;
    public static final int GAUGE_X = 16;
    public static final int GAUGE_Y = GRID_Y;
    public static final int GAUGE_W = 14;
    public static final int GAUGE_H = GRID_SIZE;
    /** The grid, centred in the window. */
    public static final int GRID_X = (176 - GRID_SIZE) / 2;

    public static final int SETTINGS_PANE_H = GRID_Y + GRID_SIZE + 5 - 17;
    public static final int INV_START_X = 7;
    public static final int INV_Y = 17 + SETTINGS_PANE_H;
    public static final int HOTBAR_Y = INV_Y + 3 * 18 + 4;

    private static final int MACHINE_SLOTS = ColorWorkstationBlockEntity.DYES.size();
    private static final int INV_END = MACHINE_SLOTS + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** Server side, from {@link ColorWorkstationBlockEntity#createMenu}. */
    public ColorWorkstationMenu(int windowId, Inventory inv, ColorWorkstationBlockEntity be) {
        this(windowId, inv, be.getBlockPos(), be.dyes, new ContainerData() {
            @Override
            public int get(int index) {
                int mb = (int) Math.min(be.getAmount(), Integer.MAX_VALUE);
                return index == 0 ? mb & 0x7FFF : mb >>> 15;
            }

            @Override
            public void set(int index, int value) {}

            @Override
            public int getCount() {
                return 2;
            }
        });
    }

    /** Client side: the position comes over the network; the slots and data are synced in. */
    public ColorWorkstationMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(windowId, inv, buf.readBlockPos(), new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(2));
    }

    private ColorWorkstationMenu(int windowId, Inventory inv, BlockPos pos, Container dyes, ContainerData data) {
        super(Registration.COLOR_WORKSTATION_MENU.get(), windowId);
        this.data = data;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);

        for (int i = 0; i < MACHINE_SLOTS; i++) {
            addSlot(new Slot(dyes, i, GRID_X + (i % 4) * 18, GRID_Y + (i / 4) * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, INV_START_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, INV_START_X + col * 18, HOTBAR_Y));
        }
        addDataSlots(data);
    }

    /** mB of Essence of Color in the buffer, for the gauge. */
    public int essence() {
        return data.get(0) | data.get(1) << 15;
    }

    public long capacity() {
        return ColorWorkstationBlockEntity.CAPACITY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.COLOR_WORKSTATION.get());
    }

    /** Dyes don't get swept up by a double-click: each would spend the buffer. */
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.index >= MACHINE_SLOTS;
    }

    /**
     * Shift-click on a dye moves one stack of it into the inventory and spends the buffer for
     * what fit; on anything else, swaps between inventory and hotbar (nothing goes in the grid).
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();

        if (index < MACHINE_SLOTS) {
            int before = stack.getCount();
            moveItemStackTo(stack, MACHINE_SLOTS, HOTBAR_END, true);
            int moved = before - stack.getCount();
            if (moved > 0) {
                ItemStack taken = slot.remove(moved);
                slot.onTake(player, taken);
            }
            // One stack per shift-click: returning empty stops vanilla from repeating it.
            return ItemStack.EMPTY;
        }

        ItemStack original = stack.copy();
        if (index < INV_END) {
            if (!moveItemStackTo(stack, INV_END, HOTBAR_END, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, MACHINE_SLOTS, INV_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
