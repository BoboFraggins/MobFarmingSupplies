package net.bobofraggins.mobfarmingsupplies.neoforge.gravitationalanomaly;

import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.gravitationalanomaly.GravitationalAnomalyItemRenderer;
import net.bobofraggins.mobfarmingsupplies.gravitationalanomaly.GravitationalAnomalyRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.bobofraggins.mobfarmingsupplies.gravitationalanomaly.AnomalyParticles;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public final class GravitationalAnomalyClientEvents {

    private GravitationalAnomalyClientEvents() {}

    @SubscribeEvent
    public static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "gravitational_anomaly_renderer"),
                GravitationalAnomalyItemRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(Registration.ACCRETION_PARTICLE.get(), AnomalyParticles.AccretionProvider::new);
        event.registerSpriteSet(Registration.JET_PARTICLE.get(), AnomalyParticles.JetProvider::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.GRAVITATIONAL_ANOMALY_BE_TYPE.get(),
                GravitationalAnomalyRenderer::new);
    }
}
