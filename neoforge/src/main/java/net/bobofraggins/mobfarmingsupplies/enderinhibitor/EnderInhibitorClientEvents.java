package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public final class EnderInhibitorClientEvents {

    private EnderInhibitorClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.ENDER_INHIBITOR_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::new);
    }

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuScreenRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.ENDER_INHIBITOR_MENU.get(), EnderInhibitorScreen::new);
    }
}
