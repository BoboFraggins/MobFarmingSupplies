package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets an object take part in transactions by saving snapshots of its state. Call
 * {@link #updateSnapshots} before changing the state inside a transaction; if that transaction is
 * aborted the state is put back with {@link #revertToSnapshot}. Once the outermost transaction
 * has committed and closed, {@link #onRootCommit} runs (for example to mark a block entity changed).
 */
public abstract class SnapshotJournal<T> {

    /** Snapshot per transaction depth; null where this journal hasn't changed in that transaction. */
    private final List<T> snapshots = new ArrayList<>();

    protected abstract T createSnapshot();

    protected abstract void revertToSnapshot(T snapshot);

    /** Called once the outermost transaction that changed this has committed, with the state before it. */
    protected void onRootCommit(T originalState) {}

    /** Saves the state for {@code tx} unless already saved there. */
    public void updateSnapshots(TransactionContext tx) {
        int depth = tx.depth();
        while (snapshots.size() <= depth) snapshots.add(null);
        if (snapshots.get(depth) != null) return;
        snapshots.set(depth, createSnapshot());
        tx.addCloseCallback(this::onClose);
    }

    private void onClose(TransactionContext context, boolean committed) {
        Transaction tx = (Transaction) context;
        int depth = tx.depth();
        T snapshot = snapshots.set(depth, null);
        if (!committed) {
            revertToSnapshot(snapshot);
        } else if (tx.parent() != null) {
            // Keep the older snapshot for the enclosing transaction, so aborting that undoes this too.
            if (snapshots.get(depth - 1) == null) {
                snapshots.set(depth - 1, snapshot);
                tx.parent().addCloseCallback(this::onClose);
            }
        } else {
            tx.afterRootCommit(() -> onRootCommit(snapshot));
        }
    }
}
