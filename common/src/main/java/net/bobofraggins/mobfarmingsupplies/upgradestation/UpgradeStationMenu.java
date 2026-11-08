package net.bobofraggins.mobfarmingsupplies.upgradestation;

import net.bobofraggins.mobfarmingsupplies.register.MFSTags;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Menu for the Upgrade Station: the item on top, then the input and the result, which is taken
 * like a crafting table's. Opening it opens the lid.
 *
 * <p>Slots: 0 item, 1 input, 2 result, 3-29 player inventory, 30-38 hotbar.
 */
public class UpgradeStationMenu extends AbstractContainerMenu {

    public static final int ITEM_X = 80;
    public static final int ITEM_Y = 20;
    public static final int INPUT_X = 52;
    public static final int OUTPUT_X = 108;
    public static final int ROW_Y = 44;

    /** The arrow between the input and the result (vanilla's furnace arrow, 24 wide), centred in the gap. */
    public static final int ARROW_X = (INPUT_X + 18 + OUTPUT_X - 24) / 2;

    public static final int SETTINGS_PANE_H = ROW_Y + 18 + 5 - 17;
    public static final int INV_START_X = 7;
    public static final int INV_Y = 17 + SETTINGS_PANE_H;
    public static final int HOTBAR_Y = INV_Y + 3 * 18 + 4;

    private static final int MACHINE_SLOTS = UpgradeStationBlockEntity.SLOTS;
    private static final int INV_END = MACHINE_SLOTS + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container items;
    private final ContainerLevelAccess access;
    @Nullable private final UpgradeStationBlockEntity station;

    /** Server side, from {@link UpgradeStationBlockEntity#createMenu}. */
    public UpgradeStationMenu(int windowId, Inventory inv, UpgradeStationBlockEntity station) {
        this(windowId, inv, station.getBlockPos(), station.items, station);
        station.startViewing(inv.player);
    }

    /** Client side: the position comes over the network; the slots are synced in. */
    public UpgradeStationMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(windowId, inv, buf.readBlockPos(), new SimpleContainer(UpgradeStationBlockEntity.SLOTS), null);
    }

    private UpgradeStationMenu(int windowId, Inventory inv, BlockPos pos, Container items,
                               @Nullable UpgradeStationBlockEntity station) {
        super(Registration.UPGRADE_STATION_MENU.get(), windowId);
        this.items = items;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);
        this.station = station;

        addSlot(new Slot(items, UpgradeStationBlockEntity.ITEM_SLOT, ITEM_X, ITEM_Y));
        addSlot(new Slot(items, UpgradeStationBlockEntity.INPUT_SLOT, INPUT_X, ROW_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isInput(stack);
            }
        });
        addSlot(new Slot(items, UpgradeStationBlockEntity.OUTPUT_SLOT, OUTPUT_X, ROW_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());
                items.removeItem(UpgradeStationBlockEntity.ITEM_SLOT, 1);
                items.removeItem(UpgradeStationBlockEntity.INPUT_SLOT, 1);
                super.onTake(player, stack);
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
    }

    /** What the input slot takes: {@code #mobfarmingsupplies:upgrade_station_inputs} (the Blank Upgrade, ...). */
    public static boolean isInput(ItemStack stack) {
        return stack.is(MFSTags.Items.UPGRADE_STATION_INPUTS);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.UPGRADE_STATION.get());
    }

    /** Closing the window shuts the lid (once the last viewer has closed it). */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (station != null) station.stopViewing(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, HOTBAR_END, true)) return ItemStack.EMPTY;
        } else {
            // Into the slot it belongs in, or if that's taken, between the inventory and the hotbar.
            int target = isInput(stack) ? UpgradeStationBlockEntity.INPUT_SLOT : UpgradeStationBlockEntity.ITEM_SLOT;
            if (!moveItemStackTo(stack, target, target + 1, false)
                    && !(index < INV_END
                            ? moveItemStackTo(stack, INV_END, HOTBAR_END, false)
                            : moveItemStackTo(stack, MACHINE_SLOTS, INV_END, false))) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        // As a crafting table does: whatever of a result didn't fit is dropped.
        if (index == UpgradeStationBlockEntity.OUTPUT_SLOT) player.drop(stack, false);
        return original;
    }
}
