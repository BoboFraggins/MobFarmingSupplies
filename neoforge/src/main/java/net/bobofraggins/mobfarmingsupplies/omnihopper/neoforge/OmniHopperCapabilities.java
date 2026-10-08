package net.bobofraggins.mobfarmingsupplies.omnihopper.neoforge;

import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

/** Registers the Omnidirectional Hopper's insert-only handlers, on its INPUT sides only. */
public final class OmniHopperCapabilities {

    private OmniHopperCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        var type = Registration.OMNI_HOPPER_BE_TYPE.get();
        event.registerBlockEntity(Capabilities.Item.BLOCK, type, (be, side) -> isInput(be, side)
                ? new OmniHopperInsertHandler<>(be, Capabilities.Item.BLOCK, ItemResource.EMPTY,
                        r -> be.allowsItem(r.toStack(1)),
                        r -> r.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : r.toStack(1).getMaxStackSize())
                : null);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, (be, side) -> isInput(be, side)
                ? new OmniHopperInsertHandler<>(be, Capabilities.Fluid.BLOCK, FluidResource.EMPTY,
                        r -> true, r -> Integer.MAX_VALUE)
                : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, type, (be, side) -> isInput(be, side)
                ? new OmniHopperEnergyHandler(be) : null);

        BlockCapability<ResourceHandler<Resource>, Direction> chemical = OmniHopperChemicals.capability();
        Resource emptyChemical = OmniHopperChemicals.empty();
        if (chemical != null && emptyChemical != null) {
            event.registerBlockEntity(chemical, type, (be, side) -> isInput(be, side)
                    ? new OmniHopperInsertHandler<>(be, chemical, emptyChemical, r -> true, r -> Integer.MAX_VALUE)
                    : null);
        }
    }

    private static boolean isInput(OmniHopperBlockEntity be, Direction side) {
        return side != null && be.getSide(side) == HopperSide.INPUT;
    }
}
