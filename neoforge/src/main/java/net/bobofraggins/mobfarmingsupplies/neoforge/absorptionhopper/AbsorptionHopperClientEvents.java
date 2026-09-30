package net.bobofraggins.mobfarmingsupplies.neoforge.absorptionhopper;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperScreen;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntityRenderer;

public final class AbsorptionHopperClientEvents {

    private AbsorptionHopperClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.ABSORPTION_HOPPER_BE_TYPE.get(),
                AbsorptionHopperBlockEntityRenderer::new);
    }

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuScreenRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.ABSORPTION_HOPPER_MENU.get(), AbsorptionHopperScreen::new);
    }
}
