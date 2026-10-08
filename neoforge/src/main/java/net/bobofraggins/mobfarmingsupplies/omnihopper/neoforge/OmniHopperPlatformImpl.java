package net.bobofraggins.mobfarmingsupplies.omnihopper.neoforge;

import java.util.function.Predicate;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.EvenSplit;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperNode;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/** NeoForge implementation of {@link net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperPlatform}. */
public final class OmniHopperPlatformImpl {

    private OmniHopperPlatformImpl() {}

    public static void transfer(HopperNode node, boolean itemTick) {
        Level level = node.getLevel();
        if (level == null || !node.beginRouting()) return;
        try {
            BlockPos pos = node.getBlockPos();
            if (itemTick) {
                Predicate<ItemResource> items = r -> node.allowsItem(r.toStack(1));
                moveResources(node, level, pos, Capabilities.Item.BLOCK, items,
                        MFSConfig.getOmniHopperItemsPerTransfer(), true);
            }
            moveResources(node, level, pos, Capabilities.Fluid.BLOCK, r -> true,
                    MFSConfig.getOmniHopperFluidPerTick(), false);
            moveEnergy(node, level, pos, MFSConfig.getOmniHopperEnergyPerTick());
            BlockCapability<ResourceHandler<Resource>, Direction> chemical = OmniHopperChemicals.capability();
            if (chemical != null) {
                moveResources(node, level, pos, chemical, r -> true, MFSConfig.getOmniHopperChemicalPerTick(), false);
            }
        } finally {
            node.endRouting();
        }
    }

    public static void invalidateCapabilities(Level level, BlockPos pos) {
        level.invalidateCapabilities(pos);
    }

    // ── Pulling ───────────────────────────────────────────────────────────────────

    /**
     * Moves up to {@code perSide} of one kind of resource from each INPUT neighbour, split evenly
     * across the node's OUTPUT targets. {@code stacking} prefers topping up partial stacks (items).
     */
    private static <T extends Resource> void moveResources(
            HopperNode be, Level level, BlockPos pos,
            BlockCapability<ResourceHandler<T>, Direction> cap, Predicate<T> filter, int perSide, boolean stacking) {
        for (Direction in : Direction.values()) {
            if (be.getSide(in) != HopperSide.INPUT) continue;
            ResourceHandler<T> source = handlerAt(level, pos, in, cap);
            if (source == null) continue;
            // Split what the source can actually give, not the whole budget - otherwise 9 items
            // over two outputs would all land in the first one's share of 32.
            int amount = available(source, filter, perSide);
            if (amount <= 0) continue;
            EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                ResourceHandler<T> dest = handlerAt(out, cap);
                if (dest == null) return 0;
                try (Transaction tx = Transaction.openRoot()) {
                    int moved = stacking
                            ? ResourceHandlerUtil.moveStacking(source, dest, filter, (int) max, tx)
                            : ResourceHandlerUtil.move(source, dest, filter, (int) max, tx);
                    if (moved > 0) tx.commit();
                    return moved;
                }
            });
        }
    }

    /** How much (up to {@code max}) {@code source} can give that passes {@code filter}. */
    private static <T extends Resource> int available(ResourceHandler<T> source, Predicate<T> filter, int max) {
        int found = 0;
        try (Transaction simulation = Transaction.openRoot()) { // never committed
            for (int slot = 0; slot < source.size() && found < max; slot++) {
                T resource = source.getResource(slot);
                if (resource.isEmpty() || !filter.test(resource)) continue;
                found += source.extract(slot, resource, max - found, simulation);
            }
        }
        return found;
    }

    private static void moveEnergy(HopperNode be, Level level, BlockPos pos, int perSide) {
        for (Direction in : Direction.values()) {
            if (be.getSide(in) != HopperSide.INPUT) continue;
            EnergyHandler source = handlerAt(level, pos, in, Capabilities.Energy.BLOCK);
            if (source == null) continue;
            int amount;
            try (Transaction simulation = Transaction.openRoot()) { // never committed
                amount = source.extract(perSide, simulation);
            }
            if (amount <= 0) continue;
            EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                EnergyHandler dest = handlerAt(out, Capabilities.Energy.BLOCK);
                if (dest == null) return 0;
                try (Transaction tx = Transaction.openRoot()) {
                    int moved = EnergyHandlerUtil.move(source, dest, (int) max, tx);
                    if (moved > 0) tx.commit();
                    return moved;
                }
            });
        }
    }

    // ── Pushed-in resources ───────────────────────────────────────────────────────

    /**
     * Routes a resource pushed into an INPUT side straight to the node's OUTPUT targets, inside the
     * caller's transaction. Returns how much was accepted.
     */
    static <T extends Resource> int routeInsert(HopperNode be,
            BlockCapability<ResourceHandler<T>, Direction> cap, T resource, int amount, TransactionContext tx) {
        Level level = be.getLevel();
        if (level == null || resource.isEmpty() || amount <= 0 || !be.isActive()) return 0;
        if (!be.beginRouting()) return 0; // hoppers feeding each other in a loop
        try {
            return (int) EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                ResourceHandler<T> dest = handlerAt(out, cap);
                return dest == null ? 0 : dest.insert(resource, (int) max, tx);
            });
        } finally {
            be.endRouting();
        }
    }

    /** Energy version of {@link #routeInsert}. */
    static int routeEnergy(HopperNode be, int amount, TransactionContext tx) {
        Level level = be.getLevel();
        if (level == null || amount <= 0 || !be.isActive()) return 0;
        if (!be.beginRouting()) return 0;
        try {
            return (int) EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                EnergyHandler dest = handlerAt(out, Capabilities.Energy.BLOCK);
                return dest == null ? 0 : dest.insert((int) max, tx);
            });
        } finally {
            be.endRouting();
        }
    }

    @Nullable
    private static <H> H handlerAt(Level level, BlockPos pos, Direction side, BlockCapability<H, Direction> cap) {
        BlockPos neighbour = pos.relative(side);
        // Never load a chunk just to look: a bridge's targets can be anywhere.
        if (!level.isLoaded(neighbour)) return null;
        return level.getCapability(cap, neighbour, side.getOpposite());
    }

    @Nullable
    private static <H> H handlerAt(HopperOutput out, BlockCapability<H, Direction> cap) {
        return handlerAt(out.level(), out.pos(), out.side(), cap);
    }
}
