package net.bobofraggins.mobfarmingsupplies.fluid.fabric;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Converts between the mod's fluid unit and Fabric's.
 *
 * <p>The mod stores fluids in millibuckets on both loaders (1000 per bucket, as NeoForge counts),
 * but Fabric's Transfer API counts in droplets (81,000 per bucket). Every Fabric storage the mod
 * exposes, and every exchange with someone else's, converts here - so a bucket is a bucket and a
 * 64,000 mB tank holds 64 buckets on Fabric too. Amounts only move in whole millibuckets: a request
 * that isn't a multiple of {@link #DROPLETS_PER_MB} is rounded down.
 */
@SuppressWarnings("UnstableApiUsage")
public final class FabricFluidUnits {

    /** Droplets in one millibucket (81). */
    public static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000;

    private FabricFluidUnits() {}

    public static long toDroplets(long mb) {
        return mb * DROPLETS_PER_MB;
    }

    /** Whole millibuckets in {@code droplets}, rounded down. */
    public static long toMb(long droplets) {
        return droplets / DROPLETS_PER_MB;
    }

    /**
     * Inserts up to {@code mb} millibuckets of {@code fluid} into someone else's storage, in whole
     * millibuckets only, and returns how many went in. A storage that would take a part-millibucket
     * (say 100 droplets) is offered the whole-millibucket part, so nothing is lost or created.
     */
    public static long insertMb(Storage<FluidVariant> dest, FluidVariant fluid, long mb, TransactionContext tx) {
        long wholeMb;
        try (Transaction trial = Transaction.openNested(tx)) { // never committed
            wholeMb = toMb(dest.insert(fluid, toDroplets(mb), trial));
        }
        if (wholeMb <= 0) return 0;
        return toMb(dest.insert(fluid, toDroplets(wholeMb), tx));
    }

    /** Extracts up to {@code mb} millibuckets of {@code fluid} from someone else's storage, in whole millibuckets. */
    public static long extractMb(Storage<FluidVariant> source, FluidVariant fluid, long mb, TransactionContext tx) {
        long wholeMb;
        try (Transaction trial = Transaction.openNested(tx)) { // never committed
            wholeMb = toMb(source.extract(fluid, toDroplets(mb), trial));
        }
        if (wholeMb <= 0) return 0;
        return toMb(source.extract(fluid, toDroplets(wholeMb), tx));
    }
}
