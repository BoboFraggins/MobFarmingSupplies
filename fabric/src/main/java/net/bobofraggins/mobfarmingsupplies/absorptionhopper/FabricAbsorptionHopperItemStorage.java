package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

/**
 * Returns a Fabric {@link Storage}{@code <ItemVariant>} for the Absorption Hopper's
 * 27-slot inventory, using {@link ContainerStorage} to wrap the existing
 * {@link net.minecraft.world.SimpleContainer}.
 */
@SuppressWarnings("UnstableApiUsage")
public final class FabricAbsorptionHopperItemStorage {

    private FabricAbsorptionHopperItemStorage() {}

    public static Storage<ItemVariant> of(AbsorptionHopperBlockEntity be) {
        return ContainerStorage.of(be.inventory, null);
    }
}
