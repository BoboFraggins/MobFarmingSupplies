package net.bobofraggins.mobfarmingsupplies.forge.toilet;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.toilet.ToiletBlockEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class ToiletClientEvents {

    private ToiletClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.TOILET_BE_TYPE.get(), ToiletBlockEntityRenderer::new);
        // The seat is invisible: only its rider is drawn.
        event.registerEntityRenderer(Registration.TOILET_SEAT.get(), NoopRenderer::new);
    }
}
