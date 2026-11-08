package net.bobofraggins.mobfarmingsupplies.essencehopper;

import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperMenu;
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

/**
 * Menu for the Essence Hopper.
 *
 * <p>Slots: 0 upgrade, 1-27 player inventory, 28-36 hotbar.
 */
public class EssenceHopperMenu extends AbstractContainerMenu {

    /** The upgrade slot, below the title bar and the gap above its pane. */
    private static final int UPGRADE_Y = 25;

    /**
     * Top of the player inventory rows:
     * upgrade row(25 + 18) + blank(8) + offsets(34) + blank(4) + showArea(20) + blank(4) + push(90) + blank(6).
     */
    static final int INV_TOP = 209;
    private static final int HOTBAR_TOP = INV_TOP + 3 * 18 + 4;

    private static final int MACHINE_SLOTS = 1;
    private static final int INV_END = MACHINE_SLOTS + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final BlockPos pos;
    private final ContainerLevelAccess access;

    /** Server side, from {@link EssenceHopperBlockEntity#createMenu}. */
    public EssenceHopperMenu(int syncId, Inventory playerInv, EssenceHopperBlockEntity be) {
        this(syncId, playerInv, be.getBlockPos(), be.getInventory());
    }

    /** Client side: the position comes over the network; the slot is synced in. */
    public EssenceHopperMenu(int syncId, Inventory playerInv, FriendlyByteBuf buf) {
        this(syncId, playerInv, buf.readBlockPos(), new SimpleContainer(MACHINE_SLOTS));
    }

    private EssenceHopperMenu(int syncId, Inventory playerInv, BlockPos pos, Container items) {
        super(Registration.ESSENCE_HOPPER_MENU.get(), syncId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        addSlot(new Slot(items, EssenceHopperBlockEntity.UPGRADE_SLOT, FluidGaugePane.SLOT_X, UPGRADE_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(MFSTags.Items.ESSENCE_FILTERS);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        int left = AbsorptionHopperMenu.PLAYER_SLOT_LEFT;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, left + col * 18, INV_TOP + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, left + col * 18, HOTBAR_TOP));
        }
    }

    public BlockPos getPos() { return pos; }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.ESSENCE_HOPPER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, MACHINE_SLOTS, false)
                && !(index < INV_END
                        ? moveItemStackTo(stack, INV_END, HOTBAR_END, false)
                        : moveItemStackTo(stack, MACHINE_SLOTS, INV_END, false))) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
