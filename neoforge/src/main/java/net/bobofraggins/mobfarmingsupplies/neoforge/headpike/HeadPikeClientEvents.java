package net.bobofraggins.mobfarmingsupplies.neoforge.headpike;

import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class HeadPikeClientEvents {

    private HeadPikeClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.HEAD_PIKE_BE_TYPE.get(), NeoHeadPikeRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.HEAD_PIKE_MENU.get(), HeadPikeScreen::new);
    }
}
