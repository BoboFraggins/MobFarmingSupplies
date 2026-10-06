package net.bobofraggins.mobfarmingsupplies.picnicbasket.neoforge;

import dev.architectury.platform.Platform;

/**
 * NeoForge implementation of {@code PicnicBasketAccessoryAvailability}. Deliberately touches
 * nothing but a mod-id string — see that class's javadoc for why it must stay that way.
 */
public final class PicnicBasketAccessoryAvailabilityImpl {

    private PicnicBasketAccessoryAvailabilityImpl() {}

    public static boolean isLoaded() {
        return Platform.isModLoaded("curios");
    }
}
