package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api;

import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.energy.EnergyHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.fluid.FluidResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.item.ItemResource;
import net.minecraft.core.Direction;

/** NeoForge 21.1's block capabilities, through this package's handler types. */
public final class Capabilities {

    private Capabilities() {}

    public static final class Item {
        public static final BlockCapability<ResourceHandler<ItemResource>, Direction> BLOCK = new BlockCapability<>(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, Wrappers::items, Adapters::items);

        private Item() {}
    }

    public static final class Fluid {
        public static final BlockCapability<ResourceHandler<FluidResource>, Direction> BLOCK = new BlockCapability<>(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, Wrappers::fluids, Adapters::fluids);

        private Fluid() {}
    }

    public static final class Energy {
        public static final BlockCapability<EnergyHandler, Direction> BLOCK = new BlockCapability<>(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, Wrappers::energy, Adapters::energy);

        private Energy() {}
    }
}
