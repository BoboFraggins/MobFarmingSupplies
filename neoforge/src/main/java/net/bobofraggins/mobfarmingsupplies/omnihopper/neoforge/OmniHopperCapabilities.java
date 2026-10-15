package net.bobofraggins.mobfarmingsupplies.omnihopper.neoforge;

import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperNode;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.BlockCapability;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.fluid.FluidResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.item.ItemResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.resource.Resource;

/** Registers the Omnidirectional Hopper's and Einstein-Rosen Bridge's insert-only handlers, on INPUT sides only. */
public final class OmniHopperCapabilities {

    private OmniHopperCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        registerFor(event, Registration.OMNI_HOPPER_BE_TYPE.get());
        registerFor(event, Registration.BRIDGE_BE_TYPE.get());
    }

    private static <B extends BlockEntity & HopperNode> void registerFor(RegisterCapabilitiesEvent event,
                                                                        BlockEntityType<B> type) {
        Capabilities.Item.BLOCK.registerBlockEntity(event, type, (be, side) -> isInput(be, side)
                ? new OmniHopperInsertHandler<>(be, Capabilities.Item.BLOCK, ItemResource.EMPTY,
                        r -> be.allowsItem(r.toStack(1)),
                        r -> r.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : r.toStack(1).getMaxStackSize())
                : null);
        Capabilities.Fluid.BLOCK.registerBlockEntity(event, type, (be, side) -> isInput(be, side)
                ? new OmniHopperInsertHandler<>(be, Capabilities.Fluid.BLOCK, FluidResource.EMPTY,
                        r -> true, r -> Integer.MAX_VALUE)
                : null);
        Capabilities.Energy.BLOCK.registerBlockEntity(event, type, (be, side) -> isInput(be, side)
                ? new OmniHopperEnergyHandler(be) : null);

        BlockCapability<ResourceHandler<Resource>, Direction> chemical = OmniHopperChemicals.capability();
        Resource emptyChemical = OmniHopperChemicals.empty();
        if (chemical != null && emptyChemical != null) {
            chemical.registerBlockEntity(event, type, (be, side) -> isInput(be, side)
                    ? new OmniHopperInsertHandler<>(be, chemical, emptyChemical, r -> true, r -> Integer.MAX_VALUE)
                    : null);
        }
    }

    private static boolean isInput(HopperNode be, Direction side) {
        return side != null && be.getSide(side) == HopperSide.INPUT;
    }
}
