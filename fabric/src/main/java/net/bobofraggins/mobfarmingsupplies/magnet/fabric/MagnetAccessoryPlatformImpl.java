package net.bobofraggins.mobfarmingsupplies.magnet.fabric;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Fabric side of {@code MagnetAccessoryPlatform}: the magnet in a Trinkets Updated slot. Only loaded with it present. */
public final class MagnetAccessoryPlatformImpl {

    private MagnetAccessoryPlatformImpl() {}

    public static ItemStack findWornMagnet(Player player) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        if (attachment == null) return ItemStack.EMPTY;
        for (TrinketSlotAccess slot : attachment.allEquipped(false)) {
            if (slot.get().is(Registration.SUPERCONDUCTING_MAGNET.get())) return slot.get();
        }
        return ItemStack.EMPTY;
    }

    public static void replaceWornMagnet(Player player, ItemStack stack) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        if (attachment == null) return;
        for (TrinketSlotAccess slot : attachment.allEquipped(false)) {
            if (slot.get().is(Registration.SUPERCONDUCTING_MAGNET.get())) {
                slot.set(stack);
                return;
            }
        }
    }
}
