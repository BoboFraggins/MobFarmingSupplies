package net.bobofraggins.mobfarmingsupplies.neoforge.omnihopper;

import net.bobofraggins.mobfarmingsupplies.bridge.EinsteinRosenBridgeMenu;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperMenu;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class OmniHopperClientEvents {

    private OmniHopperClientEvents() {}

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.OMNI_HOPPER_MENU.get(), OmniHopperScreen<OmniHopperMenu>::new);
        event.register(Registration.BRIDGE_MENU.get(), OmniHopperScreen<EinsteinRosenBridgeMenu>::new);
    }
}
