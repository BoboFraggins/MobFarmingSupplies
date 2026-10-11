package net.bobofraggins.mobfarmingsupplies.neoforge.stirlingengine;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.stirlingengine.StirlingEngineRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class StirlingEngineClientEvents {

    private StirlingEngineClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.STIRLING_ENGINE_BE_TYPE.get(), StirlingEngineRenderer::new);
    }
}
