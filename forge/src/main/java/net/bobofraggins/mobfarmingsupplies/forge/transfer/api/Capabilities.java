package net.bobofraggins.mobfarmingsupplies.forge.transfer.api;

import net.bobofraggins.mobfarmingsupplies.forge.transfer.api.energy.EnergyHandler;
import net.bobofraggins.mobfarmingsupplies.forge.transfer.api.fluid.FluidResource;
import net.bobofraggins.mobfarmingsupplies.forge.transfer.api.item.ItemResource;
import net.minecraft.core.Direction;

/** Forge's block entity capabilities, through this package's handler types. */
public final class Capabilities {

    private Capabilities() {}

    public static final class Item {
        public static final BlockCapability<ResourceHandler<ItemResource>, Direction> BLOCK = new BlockCapability<>(
                net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, Wrappers::items, Adapters::items);

        private Item() {}
    }

    public static final class Fluid {
        public static final BlockCapability<ResourceHandler<FluidResource>, Direction> BLOCK = new BlockCapability<>(
                net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, Wrappers::fluids, Adapters::fluids);

        private Fluid() {}
    }

    public static final class Energy {
        public static final BlockCapability<EnergyHandler, Direction> BLOCK = new BlockCapability<>(
                net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY, Wrappers::energy, Adapters::energy);

        private Energy() {}
    }
}
