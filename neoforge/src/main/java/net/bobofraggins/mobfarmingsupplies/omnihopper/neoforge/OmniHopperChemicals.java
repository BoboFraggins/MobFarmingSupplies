package net.bobofraggins.mobfarmingsupplies.omnihopper.neoforge;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Mekanism chemical support for the Omnidirectional Hopper, without depending on Mekanism.
 *
 * <p>Since its 26.x line, Mekanism exposes chemicals as a NeoForge {@code ResourceHandler} on the
 * block capability {@code mekanism:chemical_handler}. The hopper only ever moves chemicals from one
 * handler to another, so it looks that capability up by name and never touches a Mekanism class.
 * NeoForge returns the existing capability when the name and type match; a Mekanism build that
 * registered the name with a different type (an unofficial port did) makes creation throw, and
 * chemicals are then simply not moved. Mekanism is ordered before us (neoforge.mods.toml), so its
 * capability always exists first.
 */
public final class OmniHopperChemicals {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier CAPABILITY_ID = Identifier.fromNamespaceAndPath("mekanism", "chemical_handler");

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
            Resource emptyChemical = (Resource) Class.forName("mekanism.api.chemical.ChemicalResource")
                    .getField("EMPTY").get(null);
            capability = BlockCapability.createSided(CAPABILITY_ID, ResourceHandler.asClass());
            empty = emptyChemical;
        } catch (ReflectiveOperationException | RuntimeException e) {
            capability = null;
            LOGGER.warn("Omnidirectional Hopper: this Mekanism version's chemical API isn't supported; "
                    + "chemicals won't be moved ({})", e.toString());
        }
    }
}
