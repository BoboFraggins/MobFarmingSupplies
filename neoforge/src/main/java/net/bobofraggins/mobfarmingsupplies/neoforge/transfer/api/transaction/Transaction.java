package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * A transaction: changes made inside it are kept only if it is committed, and undone when it is
 * closed without being committed. Transactions nest; a nested transaction's changes are kept only
 * if every transaction around it commits too. Use with try-with-resources:
 *
 * <pre>{@code
 * try (Transaction tx = Transaction.openRoot()) {
 *     int moved = handler.insert(resource, amount, tx);
 *     if (moved > 0) tx.commit();
 * }
 * }</pre>
 *
 * Each thread has its own stack of open transactions.
 */
public final class Transaction implements TransactionContext, AutoCloseable {

    private static final ThreadLocal<Deque<Transaction>> OPEN = ThreadLocal.withInitial(ArrayDeque::new);

    private final Deque<Transaction> stack;
    @Nullable private final Transaction parent;
    private final int depth;
    private final List<CloseCallback> closeCallbacks = new ArrayList<>();
    /** Run after the outermost transaction has committed and closed; kept on the root. */
    private final List<Runnable> afterRootCommit;
    private boolean closed;

    private Transaction(Deque<Transaction> stack, @Nullable Transaction parent) {
        this.stack = stack;
        this.parent = parent;
        this.depth = parent == null ? 0 : parent.depth + 1;
        this.afterRootCommit = parent == null ? new ArrayList<>() : parent.afterRootCommit;
    }

    /** Opens an outermost transaction; none may be open on this thread. */
    public static Transaction openRoot() {
        Deque<Transaction> stack = OPEN.get();
        if (!stack.isEmpty()) throw new IllegalStateException("A transaction is already open on this thread");
        return push(stack, null);
    }

    /**
     * Opens a transaction inside {@code parent}, which must be the innermost open one; or an
     * outermost one if {@code parent} is null.
     */
    public static Transaction open(@Nullable TransactionContext parent) {
        if (parent == null) return openRoot();
        Deque<Transaction> stack = OPEN.get();
        if (stack.peek() != parent) {
            throw new IllegalStateException("Only the innermost open transaction can have one opened inside it");
        }
        return push(stack, (Transaction) parent);
    }

    /** Opens a transaction inside the innermost open one on this thread, or an outermost one. */
    public static Transaction openAny() {
        Deque<Transaction> stack = OPEN.get();
        return push(stack, stack.peek());
    }

    private static Transaction push(Deque<Transaction> stack, @Nullable Transaction parent) {
        Transaction tx = new Transaction(stack, parent);
        stack.push(tx);
        return tx;
    }

    @Override
    public int depth() {
        return depth;
    }

    @Override
    public void addCloseCallback(CloseCallback callback) {
        if (closed) throw new IllegalStateException("Transaction already closed");
        closeCallbacks.add(callback);
    }

    /** The transaction this one is inside; for journals moving a snapshot outwards. */
    @Nullable
    Transaction parent() {
        return parent;
    }

    /** Queues {@code action} for after the outermost transaction commits; from a root close callback only. */
    void afterRootCommit(Runnable action) {
        afterRootCommit.add(action);
    }

    /** Keeps this transaction's changes and closes it. */
    public void commit() {
        finish(true);
    }

    /** Undoes this transaction's changes and closes it. */
    public void abort() {
        finish(false);
    }

    /** Aborts unless already committed or aborted. */
    @Override
    public void close() {
        if (!closed) finish(false);
    }

    private void finish(boolean commit) {
        if (closed) throw new IllegalStateException("Transaction already closed");
        if (stack.peek() != this) throw new IllegalStateException("Close the transactions opened inside this one first");
        // Last registered first, so later changes are undone before earlier ones.
        for (int i = closeCallbacks.size() - 1; i >= 0; i--) {
            closeCallbacks.get(i).onClose(this, commit);
        }
        closed = true;
        stack.pop();
        if (parent == null) {
            List<Runnable> actions = List.copyOf(afterRootCommit);
            afterRootCommit.clear();
            if (commit) actions.forEach(Runnable::run);
        }
    }
}
