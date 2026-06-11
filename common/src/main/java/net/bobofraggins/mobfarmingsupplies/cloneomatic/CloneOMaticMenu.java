package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import net.bobofraggins.mobfarmingsupplies.dna.IDnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * Container menu for the Clone-O-Matic.
 *
 * <p>Slot layout:
 * <ul>
 *   <li>[0–8]   DNA slots — 3×3 grid, accepts any item for now</li>
 *   <li>[9–35]  Player main inventory</li>
 *   <li>[36–44] Player hotbar</li>
 * </ul>
 *
 * <p>Screen-space Y positions (relative to topPos):
 * <pre>
 *   DNA row:   y = 40  (TITLE_H 17 + blank 8 + pane label section 15)
 *   Inv row 0: y = 68  (17 + 8 + DnaSlotsPane.HEIGHT 35 + 8)
 *   Hotbar:    y = 126 (68 + 3×18 + gap 4)
 * </pre>
 */
public class CloneOMaticMenu extends AbstractContainerMenu {

    // ── Layout constants ─────────────────────────────────────────────────────────

    /** Left margin for DNA slots and player inventory. */
    public static final int SLOT_LEFT = 8;

    /**
     * Screen-space Y of the single DNA slot row.
     * = TITLE_H(17) + blank(8) + pane label section(15) = 40
     */
    public static final int DNA_SLOT_TOP = 40;

    /** Pixel stride between consecutive DNA slots (no gap; slots are 18 px wide). */
    public static final int DNA_SLOT_STRIDE = 18;

    /**
     * Screen-space Y of the first player-inventory row.
     * = TITLE_H(17) + blank(8) + DnaSlotsPane.HEIGHT(35) + blank(8) = 68
     */
    public static final int INV_TOP = 68;

    /** Screen-space Y of the player hotbar. = INV_TOP + 3×18 + gap(4) = 126 */
    public static final int HOTBAR_TOP = INV_TOP + 3 * 18 + 4;

    // ── Fields ───────────────────────────────────────────────────────────────────

    private final BlockPos pos;

    // ── Server-side constructor ──────────────────────────────────────────────────

    public CloneOMaticMenu(int syncId, Inventory playerInv, CloneOMaticBlockEntity be) {
        super(Registration.CLONE_O_MATIC_MENU.get(), syncId);
        this.pos = be.getBlockPos();
        addDnaSlots(be.getDnaSlots());
        addPlayerSlots(playerInv);
    }

    // ── Client-side constructor ──────────────────────────────────────────────────

    public CloneOMaticMenu(int syncId, Inventory playerInv, FriendlyByteBuf buf) {
        super(Registration.CLONE_O_MATIC_MENU.get(), syncId);
        this.pos = buf.readBlockPos();

        @Nullable CloneOMaticBlockEntity be = null;
        var level = playerInv.player.level();
        var beRaw = level.getBlockEntity(pos);
        if (beRaw instanceof CloneOMaticBlockEntity cbe) be = cbe;

        if (be != null) {
            addDnaSlots(be.getDnaSlots());
        } else {
            // Fallback: empty dummy slots (shouldn't happen in practice)
            for (int i = 0; i < CloneOMaticBlockEntity.DNA_SLOTS; i++) {
                addSlot(new Slot(new SimpleContainer(1), 0, 0, 0));
            }
        }
        addPlayerSlots(playerInv);
    }

    // ── Inner slot type ───────────────────────────────────────────────────────────

    /** A slot that accepts only {@link DnaSampleItem} stacks. */
    private static class DnaSampleSlot extends Slot {
        DnaSampleSlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof IDnaSampleItem;
        }
    }

    // ── Slot setup ────────────────────────────────────────────────────────────────

    private void addDnaSlots(SimpleContainer container) {
        for (int i = 0; i < CloneOMaticBlockEntity.DNA_SLOTS; i++) {
            addSlot(new DnaSampleSlot(container, i,
                    SLOT_LEFT + i * DNA_SLOT_STRIDE,
                    DNA_SLOT_TOP));
        }
    }

    private void addPlayerSlots(Inventory playerInv) {
        // Main inventory (rows 0–2)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv,
                        col + row * 9 + 9,
                        SLOT_LEFT + col * 18,
                        INV_TOP + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, SLOT_LEFT + col * 18, HOTBAR_TOP));
        }
    }

    // ── Accessors ────────────────────────────────────────────────────────────────

    public BlockPos getPos() {
        return pos;
    }

    // ── Validity + quick-move ────────────────────────────────────────────────────

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy  = stack.copy();

        int dnaEnd = CloneOMaticBlockEntity.DNA_SLOTS;
        int invEnd  = dnaEnd + 27;

        if (index < dnaEnd) {
            // DNA slot → player inventory / hotbar
            if (!moveItemStackTo(stack, dnaEnd, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Player inventory or hotbar → DNA slots first, then shuffle between inv/hotbar
            if (!moveItemStackTo(stack, 0, dnaEnd, false)) {
                if (index < invEnd) {
                    if (!moveItemStackTo(stack, invEnd, slots.size(), false)) return ItemStack.EMPTY;
                } else {
                    if (!moveItemStackTo(stack, dnaEnd, invEnd, false)) return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }
}
