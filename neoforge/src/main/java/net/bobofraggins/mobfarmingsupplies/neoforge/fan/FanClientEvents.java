package net.bobofraggins.mobfarmingsupplies.neoforge.fan;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.bobofraggins.mobfarmingsupplies.fan.FanScreen;

public final class FanClientEvents {

    private FanClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.FAN_BE_TYPE.get(),
                FanBlockEntityRendererNeoForge::new);
    }

    // FanScreen registration via Architectury's MenuScreenRegistry (deferred to
    // FMLClientSetupEvent in MobFarmingSuppliesCommonClient) never fires on NeoForge —
    // architectury's own RegisterMenuScreensEvent has already run by then. Register
    // directly here, same as AbsorptionHopper/Tank.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.FAN_MENU.get(), FanScreen::new);
    }
}
