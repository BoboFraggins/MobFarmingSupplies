package net.bobofraggins.mobfarmingsupplies.omnihopper.neoforge;

import com.mojang.logging.LogUtils;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.BlockCapability;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.MekanismChemicals;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.resource.Resource;
import net.minecraft.core.Direction;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Mekanism chemical support for the Omnidirectional Hopper, with Mekanism as an optional mod.
 *
 * <p>Mekanism 10.7 (1.21.1) exposes chemicals as its own {@code IChemicalHandler} on the block
 * capability {@code mekanism:chemical_handler}. {@link MekanismChemicals} sees those through the
 * transfer stand-in's {@code ResourceHandler}, so the hopper moves chemicals with the same code as
 * items and fluids. It refers to Mekanism's API, so it is only touched once Mekanism is known to be
 * installed; if this Mekanism version's API doesn't match, chemicals are simply not moved.
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
        if (!ModList.get().isLoaded("mekanism")) return;
        try {
            // MekanismChemicals refers to Mekanism's API: only touch it once Mekanism is known to be here.
            capability = MekanismChemicals.capability();
            empty = MekanismChemicals.EMPTY;
        } catch (LinkageError | RuntimeException e) {
            capability = null;
            empty = null;
            LOGGER.warn("Omnidirectional Hopper: this Mekanism version's chemical API isn't supported; "
                    + "chemicals won't be moved ({})", e.toString());
        }
    }
}
