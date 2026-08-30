package net.bobofraggins.mobfarmingsupplies.fabric;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import java.lang.reflect.Field;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommonClient;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.client.model.fabric.ExtraBlockModelsImpl;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.fabric.TankItemRenderer;
import net.bobofraggins.mobfarmingsupplies.tank.fabric.TankRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public class MobFarmingSuppliesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MobFarmingSuppliesCommonClient.init();
        BlockEntityRendererRegistry.register(
                Registration.ENDER_INHIBITOR_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.FAN_BE_TYPE.get(),
                FanBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.MOB_HARVESTER_BE_TYPE.get(),
                MobHarvesterRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.CLONE_O_MATIC_BE_TYPE.get(),
                CloneOMaticBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.TANK_BE_TYPE.get(),
                TankRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.ABSORPTION_HOPPER_BE_TYPE.get(),
                AbsorptionHopperBlockEntityRenderer::new);
        ExtraBlockModelsImpl.registerModelLoadingPlugin();
        registerSpecialModelRenderers();
    }

    /**
     * Registers {@code mobfarmingsupplies:tank_renderer} (referenced by {@code items/tank.json})
     * against vanilla's {@code SpecialModelRenderers.ID_MAPPER}.
     *
     * <p>NeoForge has {@code RegisterSpecialModelRendererEvent} for this; Fabric has no
     * equivalent API, so we reach the (private but late-bound, live-map-backed) ID_MAPPER
     * field via reflection. Must run before the first model bake / resource reload, which
     * {@code onInitializeClient()} satisfies.
     */
    @SuppressWarnings("unchecked")
    private static void registerSpecialModelRenderers() {
        try {
            Field idMapperField = SpecialModelRenderers.class.getDeclaredField("ID_MAPPER");
            idMapperField.setAccessible(true);
            ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpecialModelRenderer.Unbaked<?>>> idMapper =
                    (ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpecialModelRenderer.Unbaked<?>>>)
                            idMapperField.get(null);
            idMapper.put(
                    Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "tank_renderer"),
                    TankItemRenderer.Unbaked.MAP_CODEC);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register mobfarmingsupplies:tank_renderer special model renderer", e);
        }
    }
}
