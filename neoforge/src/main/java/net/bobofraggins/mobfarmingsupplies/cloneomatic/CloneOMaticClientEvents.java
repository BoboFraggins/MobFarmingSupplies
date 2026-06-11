package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public final class CloneOMaticClientEvents {

    private CloneOMaticClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.CLONE_O_MATIC_BE_TYPE.get(),
                CloneOMaticBlockEntityRenderer::new);
    }

    // CloneOMaticScreen registration via Architectury's MenuScreenRegistry (deferred to
    // FMLClientSetupEvent in MobFarmingSuppliesCommonClient) never fires on NeoForge —
    // architectury's own RegisterMenuScreensEvent has already run by then. Register
    // directly here, same as AbsorptionHopper/Tank.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.CLONE_O_MATIC_MENU.get(), CloneOMaticScreen::new);
    }
}
