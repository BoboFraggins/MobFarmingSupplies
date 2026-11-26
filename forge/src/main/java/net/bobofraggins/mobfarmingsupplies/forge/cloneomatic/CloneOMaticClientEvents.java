package net.bobofraggins.mobfarmingsupplies.forge.cloneomatic;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticScreen;

public final class CloneOMaticClientEvents {

    private CloneOMaticClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.CLONE_O_MATIC_BE_TYPE.get(),
                CloneOMaticBlockEntityRenderer::new);
    }

}
