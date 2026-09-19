package net.bobofraggins.mobfarmingsupplies;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperClientEvents;
import net.bobofraggins.mobfarmingsupplies.client.model.neoforge.ExtraBlockModelsClientEvents;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticClientEvents;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorClientEvents;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.NeoForgeEnderInhibitorEvents;
import net.bobofraggins.mobfarmingsupplies.fan.FanClientEvents;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatClientEvents;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatCurioSetup;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatSpawnEvents;
import net.bobofraggins.mobfarmingsupplies.mobharvester.BeheadingDropHandler;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterClientEvents;
import net.bobofraggins.mobfarmingsupplies.register.NeoForgeOnlyRegistration;
import net.bobofraggins.mobfarmingsupplies.tank.TankClientEvents;
import net.bobofraggins.mobfarmingsupplies.xpjuice.XpJuiceClientEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MobFarmingSuppliesCommon.MODID)
public class MobFarmingSupplies {

    public MobFarmingSupplies(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgeOnlyRegistration.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, MGRServerConfig.SPEC);

        // EnderTeleport suppression — NeoForge-specific (no Architectury equivalent)
        NeoForge.EVENT_BUS.register(NeoForgeEnderInhibitorEvents.class);
        // Beheading drops — stays NeoForge until HarvesterFakePlayer is abstracted (Phase 5)
        NeoForge.EVENT_BUS.register(new BeheadingDropHandler());
        // Magic Hat zombie easter egg — NeoForge has FinalizeSpawnEvent; Fabric uses a mixin.
        NeoForge.EVENT_BUS.register(new MagicHatSpawnEvents());
        // Magic Hat Curios integration — soft dependency, registered only if Curios is present.
        // MUST check isLoaded() before ever calling into MagicHatCurioSetup: that class
        // references Curios API types, and simply loading it (even just to have this class's
        // own bytecode verified) would throw NoClassDefFoundError without Curios installed —
        // see MagicHatCurioSetup's javadoc.
        boolean curiosLoaded = ModList.get().isLoaded("curios");
        if (curiosLoaded) {
            MagicHatCurioSetup.onCommonSetup(modEventBus);
        }
        // Networking registered in MobFarmingSuppliesCommon.init() via MGRNetwork (Phase 6)

        if (Platform.getEnvironment() == Env.CLIENT) {
            // Deferred to FMLClientSetupEvent so RegistrySupplier.get() calls are safe (registries committed by then)
            modEventBus.addListener((FMLClientSetupEvent e) -> MobFarmingSuppliesCommonClient.init());
            // NeoForge-specific client events (renderers, standalone models, NeoForge fluid rendering, etc.)
            modEventBus.register(EnderInhibitorClientEvents.class);
            modEventBus.register(TankClientEvents.class);
            modEventBus.register(XpJuiceClientEvents.class);
            modEventBus.register(AbsorptionHopperClientEvents.class);
            modEventBus.register(FanClientEvents.class);
            modEventBus.register(CloneOMaticClientEvents.class);
            modEventBus.register(MobHarvesterClientEvents.class);
            modEventBus.register(ExtraBlockModelsClientEvents.class);
            modEventBus.register(MagicHatClientEvents.class);
            if (curiosLoaded) {
                MagicHatCurioSetup.onClientSetup(modEventBus);
            }
        }

        MobFarmingSuppliesCommon.init();
    }
}
