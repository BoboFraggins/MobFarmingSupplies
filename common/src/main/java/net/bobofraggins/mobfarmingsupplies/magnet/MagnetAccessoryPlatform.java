package net.bobofraggins.mobfarmingsupplies.magnet;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A Superconducting Magnet worn as a charm: in Curios' charm slot on NeoForge, or this mod's own
 * charm slot in Trinkets Updated on Fabric. Only call these once
 * {@link net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketAccessoryAvailability#isLoaded()}
 * says that mod is there; see that class for why the check can't live here.
 */
public final class MagnetAccessoryPlatform {

    private MagnetAccessoryPlatform() {}

    /** The first worn magnet, or {@link ItemStack#EMPTY}. */
    @ExpectPlatform
    public static ItemStack findWornMagnet(Player player) {
        throw new AssertionError("Missing platform implementation");
    }

    /** Puts {@code stack} in place of the first worn magnet. */
    @ExpectPlatform
    public static void replaceWornMagnet(Player player, ItemStack stack) {
        throw new AssertionError("Missing platform implementation");
    }
}
