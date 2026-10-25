package net.bobofraggins.mobfarmingsupplies.disenchanter;

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
 * Menu for the XP Powered Disenchanter, laid out like one row of the XP Powered Incubator: the
 * item to disenchant on top, then books in, an arrow and the output, with the experience gauge on
 * the left.
 *
 * <p>Slots: 0 item, 1 books, 2 output, 3-29 player inventory, 30-38 hotbar.
 */
public class DisenchanterMenu extends AbstractContainerMenu {

    public static final int ITEM_X = 80;
    public static final int ITEM_Y = 20;
    public static final int BOOK_X = 52;
    public static final int OUTPUT_X = 108;
    public static final int ROW_Y = 44;
    /** The progress arrow (vanilla's furnace arrow, 24 wide), centred between the row's slots. */
    public static final int ARROW_X = (BOOK_X + 18 + OUTPUT_X - 24) / 2;

    public static final int GAUGE_X = 16;
    public static final int GAUGE_Y = ITEM_Y;
    public static final int GAUGE_W = 14;
    public static final int GAUGE_H = ROW_Y + 18 - ITEM_Y;

    public static final int SETTINGS_PANE_H = ROW_Y + 18 + 5 - 17;
    public static final int INV_START_X = 7;
    public static final int INV_Y = 17 + SETTINGS_PANE_H;
    public static final int HOTBAR_Y = INV_Y + 3 * 18 + 4;

    private static final int MACHINE_SLOTS = DisenchanterBlockEntity.SLOTS;
    private static final int INV_END = MACHINE_SLOTS + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** Server side, from {@link DisenchanterBlockEntity#createMenu}. */
    public DisenchanterMenu(int windowId, Inventory inv, DisenchanterBlockEntity disenchanter) {
        this(windowId, inv, disenchanter.getBlockPos(), disenchanter.items, disenchanter.data);
    }

    /** Client side: the position comes over the network; the slots and data are synced in. */
    public DisenchanterMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(windowId, inv, buf.readBlockPos(), new SimpleContainer(DisenchanterBlockEntity.SLOTS),
                new SimpleContainerData(3));
    }

    private DisenchanterMenu(int windowId, Inventory inv, BlockPos pos, Container items, ContainerData data) {
        super(Registration.DISENCHANTER_MENU.get(), windowId);
        this.data = data;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);

        addSlot(new Slot(items, DisenchanterBlockEntity.ITEM, ITEM_X, ITEM_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DisenchanterBlockEntity.isDisenchantable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addSlot(new Slot(items, DisenchanterBlockEntity.BOOK, BOOK_X, ROW_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DisenchanterBlockEntity.isBook(stack);
            }
        });
        addSlot(new Slot(items, DisenchanterBlockEntity.OUTPUT, OUTPUT_X, ROW_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
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

    /** How far the current enchantment is, 0 to 1, for the arrow. */
    public float progress() {
        return (float) data.get(0) / DisenchanterBlockEntity.PROGRESS_SCALE;
    }

    /** mB of experience in the tank, for the gauge. */
    public int experience() {
        return data.get(1) | data.get(2) << 15;
    }

    public long capacity() {
        return DisenchanterBlockEntity.CAPACITY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.DISENCHANTER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (DisenchanterBlockEntity.isBook(stack)) {
            if (!moveItemStackTo(stack, DisenchanterBlockEntity.BOOK, DisenchanterBlockEntity.BOOK + 1, false))
                return ItemStack.EMPTY;
        } else if (DisenchanterBlockEntity.isDisenchantable(stack)) {
            if (!moveItemStackTo(stack, DisenchanterBlockEntity.ITEM, DisenchanterBlockEntity.ITEM + 1, false))
                return ItemStack.EMPTY;
        } else if (index < INV_END) {
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
