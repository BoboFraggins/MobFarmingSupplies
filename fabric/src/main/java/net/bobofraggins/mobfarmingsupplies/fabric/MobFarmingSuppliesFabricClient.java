package net.bobofraggins.mobfarmingsupplies.fabric;

import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommonClient;
import net.bobofraggins.mobfarmingsupplies.client.model.fabric.ExtraBlockModelsImpl;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.fabric.MagicHatTrinketClientSetup;
import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentRenderer;
import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeRenderer;
import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantOrnamentRenderer;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterRenderer;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketRenderer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialItemRenderers;
import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightsRenderer;
import net.bobofraggins.mobfarmingsupplies.tank.TankTier;
import net.bobofraggins.mobfarmingsupplies.tank.fabric.TankItemRenderer;
import net.bobofraggins.mobfarmingsupplies.tank.fabric.TankRenderer;
import net.bobofraggins.mobfarmingsupplies.toilet.ToiletBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonRenderer;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorRenderer;
import net.bobofraggins.mobfarmingsupplies.disenchanter.DisenchanterRenderer;
import net.bobofraggins.mobfarmingsupplies.disenchanter.XpRuneProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.item.Item;

public class MobFarmingSuppliesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MobFarmingSuppliesCommonClient.init();
        BlockEntityRendererRegistry.register(
                Registration.ENDER_INHIBITOR_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.WITHER_INHIBITOR_BE_TYPE.get(),
                EnderInhibitorBlockEntityRenderer::wither);
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
                Registration.GIANT_ORNAMENT_BE_TYPE.get(),
                GiantOrnamentRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.SKULL_LIGHT_HOOK_BE_TYPE.get(),
                SkullLightsRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.HEAD_PIKE_BE_TYPE.get(),
                HeadPikeRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.TANK_BE_TYPE.get(),
                TankRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.PICNIC_BASKET_BE_TYPE.get(),
                PicnicBasketRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.PRESENT_BE_TYPE.get(),
                PresentRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.MOB_HEAD_BE_TYPE.get(),
                MobHeadBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.TOILET_BE_TYPE.get(),
                ToiletBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.EXPERIENCE_PYLON_BE_TYPE.get(),
                ExperiencePylonRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.INCUBATOR_BE_TYPE.get(),
                IncubatorRenderer::new);
        BlockEntityRendererRegistry.register(
                Registration.DISENCHANTER_BE_TYPE.get(),
                DisenchanterRenderer::new);
        net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.getInstance()
                .register(Registration.XP_RUNE.get(), XpRuneProvider::new);
        // The toilet seat is invisible: only its rider is drawn.
        EntityRendererRegistry.register(Registration.TOILET_SEAT, NoopRenderer::new);
        ExtraBlockModelsImpl.registerModelLoadingPlugin();
        registerItemRenderers();
        // Magic Hat Trinkets render layer — soft dependency, registered only if
        // present. MUST check isModLoaded() before ever calling into MagicHatTrinketClientSetup:
        // see that class's javadoc (and MagicHatTrinketSetup's) for why.
        if (Platform.isModLoaded("trinkets")) {
            MagicHatTrinketClientSetup.registerClient();
        }
    }

    /**
     * Gives every item drawn in code ({@link SpecialItemRenderers}) its renderer, through Fabric's
     * built-in item renderer registry. The Tanks' renderer is Fabric's own; the rest are common.
     */
    private static void registerItemRenderers() {
        SpecialItemRenderers.register(Registration.TANK_ITEM.get(), new TankItemRenderer(TankTier.BASIC));
        SpecialItemRenderers.register(Registration.GOLD_TANK_ITEM.get(), new TankItemRenderer(TankTier.GOLD));
        SpecialItemRenderers.register(Registration.DIAMOND_TANK_ITEM.get(), new TankItemRenderer(TankTier.DIAMOND));
        SpecialItemRenderers.register(Registration.EMERALD_TANK_ITEM.get(), new TankItemRenderer(TankTier.EMERALD));
        MobFarmingSuppliesCommonClient.registerItemRenderers();
        for (Item item : SpecialItemRenderers.all().keySet()) {
            BuiltinItemRendererRegistry.INSTANCE.register(item, SpecialItemRenderers::render);
        }
    }
}
