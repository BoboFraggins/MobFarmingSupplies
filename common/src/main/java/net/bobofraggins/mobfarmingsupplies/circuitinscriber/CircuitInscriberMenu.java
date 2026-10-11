package net.bobofraggins.mobfarmingsupplies.circuitinscriber;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The Circuit Inscriber's menu: its two inputs and output, then the player's inventory. Progress,
 * recipe time and energy come through a {@link ContainerData}.
 *
 * <p>Screen-space slot positions (relative to the screen's top-left):
 * <pre>
 *   inputs: x 56, y 29 and 55       output: x 116, y 42
 *   inventory rows from y 87, hotbar y 145
 * </pre>
 */
public class CircuitInscriberMenu extends AbstractContainerMenu {

    /** Top of the inscriber pane: the title bar (17) and a blank gap (8). */
    public static final int PANE_TOP = 25;
    public static final int INPUT_X = 56;
    public static final int FIRST_Y = PANE_TOP + 4;
    public static final int SECOND_Y = PANE_TOP + 30;
    public static final int OUTPUT_X = 116;
    public static final int OUTPUT_Y = PANE_TOP + 17;
    public static final int SLOT_LEFT = 8;
    /** Top of the inventory: the pane (54) and another gap (8) below it. */
    public static final int INV_TOP = PANE_TOP + CircuitInscriberPane.HEIGHT + 8;
    public static final int HOTBAR_TOP = INV_TOP + 3 * 18 + 4;

    private final Container container;
    private final ContainerData data;
    private final BlockPos pos;

    public CircuitInscriberMenu(int syncId, Inventory inventory, CircuitInscriberBlockEntity be, ContainerData data) {
        super(Registration.CIRCUIT_INSCRIBER_MENU.get(), syncId);
        this.container = be;
        this.data = data;
        this.pos = be.getBlockPos();
        addSlots(inventory);
    }

    /** Client side: the slots mirror the server's through the menu, so any container will do. */
    public CircuitInscriberMenu(int syncId, Inventory inventory, FriendlyByteBuf buf) {
        super(Registration.CIRCUIT_INSCRIBER_MENU.get(), syncId);
        this.container = new SimpleContainer(3);
        this.data = new SimpleContainerData(4);
        this.pos = buf.readBlockPos();
        addSlots(inventory);
    }

    private void addSlots(Inventory inventory) {
        addSlot(new Slot(container, CircuitInscriberBlockEntity.FIRST, INPUT_X, FIRST_Y));
        addSlot(new Slot(container, CircuitInscriberBlockEntity.SECOND, INPUT_X, SECOND_Y));
        addSlot(new Slot(container, CircuitInscriberBlockEntity.OUTPUT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, SLOT_LEFT + col * 18, INV_TOP + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, SLOT_LEFT + col * 18, HOTBAR_TOP));
        }
        addDataSlots(data);
    }

    public BlockPos pos() {
        return pos;
    }

    /** Fraction of the current craft done, 0 to 1. */
    public float progress() {
        int time = data.get(1);
        return time <= 0 ? 0f : Math.min(1f, data.get(0) / (float) time);
    }

    public long energy() {
        return (data.get(2) & 0xFFFFL) | ((data.get(3) & 0xFFFFL) << 16);
    }

    public long capacity() {
        return CircuitInscriberBlockEntity.CAPACITY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int machineEnd = 3;
        int invEnd = machineEnd + 27;
        if (index < machineEnd) {
            if (!moveItemStackTo(stack, machineEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 2, false)) {
            // Into the first input, then the second; otherwise between inventory and hotbar.
            if (index < invEnd) {
                if (!moveItemStackTo(stack, invEnd, slots.size(), false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, machineEnd, invEnd, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
