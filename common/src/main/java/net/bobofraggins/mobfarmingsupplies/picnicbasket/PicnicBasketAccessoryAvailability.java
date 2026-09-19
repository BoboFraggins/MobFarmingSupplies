package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * Whether this platform's accessory-slot mod (Curios on NeoForge, Trinkets Updated on Fabric)
 * is installed.
 *
 * <p>This is a deliberately separate {@code @ExpectPlatform} bridge from
 * {@link PicnicBasketAccessoryPlatform}, even though both are conceptually about the same
 * feature. Architectury's {@code @ExpectPlatform} requires every annotated method of a common
 * class to be implemented together in one matching {@code <platform>.<ClassName>Impl} class
 * (same convention as {@code MGRConfig}/{@code MGRConfigImpl} elsewhere in this codebase) — so
 * if this check lived in the same class as {@code findAccessoryBasket}/{@code
 * writeAccessoryBasket}, calling it would require loading that whole class, which references
 * Curios/Trinkets types in its other methods. Loading a class means verifying <em>all</em> of
 * its bytecode, not just the method being called, so that would throw
 * {@link NoClassDefFoundError} the moment the accessory mod is absent — before this check ever
 * got a chance to say so. Keeping this check in its own class, with its own implementation that
 * touches nothing but a mod-id string, makes it genuinely safe to call unconditionally.
 *
 * <p>See the {@code feedback_soft_dependency_classloading} project memory for the full story —
 * this is the same class of bug that broke the Magic Hat's Curios integration once already.
 */
public final class PicnicBasketAccessoryAvailability {

    private PicnicBasketAccessoryAvailability() {}

    @ExpectPlatform
    public static boolean isLoaded() {
        throw new AssertionError("Missing platform implementation");
    }
}
