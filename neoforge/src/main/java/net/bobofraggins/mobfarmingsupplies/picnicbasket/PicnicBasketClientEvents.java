package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public final class PicnicBasketClientEvents {

    private PicnicBasketClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                Registration.PICNIC_BASKET_BE_TYPE.get(),
                PicnicBasketRenderer::new);
    }

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuScreenRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.PICNIC_BASKET_MENU.get(), PicnicBasketScreen::new);
    }
}
