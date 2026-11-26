package net.bobofraggins.mobfarmingsupplies.forge.fan;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.bobofraggins.mobfarmingsupplies.fan.FanScreen;

public final class FanClientEvents {

    private FanClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.FAN_BE_TYPE.get(),
                FanBlockEntityRendererNeoForge::new);
    }

}
