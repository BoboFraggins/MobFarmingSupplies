package net.bobofraggins.mobfarmingsupplies.neoforge.lawnornament;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class LawnOrnamentClientEvents {

    private LawnOrnamentClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.GIANT_ORNAMENT_BE_TYPE.get(), NeoGiantOrnamentRenderer::new);
        event.registerBlockEntityRenderer(Registration.SKULL_LIGHT_HOOK_BE_TYPE.get(), NeoSkullLightsRenderer::new);
    }
}
