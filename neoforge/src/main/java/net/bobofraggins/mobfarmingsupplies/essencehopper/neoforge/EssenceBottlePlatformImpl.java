package net.bobofraggins.mobfarmingsupplies.essencehopper.neoforge;

import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.essencehopper.EssenceBottleItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.Capabilities;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.fluid.FluidResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public final class EssenceBottlePlatformImpl {

    private EssenceBottlePlatformImpl() {}

    @Nullable
    public static Essence extractBottle(Level level, BlockPos pos, Direction side) {
        ResourceHandler<FluidResource> handler = Capabilities.Fluid.BLOCK.getCapability(level, pos, side);
        if (handler == null) return null;
        for (int i = 0; i < handler.size(); i++) {
            FluidResource resource = handler.getResource(i);
            if (resource.isEmpty() || handler.getAmountAsLong(i) < EssenceBottleItem.MB) continue;
            Essence essence = essenceOf(resource);
            if (essence == null) continue;
            try (Transaction tx = Transaction.openRoot()) {
                if (handler.extract(resource, EssenceBottleItem.MB, tx) == EssenceBottleItem.MB) {
                    tx.commit();
                    return essence;
                }
            }
        }
        return null;
    }

    public static boolean insertBottle(Level level, BlockPos pos, Direction side, Essence essence) {
        ResourceHandler<FluidResource> handler = Capabilities.Fluid.BLOCK.getCapability(level, pos, side);
        if (handler == null) return false;
        FluidResource resource = FluidResource.of(Registration.ESSENCE_SOURCES.get(essence).get());
        try (Transaction tx = Transaction.openRoot()) {
            if (handler.insert(resource, EssenceBottleItem.MB, tx) != EssenceBottleItem.MB) return false;
            tx.commit();
            return true;
        }
    }

    @Nullable
    private static Essence essenceOf(FluidResource resource) {
        for (Essence essence : Essence.values()) {
            if (resource.getFluid().isSame(Registration.ESSENCE_SOURCES.get(essence).get())) return essence;
        }
        return null;
    }
}
