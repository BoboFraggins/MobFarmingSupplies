package net.bobofraggins.mobfarmingsupplies.xpanvil;

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
 * Menu for the XP Powered Anvil: an input slot (anything with durability), an output slot, the
 * repair progress for the arrow and the tank's amount for the gauge.
 *
 * <p>Slots: 0 input, 1 output, 2-28 player inventory, 29-37 hotbar.
 */
public class XpAnvilMenu extends AbstractContainerMenu {

    public static final int GAUGE_X = 16;
    public static final int GAUGE_Y = 20;
    public static final int GAUGE_W = 14;
    public static final int GAUGE_H = 40;
    public static final int INPUT_X = 52;
    public static final int OUTPUT_X = 108;
    public static final int ROW_Y = GAUGE_Y + (GAUGE_H - 16) / 2;
    /** The progress arrow (vanilla's furnace arrow, 24 wide), centred between the slots. */
    public static final int ARROW_X = (INPUT_X + 18 + OUTPUT_X - 24) / 2;

    public static final int SETTINGS_PANE_H = GAUGE_Y + GAUGE_H + 5 - 17;
    public static final int INV_START_X = 7;
    public static final int INV_Y = 17 + SETTINGS_PANE_H;
    public static final int HOTBAR_Y = INV_Y + 3 * 18 + 4;

    private static final int MACHINE_SLOTS = 2;
    private static final int INV_END = MACHINE_SLOTS + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** Server side, from {@link XpAnvilBlockEntity#createMenu}. */
    public XpAnvilMenu(int windowId, Inventory inv, XpAnvilBlockEntity anvil) {
        this(windowId, inv, anvil.getBlockPos(), anvil.items, anvil.data);
    }

    /** Client side: the position comes over the network; the slots and data are synced in. */
    public XpAnvilMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(windowId, inv, buf.readBlockPos(), new SimpleContainer(2), new SimpleContainerData(3));
    }

    private XpAnvilMenu(int windowId, Inventory inv, BlockPos pos, Container items, ContainerData data) {
        super(Registration.XP_ANVIL_MENU.get(), windowId);
        this.data = data;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);

        addSlot(new Slot(items, XpAnvilBlockEntity.INPUT, INPUT_X, ROW_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isRepairable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addSlot(new Slot(items, XpAnvilBlockEntity.OUTPUT, OUTPUT_X, ROW_Y) {
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

    /** Anything with durability. */
    public static boolean isRepairable(ItemStack stack) {
        return stack.isDamageableItem();
    }

    /** How whole the item being repaired is, 0 to 1, for the arrow. */
    public float progress() {
        return (float) data.get(0) / XpAnvilBlockEntity.PROGRESS_SCALE;
    }

    /** mB of experience in the tank, for the gauge. */
    public int experience() {
        return data.get(1) | data.get(2) << 15;
    }

    public long capacity() {
        return XpAnvilBlockEntity.CAPACITY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.XP_ANVIL.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (isRepairable(stack)) {
            if (!moveItemStackTo(stack, XpAnvilBlockEntity.INPUT, XpAnvilBlockEntity.INPUT + 1, false))
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
