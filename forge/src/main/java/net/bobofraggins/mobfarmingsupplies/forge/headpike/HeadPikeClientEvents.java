package net.bobofraggins.mobfarmingsupplies.forge.headpike;

import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class HeadPikeClientEvents {

    private HeadPikeClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.HEAD_PIKE_BE_TYPE.get(), NeoHeadPikeRenderer::new);
    }

}
