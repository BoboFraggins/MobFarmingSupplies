package net.bobofraggins.mobfarmingsupplies.mobharvester;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public final class MobHarvesterClientEvents {

    private MobHarvesterClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.MOB_HARVESTER_BE_TYPE.get(),
                MobHarvesterRendererNeoForge::new);
    }

    // MobHarvesterScreen registration via Architectury's MenuScreenRegistry (deferred to
    // FMLClientSetupEvent in MobFarmingSuppliesCommonClient) never fires on NeoForge —
    // architectury's own RegisterMenuScreensEvent has already run by then. Register
    // directly here, same as AbsorptionHopper/Tank.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.MOB_HARVESTER_MENU.get(), MobHarvesterScreen::new);
    }
}
