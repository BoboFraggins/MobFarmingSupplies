package net.bobofraggins.mobfarmingsupplies.neoforge.transfer;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Runs code only if a transfer really happens, not when it is simulated or rolled back. */
public final class CommitCallbacks {

    private CommitCallbacks() {}

    /** Runs {@code action} once the outermost transaction of {@code tx} commits; never if it's aborted. */
    public static void onCommit(TransactionContext tx, Runnable action) {
        new SnapshotJournal<Boolean>() {
            private boolean pending;

            {
                updateSnapshots(tx);
                pending = true;
            }

            @Override
            protected Boolean createSnapshot() { return pending; }

            @Override
            protected void revertToSnapshot(Boolean snapshot) { pending = snapshot; }

            @Override
            protected void onRootCommit(Boolean originalState) {
                if (pending) action.run();
            }
        };
    }
}
