package net.bobofraggins.mobfarmingsupplies.omnihopper.forge;

import com.mojang.logging.LogUtils;
import net.bobofraggins.mobfarmingsupplies.forge.transfer.api.BlockCapability;
import net.bobofraggins.mobfarmingsupplies.forge.transfer.api.ResourceHandler;
import net.bobofraggins.mobfarmingsupplies.forge.transfer.api.resource.Resource;
import net.minecraft.core.Direction;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Mekanism chemical support for the Omnidirectional Hopper, with Mekanism as an optional mod.
 *
 * <p>Not on 1.20.1 yet: Mekanism 10.4 splits chemicals into gases, infusions, pigments and slurries,
 * each with its own handler, where 10.7 (1.21.1) has one {@code IChemicalHandler} that the newer
 * branches wrap. Until that's done the hopper doesn't move chemicals here, and this reports none.
 */
public final class OmniHopperChemicals {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable private static BlockCapability<ResourceHandler<Resource>, Direction> capability;
    @Nullable private static Resource empty;
    private static boolean initialized;

    private OmniHopperChemicals() {}

    /** The chemical capability, or null when Mekanism isn't installed or isn't compatible. */
    @Nullable
    public static BlockCapability<ResourceHandler<Resource>, Direction> capability() {
        init();
        return capability;
    }

    /** Mekanism's empty chemical, for the hopper's insert-only handler to report. Null with no capability. */
    @Nullable
    static Resource empty() {
        init();
        return empty;
    }

    private static void init() {
        if (initialized) return;
        initialized = true;
        if (ModList.get().isLoaded("mekanism")) {
            LOGGER.info("Omnidirectional Hopper: Mekanism chemicals aren't moved on 1.20.1");
        }
    }
}
