package net.bobofraggins.mobfarmingsupplies.forge.mobhead;

import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class MobHeadClientEvents {

    private MobHeadClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.MOB_HEAD_BE_TYPE.get(), MobHeadBlockEntityRenderer::new);
    }
}
