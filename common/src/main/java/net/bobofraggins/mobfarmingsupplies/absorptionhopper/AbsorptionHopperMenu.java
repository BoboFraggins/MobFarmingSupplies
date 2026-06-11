package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * Container menu for the Absorption Hopper.
 *
 * <p>Slot layout:
 * <ul>
 *   <li>[0..15]  Hopper inventory (2 rows × 8 cols, left-aligned)</li>
 *   <li>[16..42] Player main inventory</li>
 *   <li>[43..51] Player hotbar</li>
 * </ul>
 */
public class AbsorptionHopperMenu extends AbstractContainerMenu {

    // ── Layout constants ────────────────────────────────────────────────────────

    /** Left edge of the hopper item slots (and the fluid gauge area). */
    public static final int SLOT_LEFT = 8;

    /**
     * Left edge of the player inventory slots — shifted right to centre the 9-column
     * grid under the combined hopper-slots + gauge area.
     * = SLOT_LEFT + (GAUGE_W + GAUGE_GAP) / 2 = 8 + (15 + 3) / 2 = 17
     */
    public static final int PLAYER_SLOT_LEFT =
            SLOT_LEFT + (HopperSlotsPane.GAUGE_W + HopperSlotsPane.GAUGE_GAP) / 2; // 17

    /**
     * Top of the first hopper-slot row.
     * = Dialog.TITLE_H(17) + top-blank-pane(8) = 25
     */
    public static final int SLOTS_TOP = 25;

    /**
     * Top of the player inventory rows.
     * = Dialog.TITLE_H(17) + blank(8) + slots(54) + blank(8) + offset(34)
     *   + blank(4) + showArea(20) + blank(4) + push(90) + blank(6) = 245
     */
    public static final int INV_TOP = 245;

    /**
     * Top of the player hotbar row.
     * = INV_TOP + 3×18 + hotbarGap(4) = 245 + 54 + 4 = 303
     */
    public static final int HOTBAR_TOP = 303;

    private final BlockPos pos;

    // ── Server-side constructor ──────────────────────────────────────────────────

    public AbsorptionHopperMenu(int syncId, Inventory playerInv, AbsorptionHopperBlockEntity be) {
        super(Registration.ABSORPTION_HOPPER_MENU.get(), syncId);
        this.pos = be.getBlockPos();
        addHopperSlots(be);
        addPlayerSlots(playerInv);
    }

    // ── Client-side constructor (via FriendlyByteBuf / MenuRegistry.ofExtended) ──

    public AbsorptionHopperMenu(int syncId, Inventory playerInv, FriendlyByteBuf buf) {
        super(Registration.ABSORPTION_HOPPER_MENU.get(), syncId);
        this.pos = buf.readBlockPos();
        // On the client, read the BE from the local level for slot backing
        @Nullable
        AbsorptionHopperBlockEntity be = null;
        var level = playerInv.player.level();
        var beRaw = level.getBlockEntity(pos);
        if (beRaw instanceof AbsorptionHopperBlockEntity h) be = h;

        if (be != null) {
            addHopperSlots(be);
        } else {
            // Fallback: add 27 empty dummy slots (shouldn't happen in practice)
            for (int i = 0; i < 27; i++) {
                int col = i % 9;
                int row = i / 9;
                addSlot(new Slot(new net.minecraft.world.SimpleContainer(1), 0,
                        SLOT_LEFT + col * 18, SLOTS_TOP + row * 18));
            }
        }
        addPlayerSlots(playerInv);
    }

    private void addHopperSlots(AbsorptionHopperBlockEntity be) {
        for (int i = 0; i < 27; i++) {
            int col = i % 9;
            int row = i / 9;
            addSlot(new Slot(be.getInventory(), i, SLOT_LEFT + col * 18, SLOTS_TOP + row * 18));
        }
    }

    private void addPlayerSlots(Inventory playerInv) {
        // Main inventory (rows 0–2, slots 9–35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, PLAYER_SLOT_LEFT + col * 18, INV_TOP + row * 18));
            }
        }
        // Hotbar (slots 0–8)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, PLAYER_SLOT_LEFT + col * 18, HOTBAR_TOP));
        }
    }

    public BlockPos getPos() { return pos; }

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
        ItemStack remainder = stack.copy();

        if (index < 27) {
            // Hopper → player inventory
            if (!moveItemStackTo(stack, 27, 63, true)) return ItemStack.EMPTY;
        } else {
            // Player inventory/hotbar → hopper slots
            if (!moveItemStackTo(stack, 0, 27, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == remainder.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return remainder;
    }
}
