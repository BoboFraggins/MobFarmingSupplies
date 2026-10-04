package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits an amount of items as evenly as possible across several outputs.
 *
 * <p>Each output gets {@code amount / n}; the remainder goes one item each to the first outputs
 * in list order (the caller passes them in round-robin order, so over time every output gets its
 * turn at the extras). An output that takes less than its share is full and drops out, and what it
 * couldn't take is split the same way among the outputs still accepting, until everything is
 * placed or nothing more moves.
 *
 * <p>Example: 64 items, outputs A, B, C → 22, 21, 21. If A only had room for 10, the 12 left
 * over are split 6 / 6 between B and C, ending at 10, 27, 27.
 */
public final class EvenSplit {

    /** Moves up to {@code max} items to {@code out} and returns how many actually moved. */
    @FunctionalInterface
    public interface Sender {
        long send(Direction out, long max);
    }

    private EvenSplit() {}

    /** Distributes up to {@code amount} items across {@code outputs}; returns how many moved. */
    public static long distribute(List<Direction> outputs, long amount, Sender sender) {
        List<Direction> open = new ArrayList<>(outputs);
        long left = amount;
        while (left > 0 && !open.isEmpty()) {
            long share = left / open.size();
            long extra = left % open.size();
            long movedThisRound = 0;
            List<Direction> stillOpen = new ArrayList<>(open.size());
            for (int i = 0; i < open.size(); i++) {
                Direction out = open.get(i);
                long quota = share + (i < extra ? 1 : 0);
                if (quota == 0) {
                    stillOpen.add(out); // fewer items than outputs: no turn this round
                    continue;
                }
                long moved = sender.send(out, quota);
                movedThisRound += moved;
                if (moved >= quota) stillOpen.add(out);
            }
            if (movedThisRound == 0) break; // nothing accepted, or the source ran dry
            left -= movedThisRound;
            open = stillOpen;
        }
        return amount - left;
    }
}
