package net.bobofraggins.mobfarmingsupplies.omnihopper.neoforge;

import net.bobofraggins.mobfarmingsupplies.shared.TransferSchedule;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.CommitCallbacks;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.HopperLookups;
import java.util.function.Predicate;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.EvenSplit;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperNode;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.BlockCapability;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.Capabilities;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.ResourceHandlerUtil;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.energy.EnergyHandler;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.energy.EnergyHandlerUtil;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.item.ItemResource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.resource.Resource;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.Transaction;
import net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/** NeoForge implementation of {@link net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperPlatform}. */
public final class OmniHopperPlatformImpl {

    private OmniHopperPlatformImpl() {}

    public static void transfer(HopperNode node) {
        Level level = node.nodeLevel();
        if (level == null || !node.beginRouting()) return;
        try {
            BlockPos pos = node.nodePos();
            TransferSchedule schedule = node.transferSchedule();
            Predicate<ItemResource> items = r -> node.allowsItem(r.toStack(1));
            schedule.attempt(TransferSchedule.Kind.ITEMS, () -> moveResources(node, level, pos,
                    Capabilities.Item.BLOCK, items, MFSConfig.getHopperItemsPerTransfer(), true) > 0);
            schedule.attempt(TransferSchedule.Kind.FLUIDS, () -> moveResources(node, level, pos,
                    Capabilities.Fluid.BLOCK, r -> true,
                    TransferSchedule.perTransfer(MFSConfig.getHopperFluidPerTick()), false) > 0);
            schedule.attempt(TransferSchedule.Kind.ENERGY, () -> moveEnergy(node, level, pos,
                    TransferSchedule.perTransfer(MFSConfig.getHopperEnergyPerTick())) > 0);
            BlockCapability<ResourceHandler<Resource>, Direction> chemical = OmniHopperChemicals.capability();
            if (chemical != null) {
                schedule.attempt(TransferSchedule.Kind.CHEMICALS, () -> moveResources(node, level, pos,
                        chemical, r -> true,
                        TransferSchedule.perTransfer(MFSConfig.getHopperChemicalPerTick()), false) > 0);
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
     * Returns how much moved.
     */
    private static <T extends Resource> long moveResources(
            HopperNode be, Level level, BlockPos pos,
            BlockCapability<ResourceHandler<T>, Direction> cap, Predicate<T> filter, int perSide, boolean stacking) {
        long total = 0;
        for (Direction in : Direction.values()) {
            if (be.getSide(in) != HopperSide.INPUT) continue;
            ResourceHandler<T> source = handlerAt(be, level, pos, in, cap);
            // Nothing to move: skip without opening a transaction.
            if (source == null || !HopperLookups.hasAny(source, filter)) continue;
            // Split what the source can actually give, not the whole budget - otherwise 9 items
            // over two outputs would all land in the first one's share of 32.
            int amount = available(source, filter, perSide);
            if (amount <= 0) continue;
            total += EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                ResourceHandler<T> dest = handlerAt(be, out, cap);
                if (dest == null || HopperLookups.definitelyFull(dest)) return 0;
                try (Transaction tx = Transaction.openRoot()) {
                    int moved = stacking
                            ? ResourceHandlerUtil.moveStacking(source, dest, filter, (int) max, tx)
                            : ResourceHandlerUtil.move(source, dest, filter, (int) max, tx);
                    if (moved > 0) {
                        tx.commit();
                        be.delivered(out);
                    }
                    return moved;
                }
            });
        }
        return total;
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

    /** Moves up to {@code perSide} energy from each INPUT neighbour; returns how much moved. */
    private static long moveEnergy(HopperNode be, Level level, BlockPos pos, int perSide) {
        long total = 0;
        for (Direction in : Direction.values()) {
            if (be.getSide(in) != HopperSide.INPUT) continue;
            EnergyHandler source = handlerAt(be, level, pos, in, Capabilities.Energy.BLOCK);
            if (source == null || source.getAmountAsLong() <= 0) continue;
            int amount;
            try (Transaction simulation = Transaction.openRoot()) { // never committed
                amount = source.extract(perSide, simulation);
            }
            if (amount <= 0) continue;
            total += EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                EnergyHandler dest = handlerAt(be, out, Capabilities.Energy.BLOCK);
                if (dest == null || HopperLookups.definitelyFull(dest)) return 0;
                try (Transaction tx = Transaction.openRoot()) {
                    int moved = EnergyHandlerUtil.move(source, dest, (int) max, tx);
                    if (moved > 0) {
                        tx.commit();
                        be.delivered(out);
                    }
                    return moved;
                }
            });
        }
        return total;
    }

    // ── Pushed-in resources ───────────────────────────────────────────────────────

    /**
     * Routes a resource pushed into an INPUT side straight to the node's OUTPUT targets, inside the
     * caller's transaction. Returns how much was accepted.
     */
    static <T extends Resource> int routeInsert(HopperNode be,
            BlockCapability<ResourceHandler<T>, Direction> cap, T resource, int amount, TransactionContext tx) {
        Level level = be.nodeLevel();
        if (level == null || resource.isEmpty() || amount <= 0 || !be.isActive()) return 0;
        if (!be.beginRouting()) return 0; // hoppers feeding each other in a loop
        try {
            return (int) EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                ResourceHandler<T> dest = handlerAt(be, out, cap);
                int inserted = dest == null ? 0 : dest.insert(resource, (int) max, tx);
                if (inserted > 0) CommitCallbacks.onCommit(tx, () -> be.delivered(out));
                return inserted;
            });
        } finally {
            be.endRouting();
        }
    }

    /** Energy version of {@link #routeInsert}. */
    static int routeEnergy(HopperNode be, int amount, TransactionContext tx) {
        Level level = be.nodeLevel();
        if (level == null || amount <= 0 || !be.isActive()) return 0;
        if (!be.beginRouting()) return 0;
        try {
            return (int) EvenSplit.distribute(be.outputTargets(), amount, (out, max) -> {
                EnergyHandler dest = handlerAt(be, out, Capabilities.Energy.BLOCK);
                int inserted = dest == null ? 0 : dest.insert((int) max, tx);
                if (inserted > 0) CommitCallbacks.onCommit(tx, () -> be.delivered(out));
                return inserted;
            });
        } finally {
            be.endRouting();
        }
    }

    /** The handler beside {@code pos} on {@code side}, from the node's cached lookups. */
    @Nullable
    private static <H> H handlerAt(HopperNode node, Level level, BlockPos pos, Direction side,
                                   BlockCapability<H, Direction> cap) {
        return HopperLookups.of(node).at(level, pos, side, cap);
    }

    @Nullable
    private static <H> H handlerAt(HopperNode node, HopperOutput out, BlockCapability<H, Direction> cap) {
        return handlerAt(node, out.level(), out.pos(), out.side(), cap);
    }
}
