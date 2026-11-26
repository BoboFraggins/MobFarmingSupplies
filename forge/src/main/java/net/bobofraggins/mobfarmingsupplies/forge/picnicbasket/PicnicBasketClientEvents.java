package net.bobofraggins.mobfarmingsupplies.forge.picnicbasket;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketRenderer;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketScreen;

public final class PicnicBasketClientEvents {

    private PicnicBasketClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.PICNIC_BASKET_BE_TYPE.get(),
                PicnicBasketRenderer::new);
    }

}
