package net.bobofraggins.mobfarmingsupplies.incubator;

import net.bobofraggins.mobfarmingsupplies.register.MFSTags;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankTier;
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
import org.jetbrains.annotations.Nullable;

/**
 * Menu for the XP Powered Incubator: the sample slot on top, then four rows of an input and an
 * output, with each row's progress synced for the screen's arrows. Opening it opens the lid.
 *
 * <p>Slots: 0 DNA, 1-4 eggs in, 5-8 eggs out, 9-35 player inventory, 36-44 hotbar.
 */
public class IncubatorMenu extends AbstractContainerMenu {

    public static final int DNA_X = 80;
    public static final int DNA_Y = 20;
    public static final int INPUT_X = 52;
    public static final int OUTPUT_X = 108;
    public static final int FIRST_ROW_Y = 44;
    public static final int ROW_STEP = 20;
    /** The experience gauge, left of the rows, from the top of the sample slot to the bottom of the last row. */
    public static final int GAUGE_X = 16;
    public static final int GAUGE_Y = DNA_Y;
    public static final int GAUGE_W = 14;
    public static final int GAUGE_H = FIRST_ROW_Y + 3 * ROW_STEP + 18 - DNA_Y;

    /** The progress arrow between a row's slots (vanilla's furnace arrow, 24 wide), centred in the gap. */
    public static final int ARROW_X = (INPUT_X + 18 + OUTPUT_X - 24) / 2;

    public static final int SETTINGS_PANE_H = FIRST_ROW_Y + 3 * ROW_STEP + 18 + 5 - 17;
    public static final int INV_START_X = 7;
    public static final int INV_Y = 17 + SETTINGS_PANE_H;
    public static final int HOTBAR_Y = INV_Y + 3 * 18 + 4;

    private static final int MACHINE_SLOTS = IncubatorBlockEntity.SLOTS;
    private static final int INV_END = MACHINE_SLOTS + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container items;
    private final ContainerData progress;
    private final ContainerData tank;
    private final ContainerLevelAccess access;
    @Nullable private final IncubatorBlockEntity incubator;

    /** Server side, from {@link IncubatorBlockEntity#createMenu}. */
    public IncubatorMenu(int windowId, Inventory inv, IncubatorBlockEntity incubator) {
        this(windowId, inv, incubator.getBlockPos(), incubator.items, incubator.progressData,
                incubator.tankData, incubator);
        incubator.startViewing(inv.player);
    }

    /** Client side: the position comes over the network; the slots and progress are synced in. */
    public IncubatorMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(windowId, inv, buf.readBlockPos(), new SimpleContainer(IncubatorBlockEntity.SLOTS),
                new SimpleContainerData(IncubatorBlockEntity.ROWS), new SimpleContainerData(2), null);
    }

    private IncubatorMenu(int windowId, Inventory inv, BlockPos pos, Container items, ContainerData progress,
                          ContainerData tank, @Nullable IncubatorBlockEntity incubator) {
        super(Registration.INCUBATOR_MENU.get(), windowId);
        this.items = items;
        this.progress = progress;
        this.tank = tank;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);
        this.incubator = incubator;

        addSlot(new Slot(items, IncubatorBlockEntity.DNA_SLOT, DNA_X, DNA_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSample(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < IncubatorBlockEntity.ROWS; row++) {
            addSlot(new Slot(items, IncubatorBlockEntity.FIRST_INPUT + row, INPUT_X, rowY(row)) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return isInput(stack);
                }
            });
        }
        for (int row = 0; row < IncubatorBlockEntity.ROWS; row++) {
            addSlot(new Slot(items, IncubatorBlockEntity.FIRST_OUTPUT + row, OUTPUT_X, rowY(row)) {
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
        addDataSlots(progress);
        addDataSlots(tank);
    }

    public static int rowY(int row) {
        return FIRST_ROW_Y + row * ROW_STEP;
    }

    /** What the sample slot takes: {@code #mobfarmingsupplies:incubator_samples} (DNA samples and packs, ...). */
    public static boolean isSample(ItemStack stack) {
        return stack.is(MFSTags.Items.INCUBATOR_SAMPLES);
    }

    /** What the input slots take: {@code #mobfarmingsupplies:incubator_inputs} (the eggs, ...). */
    public static boolean isInput(ItemStack stack) {
        return stack.is(MFSTags.Items.INCUBATOR_INPUTS);
    }

    /** How far a row has incubated, 0 to 1, for its arrow. */
    public float progress(int row) {
        return (float) progress.get(row) / IncubatorBlockEntity.PROGRESS_SCALE;
    }

    /** mB of experience in the tank, for the gauge. */
    public int experience() {
        return tank.get(0) | tank.get(1) << 15;
    }

    /** How much the tank holds. */
    public long capacity() {
        return TankTier.BASIC.capacity();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.INCUBATOR.get());
    }

    /** Closing the window shuts the lid (once the last viewer has closed it). */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (incubator != null) incubator.stopViewing(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (isSample(stack)) {
            if (!moveItemStackTo(stack, IncubatorBlockEntity.DNA_SLOT, IncubatorBlockEntity.DNA_SLOT + 1, false))
                return ItemStack.EMPTY;
        } else if (isInput(stack)) {
            // Spread over the input slots, a fair share each (dragging in is left as the player puts it).
            if (IncubatorBlockEntity.insert(items, stack, false) == 0) return ItemStack.EMPTY;
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
