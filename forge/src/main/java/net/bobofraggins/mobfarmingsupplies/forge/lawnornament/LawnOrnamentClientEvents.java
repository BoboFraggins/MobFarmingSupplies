package net.bobofraggins.mobfarmingsupplies.forge.lawnornament;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class LawnOrnamentClientEvents {

    private LawnOrnamentClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.GIANT_ORNAMENT_BE_TYPE.get(), NeoGiantOrnamentRenderer::new);
        event.registerBlockEntityRenderer(Registration.SKULL_LIGHT_HOOK_BE_TYPE.get(), NeoSkullLightsRenderer::new);
    }
}
