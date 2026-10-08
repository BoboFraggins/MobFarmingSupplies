package net.bobofraggins.mobfarmingsupplies.bridge;

import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperMenu;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

/**
 * Menu for the Einstein-Rosen Bridge — the Omnidirectional Hopper's layout. Its filter slots are the
 * channel's shared filters, so every bridge on the channel shows (and edits) the same nine.
 */
public class EinsteinRosenBridgeMenu extends OmniHopperMenu {

    /** Server-side constructor. */
    public EinsteinRosenBridgeMenu(int syncId, Inventory inv, EinsteinRosenBridgeBlockEntity be) {
        super(Registration.BRIDGE_MENU.get(), Registration.EINSTEIN_ROSEN_BRIDGE.get(),
                syncId, inv, be.getBlockPos(), be.getFilters());
    }

    /** Client-side constructor (via MenuRegistry.ofExtended). */
    public EinsteinRosenBridgeMenu(int syncId, Inventory inv, FriendlyByteBuf buf) {
        super(Registration.BRIDGE_MENU.get(), Registration.EINSTEIN_ROSEN_BRIDGE.get(),
                syncId, inv, buf.readBlockPos(), new SimpleContainer(BridgeChannelData.FILTER_SLOTS));
    }
}
