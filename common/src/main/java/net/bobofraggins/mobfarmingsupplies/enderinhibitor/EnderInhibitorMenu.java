package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Container menu for the Ender Inhibitor settings screen.
 *
 * <p>Carries no item slots — it exists only to open/close the settings UI and to give
 * the screen access to the block position so it can send offset/area packets.
 */
public class EnderInhibitorMenu extends AbstractContainerMenu {

    private final BlockPos pos;

    // ── Server-side constructor ──────────────────────────────────────────────────

    public EnderInhibitorMenu(int syncId, Inventory playerInv, EnderInhibitorBlockEntity be) {
        super(Registration.ENDER_INHIBITOR_MENU.get(), syncId);
        this.pos = be.getBlockPos();
    }

    // ── Client-side constructor (via FriendlyByteBuf / MenuRegistry.ofExtended) ──

    public EnderInhibitorMenu(int syncId, Inventory playerInv, FriendlyByteBuf buf) {
        super(Registration.ENDER_INHIBITOR_MENU.get(), syncId);
        this.pos = buf.readBlockPos();
    }

    public BlockPos getPos() { return pos; }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
