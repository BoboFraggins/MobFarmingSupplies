package net.bobofraggins.mobfarmingsupplies.neoforge.tank;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonScreen;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorScreen;
import net.bobofraggins.mobfarmingsupplies.xpanvil.XpAnvilScreen;
import net.bobofraggins.mobfarmingsupplies.disenchanter.DisenchanterRenderer;
import net.bobofraggins.mobfarmingsupplies.disenchanter.DisenchanterScreen;
import net.bobofraggins.mobfarmingsupplies.disenchanter.XpRuneProvider;

public final class TankClientEvents {

    private TankClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.TANK_BE_TYPE.get(), TankRenderer::new);
        event.registerBlockEntityRenderer(Registration.EXPERIENCE_PYLON_BE_TYPE.get(), NeoExperiencePylonRenderer::new);
        event.registerBlockEntityRenderer(Registration.INCUBATOR_BE_TYPE.get(), NeoIncubatorRenderer::new);
        event.registerBlockEntityRenderer(Registration.DISENCHANTER_BE_TYPE.get(), DisenchanterRenderer::new);
        event.registerBlockEntityRenderer(Registration.BLOCK_BREAKER_BE_TYPE.get(),
                net.bobofraggins.mobfarmingsupplies.blockbreaker.BlockBreakerRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(Registration.XP_RUNE.get(), XpRuneProvider::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.TANK_MENU.get(), TankScreen::new);
        event.register(Registration.EXPERIENCE_PYLON_MENU.get(), ExperiencePylonScreen::new);
        event.register(Registration.INCUBATOR_MENU.get(), IncubatorScreen::new);
        event.register(Registration.XP_ANVIL_MENU.get(), XpAnvilScreen::new);
        event.register(Registration.DISENCHANTER_MENU.get(), DisenchanterScreen::new);
    }
}
