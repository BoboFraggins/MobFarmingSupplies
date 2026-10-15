package net.bobofraggins.mobfarmingsupplies.neoforge.mobhead;

import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class MobHeadClientEvents {

    private MobHeadClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.MOB_HEAD_BE_TYPE.get(), MobHeadBlockEntityRenderer::new);
    }
}
