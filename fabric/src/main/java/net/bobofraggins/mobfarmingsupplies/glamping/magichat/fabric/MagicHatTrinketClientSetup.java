package net.bobofraggins.mobfarmingsupplies.glamping.magichat.fabric;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.bobofraggins.mobfarmingsupplies.register.Registration;

/**
 * Trinkets render-layer registration for the Magic Hat — client-only.
 *
 * <p>Split out from {@link MagicHatTrinketSetup} because this class references client-only
 * rendering types ({@link MagicHatTrinketRenderer}, {@code TrinketRendererRegistry}), which a
 * Fabric dedicated server never has on its classpath — see {@code MagicHatTrinketSetup}'s
 * javadoc for the full reasoning. Callers must check
 * {@code Platform.isModLoaded("trinkets")} themselves before ever calling
 * {@link #registerClient()}, same as {@link MagicHatTrinketSetup#registerCommon()}.
 */
public final class MagicHatTrinketClientSetup {

    private MagicHatTrinketClientSetup() {}

    public static void registerClient() {
        TrinketRendererRegistry.registerRenderer(Registration.MAGIC_HAT_ITEM.get(), new MagicHatTrinketRenderer());
    }
}
