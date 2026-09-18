package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * Container menu for the Picnic Basket. Works for both the block form (backed by the live
 * block entity's inventory) and the item form (backed by {@link PicnicBasketItemContainer}),
 * since it accepts any 54-slot {@link Container}. Only accepts food items in the basket slots.
 *
 * <p>Slot layout:
 * <ul>
 *   <li>[0..53]  Basket inventory (6 rows × 9 cols)</li>
 *   <li>[54..80] Player main inventory</li>
 *   <li>[81..89] Player hotbar</li>
 * </ul>
 */
public class PicnicBasketMenu extends AbstractContainerMenu {

    public static final int SLOT_COUNT = 54;
    private static final int ROWS = 6;
    private static final int COLS = 9;

    public static final int SLOT_LEFT = 7;
    public static final int SLOTS_TOP = 25;

    /** Same left margin as the basket slots so both grids line up. */
    public static final int PLAYER_SLOT_LEFT = SLOT_LEFT;

    /** = {@link #SLOTS_TOP} + 6 rows × 18 + a 14 px gap below the basket inventory. */
    public static final int INV_TOP = SLOTS_TOP + ROWS * 18 + 14;
    public static final int HOTBAR_TOP = INV_TOP + 58;

    private final Container container;
    @Nullable private final BlockPos pos;

    /** Server-side constructor for the block form. */
    public PicnicBasketMenu(int syncId, Inventory playerInv, Container container, @Nullable BlockPos pos) {
        super(Registration.PICNIC_BASKET_MENU.get(), syncId);
        this.container = container;
        this.pos = pos;
        addBasketSlots();
        addPlayerSlots(playerInv);
    }

    /** Client-side constructor (via {@link dev.architectury.registry.menu.MenuRegistry}). */
    public PicnicBasketMenu(int syncId, Inventory playerInv, FriendlyByteBuf buf) {
        this(syncId, playerInv, new SimpleContainer(SLOT_COUNT), buf.readBoolean() ? buf.readBlockPos() : null);
    }

    private void addBasketSlots() {
        for (int i = 0; i < ROWS * COLS; i++) {
            int col = i % COLS;
            int row = i / COLS;
            addSlot(new FoodSlot(container, i, SLOT_LEFT + col * 18, SLOTS_TOP + row * 18));
        }
    }

    /** Only accepts items with a {@link DataComponents#FOOD} component — matches the source mod. */
    private static class FoodSlot extends Slot {
        FoodSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.has(DataComponents.FOOD);
        }
    }

    private void addPlayerSlots(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, PLAYER_SLOT_LEFT + col * 18, INV_TOP + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, PLAYER_SLOT_LEFT + col * 18, HOTBAR_TOP));
        }
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────────

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide() && pos != null
                && player.level().getBlockEntity(pos) instanceof PicnicBasketBlockEntity be) {
            be.stopOpen(player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // ── Shift-click ──────────────────────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack remainder = stack.copy();

        if (index < SLOT_COUNT) {
            if (!moveItemStackTo(stack, SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, SLOT_COUNT, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == remainder.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return remainder;
    }
}
