package net.bobofraggins.mobfarmingsupplies.shared;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * When a hopper moves things: the cadence and back-off shared by the Absorption Hopper, Logistic
 * Sorter, Omnidirectional Hopper and Einstein-Rosen Bridge. Each block holds one and calls
 * {@link #due()} every tick; on a due tick it runs each kind of transfer through
 * {@link #attempt}.
 *
 * <p>Every kind of material moves on the same cadence, once every
 * {@link MFSConfig#getHopperTransferInterval()} ticks. The per-tick rates in the config (fluid,
 * energy, chemicals) are scaled by {@link #perTransfer} so throughput doesn't depend on the
 * interval. Each block's transfers are offset within the interval by its position, so hoppers
 * loaded together don't all move on the same tick.
 *
 * <p>Back-off: a transfer that moves nothing is often going to keep moving nothing (the
 * destination is full, or the source is empty), and trying again is the expensive part, especially
 * against large modded storage. So after a transfer of a kind moves nothing, the next attempts of
 * that kind are skipped: one after the first empty transfer, then two, doubling up to
 * {@link #MAX_SKIPPED}. Each transfer that moves something halves the wait again, so a
 * destination that only takes a trickle isn't tried at full speed straight away. {@link #reset}
 * puts every kind back on every due tick (call it when a neighbour or the block's own settings or
 * contents change). Each kind backs off on its own, so a full tank doesn't slow down items.
 *
 * <p>It also keeps the loader's lookup caches for the block ({@link #lookups}), so neighbouring
 * handlers are found once rather than on every transfer.
 */
public final class TransferSchedule {

    /** A block entity that moves things on a schedule. */
    public interface Holder {
        TransferSchedule transferSchedule();
    }

    /** What a hopper moves; each backs off separately. */
    public enum Kind { ITEMS, FLUIDS, ENERGY, CHEMICALS }

    /** The most due ticks skipped in a row while a transfer keeps moving nothing. */
    private static final int MAX_SKIPPED = 4;

    private int tick;
    @Nullable private Object lookups;
    /** Per kind: how many attempts the next empty transfer skips, and how many are left to skip. */
    private final int[] backoff = new int[Kind.values().length];
    private final int[] skipping = new int[Kind.values().length];

    /** Counts a tick; true when the block at {@code pos} moves things this tick. */
    public boolean due(BlockPos pos) {
        int interval = interval();
        return (++tick + Math.floorMod(Mth.getSeed(pos), interval)) % interval == 0;
    }

    /**
     * Runs {@code transfer} unless {@code kind} is backing off, and notes whether it moved
     * anything. {@code transfer} returns true when it moved something.
     */
    public void attempt(Kind kind, BooleanSupplier transfer) {
        int k = kind.ordinal();
        if (skipping[k] > 0) {
            skipping[k]--;
            return;
        }
        if (transfer.getAsBoolean()) {
            backoff[k] /= 2;
            skipping[k] = backoff[k];
        } else {
            backoff[k] = backoff[k] == 0 ? 1 : Math.min(backoff[k] * 2, MAX_SKIPPED);
            skipping[k] = backoff[k];
        }
    }

    /**
     * The loader's lookup caches for this block, made by {@code create} on first use. The loader
     * code owns what's in it; it lives as long as the block entity does.
     */
    @SuppressWarnings("unchecked")
    public <C> C lookups(Supplier<C> create) {
        if (lookups == null) lookups = create.get();
        return (C) lookups;
    }

    /** Tries every kind again on the next due tick. */
    public void reset() {
        Arrays.fill(backoff, 0);
        Arrays.fill(skipping, 0);
    }

    /**
     * Resets the schedule of the block entity at {@code pos}, if it has one. Blocks call this from
     * {@code neighborChanged}, so a container placed or swapped beside a hopper is tried at once.
     */
    public static void resetAt(Level level, BlockPos pos) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof Holder holder) {
            holder.transferSchedule().reset();
        }
    }

    /** Ticks between transfers. */
    public static int interval() {
        return Math.max(1, MFSConfig.getHopperTransferInterval());
    }

    /** How much of a per-tick rate one transfer moves, so the rate holds whatever the interval. */
    public static int perTransfer(int perTick) {
        return (int) Math.min(Integer.MAX_VALUE, (long) perTick * interval());
    }
}
