package net.bobofraggins.mobfarmingsupplies.glamping.present;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@OnlyIn(Dist.CLIENT)
public final class PresentClientEvents {

    private PresentClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.PRESENT_BE_TYPE.get(), PresentRenderer::new);
    }
}
