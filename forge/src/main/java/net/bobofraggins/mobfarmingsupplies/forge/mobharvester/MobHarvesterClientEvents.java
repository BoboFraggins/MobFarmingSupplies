package net.bobofraggins.mobfarmingsupplies.forge.mobharvester;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterScreen;

public final class MobHarvesterClientEvents {

    private MobHarvesterClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.MOB_HARVESTER_BE_TYPE.get(),
                MobHarvesterRendererNeoForge::new);
    }

}
