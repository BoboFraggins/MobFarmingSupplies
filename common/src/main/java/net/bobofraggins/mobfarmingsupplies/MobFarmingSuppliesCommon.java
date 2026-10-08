package net.bobofraggins.mobfarmingsupplies;

import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.LifecycleEvent;
import net.bobofraggins.mobfarmingsupplies.bridge.BridgeNetworks;
import net.bobofraggins.mobfarmingsupplies.dna.DnaCollectorEvents;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorEvents;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatCaptureEvents;
import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentWrapEvents;
import net.bobofraggins.mobfarmingsupplies.network.MFSNetwork;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketFeedHandler;
import org.slf4j.Logger;

public final class MobFarmingSuppliesCommon {

    public static final String MODID = "mobfarmingsupplies";
    public static final Logger LOGGER = LogUtils.getLogger();

    private MobFarmingSuppliesCommon() {}

    /**
     * Called from both the NeoForge and Fabric entry points after their
     * loader-specific setup (registration, events, config) is complete.
     * Phases 3-9 will progressively move that setup in here.
     */
    public static void init() {
        EnderInhibitorEvents.registerCommonEvents();
        DnaCollectorEvents.registerCommonEvents();
        MagicHatCaptureEvents.registerCommonEvents();
        PresentWrapEvents.registerCommonEvents();
        // Bridge networks hold loaded block entities; never carry them into the next world.
        LifecycleEvent.SERVER_STOPPED.register(server -> BridgeNetworks.clear());
        MFSNetwork.register();
        PicnicBasketFeedHandler.register();
        LOGGER.info("MobFarmingSupplies initialized");
    }
}
