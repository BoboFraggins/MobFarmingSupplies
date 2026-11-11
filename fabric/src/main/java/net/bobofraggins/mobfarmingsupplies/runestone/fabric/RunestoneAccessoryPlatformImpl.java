package net.bobofraggins.mobfarmingsupplies.runestone.fabric;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/** Fabric side of {@code RunestoneAccessoryPlatform}: runestones in a Trinkets Updated slot. Only loaded with it present. */
public final class RunestoneAccessoryPlatformImpl {

    private RunestoneAccessoryPlatformImpl() {}

    public static boolean isWearing(Player player, Item item) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        if (attachment == null) return false;
        for (TrinketSlotAccess slot : attachment.allEquipped(false)) {
            if (slot.get().is(item)) return true;
        }
        return false;
    }
}
