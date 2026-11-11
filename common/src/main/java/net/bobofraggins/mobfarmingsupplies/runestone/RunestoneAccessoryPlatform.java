package net.bobofraggins.mobfarmingsupplies.runestone;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * Runestones worn as charms: in Curios' charm slot on NeoForge, or this mod's own charm slot in
 * Trinkets Updated on Fabric. Only call this once
 * {@link net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketAccessoryAvailability#isLoaded()}
 * says that mod is there; see that class for why the check can't live here.
 */
public final class RunestoneAccessoryPlatform {

    private RunestoneAccessoryPlatform() {}

    /** Whether {@code player} wears {@code item} in an accessory slot. */
    @ExpectPlatform
    public static boolean isWearing(Player player, Item item) {
        throw new AssertionError("Missing platform implementation");
    }
}
