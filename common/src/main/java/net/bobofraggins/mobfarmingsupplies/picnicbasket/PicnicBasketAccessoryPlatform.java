package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Cross-platform access to a worn Picnic Basket in an accessory-slot mod.
 *
 * <p>NeoForge implementation: Curios ({@code top.theillusivec4.curios}), a soft dependency.
 * Fabric implementation: Trinkets Updated ({@code eu.pb4.trinkets}), a soft dependency.
 * Both implementations must guard against the accessory mod being absent and return
 * {@link ItemStack#EMPTY} / no-op in that case.
 */
public final class PicnicBasketAccessoryPlatform {

    private PicnicBasketAccessoryPlatform() {}

    /**
     * Callers MUST check {@link PicnicBasketAccessoryAvailability#isLoaded()} before ever
     * calling {@link #findAccessoryBasket} or {@link #writeAccessoryBasket} — see that class's
     * javadoc for why the check has to live in a completely separate class from these methods.
     */

    /** Returns the first worn Picnic Basket found, or {@link ItemStack#EMPTY} if none. */
    @ExpectPlatform
    public static ItemStack findAccessoryBasket(Player player) {
        throw new AssertionError("Missing platform implementation");
    }

    /** Writes {@code stack} back into the accessory slot currently holding the worn basket. */
    @ExpectPlatform
    public static void writeAccessoryBasket(Player player, ItemStack stack) {
        throw new AssertionError("Missing platform implementation");
    }
}
