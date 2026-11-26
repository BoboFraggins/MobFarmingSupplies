package net.bobofraggins.mobfarmingsupplies.forge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommonClient;
import net.bobofraggins.mobfarmingsupplies.client.model.forge.ExtraBlockModelsClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.absorptionhopper.AbsorptionHopperClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.cloneomatic.CloneOMaticClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.enderinhibitor.EnderInhibitorClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.enderinhibitor.NeoForgeEnderInhibitorEvents;
import net.bobofraggins.mobfarmingsupplies.forge.fan.FanClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.filterscribingterminal.FilterScribingTerminalClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.glamping.magichat.MagicHatCurioSetup;
import net.bobofraggins.mobfarmingsupplies.forge.glamping.magichat.MagicHatSpawnEvents;
import net.bobofraggins.mobfarmingsupplies.forge.glamping.present.PresentClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.glamping.present.PresentSpawnEvents;
import net.bobofraggins.mobfarmingsupplies.forge.headpike.HeadPikeClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.headpike.HeadPikeSpawnEvents;
import net.bobofraggins.mobfarmingsupplies.forge.lawnornament.LawnOrnamentClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.logisticsorter.LogisticSorterClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.mobharvester.MobHarvesterClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.mobhead.MobHeadClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.omnihopper.OmniHopperClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.picnicbasket.PicnicBasketClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.register.ForgeOnlyRegistration;
import net.bobofraggins.mobfarmingsupplies.forge.tank.TankClientEvents;
import net.bobofraggins.mobfarmingsupplies.forge.toilet.ToiletClientEvents;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(MobFarmingSuppliesCommon.MODID)
public class MobFarmingSupplies {

    public MobFarmingSupplies() {
        IEventBus modEventBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        // Architectury needs the mod bus before anything is registered through it.
        dev.architectury.platform.forge.EventBuses.registerModEventBus(MobFarmingSuppliesCommon.MODID, modEventBus);
        ForgeOnlyRegistration.register(modEventBus);
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, MFSServerConfig.SPEC);

        // EnderTeleport suppression — Forge-specific (no Architectury equivalent)
        MinecraftForge.EVENT_BUS.register(NeoForgeEnderInhibitorEvents.class);
        // Magic Hat zombie easter egg — Forge has MobSpawnEvent.FinalizeSpawn; Fabric uses a mixin.
        MinecraftForge.EVENT_BUS.register(new MagicHatSpawnEvents());
        // Present enderman easter egg — same split as the Magic Hat.
        MinecraftForge.EVENT_BUS.register(new PresentSpawnEvents());
        MinecraftForge.EVENT_BUS.register(new HeadPikeSpawnEvents());
        // Potions of Flight and Oresight — Forge has a brewing recipe API; Fabric uses mixins.
        modEventBus.addListener(net.bobofraggins.mobfarmingsupplies.forge.potion.MFSBrewingRecipe::register);
        // Magic Hat Curios integration — soft dependency, registered only if Curios is present.
        // MUST check isLoaded() before ever calling into MagicHatCurioSetup: that class
        // references Curios API types, and simply loading it (even just to have this class's
        // own bytecode verified) would throw NoClassDefFoundError without Curios installed —
        // see MagicHatCurioSetup's javadoc.
        boolean curiosLoaded = ModList.get().isLoaded("curios");
        if (curiosLoaded) {
            MagicHatCurioSetup.onCommonSetup(modEventBus);
        }
        // Networking registered in MobFarmingSuppliesCommon.init() via MFSNetwork (Phase 6)

        if (Platform.getEnvironment() == Env.CLIENT) {
            // Keys now: Forge's key registration event fires before client setup.
            MobFarmingSuppliesCommonClient.initKeybinds();
            // Deferred to FMLClientSetupEvent so RegistrySupplier.get() calls are safe (registries committed by then)
            modEventBus.addListener((FMLClientSetupEvent e) -> MobFarmingSuppliesCommonClient.init());
            // Forge-specific client events (renderers, standalone models, fluid rendering, etc.)
            modEventBus.register(EnderInhibitorClientEvents.class);
            MinecraftForge.EVENT_BUS.addListener(net.bobofraggins.mobfarmingsupplies.forge.oresight.OresightClientEvents::onRenderLevel);
            modEventBus.register(FilterScribingTerminalClientEvents.class);
            modEventBus.register(LogisticSorterClientEvents.class);
            modEventBus.register(OmniHopperClientEvents.class);
            modEventBus.register(TankClientEvents.class);
            modEventBus.register(AbsorptionHopperClientEvents.class);
            modEventBus.register(FanClientEvents.class);
            modEventBus.register(CloneOMaticClientEvents.class);
            modEventBus.register(MobHarvesterClientEvents.class);
            modEventBus.register(PicnicBasketClientEvents.class);
            modEventBus.register(ExtraBlockModelsClientEvents.class);
            modEventBus.register(PresentClientEvents.class);
            if (curiosLoaded) {
                MagicHatCurioSetup.onClientSetup(modEventBus);
            }
            modEventBus.register(ToiletClientEvents.class);
            modEventBus.register(LawnOrnamentClientEvents.class);
            modEventBus.register(HeadPikeClientEvents.class);
            modEventBus.register(MobHeadClientEvents.class);
            modEventBus.register(net.bobofraggins.mobfarmingsupplies.forge.client.ItemRendererClientEvents.class);
        }

        MobFarmingSuppliesCommon.init();
    }
}
