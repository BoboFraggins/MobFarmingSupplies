package net.bobofraggins.mobfarmingsupplies.forge.enderinhibitor;

import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class EnderInhibitorClientEvents {

    private EnderInhibitorClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.ENDER_INHIBITOR_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                Registration.WITHER_INHIBITOR_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::wither);
        event.registerBlockEntityRenderer(
                Registration.WARDEN_INHIBITOR_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::warden);
        event.registerBlockEntityRenderer(
                Registration.MUFFLER_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::muffler);
    }

}
