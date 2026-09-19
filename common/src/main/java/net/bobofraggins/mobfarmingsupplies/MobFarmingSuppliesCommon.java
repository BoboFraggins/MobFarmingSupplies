package net.bobofraggins.mobfarmingsupplies;

import com.mojang.logging.LogUtils;
import net.bobofraggins.mobfarmingsupplies.dna.DnaCollectorEvents;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorEvents;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatCaptureEvents;
import net.bobofraggins.mobfarmingsupplies.network.MGRNetwork;
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
        MGRNetwork.register();
        PicnicBasketFeedHandler.register();
        LOGGER.info("MobFarmingSupplies initialized");
    }
}
