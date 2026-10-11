package net.bobofraggins.mobfarmingsupplies.neoforge.circuitinscriber;

import net.bobofraggins.mobfarmingsupplies.circuitinscriber.CircuitInscriberRenderer;
import net.bobofraggins.mobfarmingsupplies.circuitinscriber.CircuitInscriberScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.bobofraggins.mobfarmingsupplies.circuitinscriber.CircuitInscriberItemRenderer;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public final class CircuitInscriberClientEvents {

    private CircuitInscriberClientEvents() {}

    // NeoForge's RegisterMenuScreensEvent has already fired by the time the common
    // MenuScreenRegistry.registerScreenFactory call runs, so register here directly.
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.CIRCUIT_INSCRIBER_MENU.get(), CircuitInscriberScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "circuit_inscriber_renderer"),
                CircuitInscriberItemRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.CIRCUIT_INSCRIBER_BE_TYPE.get(), CircuitInscriberRenderer::new);
    }
}
