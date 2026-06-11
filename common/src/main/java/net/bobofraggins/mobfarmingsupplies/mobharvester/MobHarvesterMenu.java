package net.bobofraggins.mobfarmingsupplies.mobharvester;

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
 * Container menu for the Mob Harvester.
 *
 * <p>Slot layout:
 * <ul>
 *   <li>[0]   Sharpness upgrade slot — accepts only {@link HarvesterUpgradeItem} of type SHARPNESS</li>
 *   <li>[1]   Looting upgrade slot   — accepts only {@link HarvesterUpgradeItem} of type LOOTING</li>
 *   <li>[2]   Beheading upgrade slot — accepts only {@link HarvesterUpgradeItem} of type BEHEADING</li>
 *   <li>[3–29]  Player main inventory</li>
 *   <li>[30–38] Player hotbar</li>
 * </ul>
 *
 * <p>The three upgrade slots are centered horizontally in the dialog.
 */
public class MobHarvesterMenu extends AbstractContainerMenu {

    // ── Layout constants ──────────────────────────────────────────────────────────

    /** Left margin used for player inventory slots. */
    public static final int SLOT_LEFT = 8;

    /**
     * X position of the first upgrade slot (centered in the 176 px-wide dialog).
     * (176 − 3 × 18) / 2 = 61
     */
    public static final int UPGRADE_X_START = (176 - 3 * 18) / 2; // 61

    /** Y position of the upgrade slots within the dialog (title + gap + header + pane-pad). */
    public static final int UPGRADE_TOP = 41; // 17 title + 8 gap + 10 header + 6 pane-pad

    /**
     * Y position of the player inventory.
     * title(17) + gap(8) + pane(36) + gap(8) = 69
     */
    public static final int INV_TOP = 69;

    /** Y position of the hotbar. */
    public static final int HOTBAR_TOP = INV_TOP + 3 * 18 + 4; // 117

    // ── Fields ────────────────────────────────────────────────────────────────────

    private final BlockPos pos;

    // ── Server-side constructor ───────────────────────────────────────────────────

    public MobHarvesterMenu(int syncId, Inventory playerInv, MobHarvesterBlockEntity be) {
        super(Registration.MOB_HARVESTER_MENU.get(), syncId);
        this.pos = be.getBlockPos();
        addUpgradeSlots(be.getUpgrades());
        addPlayerSlots(playerInv);
    }

    // ── Client-side constructor ───────────────────────────────────────────────────

    public MobHarvesterMenu(int syncId, Inventory playerInv, FriendlyByteBuf buf) {
        super(Registration.MOB_HARVESTER_MENU.get(), syncId);
        this.pos = buf.readBlockPos();

        @Nullable MobHarvesterBlockEntity be = null;
        var level  = playerInv.player.level();
        var beRaw  = level.getBlockEntity(pos);
        if (beRaw instanceof MobHarvesterBlockEntity m) be = m;

        if (be != null) {
            addUpgradeSlots(be.getUpgrades());
        } else {
            for (int i = 0; i < MobHarvesterBlockEntity.UPGRADE_SLOTS; i++) {
                addSlot(new Slot(new SimpleContainer(1), 0, 0, 0));
            }
        }
        addPlayerSlots(playerInv);
    }

    // ── Slot setup ────────────────────────────────────────────────────────────────

    private void addUpgradeSlots(SimpleContainer container) {
        HarvesterUpgradeItem.UpgradeType[] types = HarvesterUpgradeItem.UpgradeType.values();
        for (int i = 0; i < types.length; i++) {
            addSlot(new HarvesterUpgradeSlot(container, i, types[i],
                    UPGRADE_X_START + i * 18,
                    UPGRADE_TOP));
        }
    }

    private void addPlayerSlots(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv,
                        col + row * 9 + 9,
                        SLOT_LEFT + col * 18,
                        INV_TOP + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, SLOT_LEFT + col * 18, HOTBAR_TOP));
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────────

    public BlockPos getPos() { return pos; }

    // ── Validity + quick-move ─────────────────────────────────────────────────────

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

        int upgradeEnd = MobHarvesterBlockEntity.UPGRADE_SLOTS;
        int invEnd     = upgradeEnd + 27;

        if (index < upgradeEnd) {
            if (!moveItemStackTo(stack, upgradeEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, upgradeEnd, false)) {
                if (index < invEnd) {
                    if (!moveItemStackTo(stack, invEnd, slots.size(), false)) return ItemStack.EMPTY;
                } else {
                    if (!moveItemStackTo(stack, upgradeEnd, invEnd, false)) return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    // ── Typed upgrade slot ────────────────────────────────────────────────────────

    /**
     * A slot that only accepts {@link HarvesterUpgradeItem} of the specified type,
     * capped at {@link HarvesterUpgradeItem.UpgradeType#maxStack}.
     */
    private static class HarvesterUpgradeSlot extends Slot {

        private final HarvesterUpgradeItem.UpgradeType type;

        HarvesterUpgradeSlot(SimpleContainer container, int slotIndex,
                          HarvesterUpgradeItem.UpgradeType type, int x, int y) {
            super(container, slotIndex, x, y);
            this.type = type;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof HarvesterUpgradeItem upgrade
                    && upgrade.upgradeType == type;
        }

        @Override
        public int getMaxStackSize() {
            return type.maxStack;
        }
    }
}
