package net.bobofraggins.mobfarmingsupplies.magnet.fabric;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Fabric side of {@code MagnetAccessoryPlatform}: the magnet in a Trinkets slot. Only loaded with it present. */
public final class MagnetAccessoryPlatformImpl {

    private MagnetAccessoryPlatformImpl() {}

    public static ItemStack findWornMagnet(Player player) {
        TrinketComponent trinkets = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinkets == null) return ItemStack.EMPTY;
        for (Tuple<SlotReference, ItemStack> slot : trinkets.getAllEquipped()) {
            if (slot.getB().is(Registration.SUPERCONDUCTING_MAGNET.get())) return slot.getB();
        }
        return ItemStack.EMPTY;
    }

    public static void replaceWornMagnet(Player player, ItemStack stack) {
        TrinketComponent trinkets = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinkets == null) return;
        for (Tuple<SlotReference, ItemStack> slot : trinkets.getAllEquipped()) {
            if (slot.getB().is(Registration.SUPERCONDUCTING_MAGNET.get())) {
                slot.getA().inventory().setItem(slot.getA().index(), stack);
                return;
            }
        }
    }
}
