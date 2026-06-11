package net.bobofraggins.mobfarmingsupplies.fan;

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
 * Container menu for the Fan.
 *
 * <p>Slot layout:
 * <ul>
 *   <li>[0]     Width upgrade slot  — accepts only {@code fan_upgrade_width}</li>
 *   <li>[1]     Height upgrade slot — accepts only {@code fan_upgrade_height}</li>
 *   <li>[2]     Distance upgrade slot  — accepts only {@code fan_upgrade_distance}</li>
 *   <li>[3..29] Player main inventory</li>
 *   <li>[30..38] Player hotbar</li>
 * </ul>
 */
public class FanMenu extends AbstractContainerMenu {

    // ── Layout constants (screen-space y positions) ─────────────────────────────

    /** Left margin used for player inventory slots. */
    public static final int SLOT_LEFT = 8;

    /**
     * X position of the first upgrade slot (centered in the 176 px-wide dialog).
     * (176 − 3 × 18) / 2 = 61
     */
    public static final int UPGRADE_X_START = (176 - 3 * 18) / 2; // 61

    /**
     * Y position of the upgrade slots in screen space.
     * = TITLE_H(17) + blank(8) + header(10) + pane_internal_top(6) = 41
     */
    public static final int UPGRADE_TOP = 41;

    /**
     * Y position of the first player inventory row in screen space.
     * = TITLE_H(17) + blank(8) + upgrades(36) + blank(4) + showArea(20) + blank(12) = 97
     */
    public static final int INV_TOP = 97;

    /** Y position of the player hotbar row in screen space (INV_TOP + 3×18 + gap4). */
    public static final int HOTBAR_TOP = INV_TOP + 3 * 18 + 4;

    private final BlockPos pos;

    // ── Server-side constructor ──────────────────────────────────────────────────

    public FanMenu(int syncId, Inventory playerInv, FanBlockEntity be) {
        super(Registration.FAN_MENU.get(), syncId);
        this.pos = be.getBlockPos();
        addUpgradeSlots(be.getUpgrades());
        addPlayerSlots(playerInv);
    }

    // ── Client-side constructor (via FriendlyByteBuf / MenuRegistry.ofExtended) ──

    public FanMenu(int syncId, Inventory playerInv, FriendlyByteBuf buf) {
        super(Registration.FAN_MENU.get(), syncId);
        this.pos = buf.readBlockPos();

        @Nullable FanBlockEntity be = null;
        var level = playerInv.player.level();
        var beRaw = level.getBlockEntity(pos);
        if (beRaw instanceof FanBlockEntity f) be = f;

        if (be != null) {
            addUpgradeSlots(be.getUpgrades());
        } else {
            // Fallback: empty dummy slots (shouldn't happen in practice)
            for (int i = 0; i < FanBlockEntity.UPGRADE_SLOTS; i++) {
                addSlot(new Slot(new SimpleContainer(1), 0, 0, 0));
            }
        }
        addPlayerSlots(playerInv);
    }

    private void addUpgradeSlots(SimpleContainer upgrades) {
        addSlot(new RestrictedSlot(upgrades, 0, UPGRADE_X_START,      UPGRADE_TOP, Registration.FAN_UPGRADE_WIDTH.get()));
        addSlot(new RestrictedSlot(upgrades, 1, UPGRADE_X_START + 18, UPGRADE_TOP, Registration.FAN_UPGRADE_HEIGHT.get()));
        addSlot(new RestrictedSlot(upgrades, 2, UPGRADE_X_START + 36, UPGRADE_TOP, Registration.FAN_UPGRADE_DISTANCE.get()));
    }

    private void addPlayerSlots(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, SLOT_LEFT + col * 18, INV_TOP + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, SLOT_LEFT + col * 18, HOTBAR_TOP));
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
        ItemStack copy  = stack.copy();

        if (index < FanBlockEntity.UPGRADE_SLOTS) {
            // Upgrade slot → player inventory/hotbar
            if (!moveItemStackTo(stack, FanBlockEntity.UPGRADE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Player inventory or hotbar → upgrade slots
            if (!moveItemStackTo(stack, 0, FanBlockEntity.UPGRADE_SLOTS, false)) {
                // Didn't fit in upgrade slots — shift between inv/hotbar
                int invEnd = FanBlockEntity.UPGRADE_SLOTS + 27;
                if (index < invEnd) {
                    if (!moveItemStackTo(stack, invEnd, slots.size(), false)) return ItemStack.EMPTY;
                } else {
                    if (!moveItemStackTo(stack, FanBlockEntity.UPGRADE_SLOTS, invEnd, false)) return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    // ── Restricted slot ──────────────────────────────────────────────────────────

    /**
     * A slot that only accepts a specific item type.
     */
    private static class RestrictedSlot extends Slot {

        private final net.minecraft.world.item.Item requiredItem;

        RestrictedSlot(SimpleContainer container, int slotIndex, int x, int y,
                       net.minecraft.world.item.Item requiredItem) {
            super(container, slotIndex, x, y);
            this.requiredItem = requiredItem;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(requiredItem);
        }

        @Override
        public int getMaxStackSize() {
            return FanBlockEntity.MAX_UPGRADES;
        }
    }
}
