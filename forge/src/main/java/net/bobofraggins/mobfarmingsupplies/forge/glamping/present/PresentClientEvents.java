package net.bobofraggins.mobfarmingsupplies.forge.glamping.present;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentRenderer;

public final class PresentClientEvents {

    private PresentClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.PRESENT_BE_TYPE.get(), PresentRenderer::new);
    }
}
