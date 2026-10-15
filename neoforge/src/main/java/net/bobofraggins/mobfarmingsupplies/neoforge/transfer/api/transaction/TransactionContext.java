package net.bobofraggins.mobfarmingsupplies.neoforge.transfer.api.transaction;

/** An open transaction, as passed to handlers. */
public interface TransactionContext {

    /** 0 for the outermost transaction, 1 for one opened inside it, and so on. */
    int depth();

    /** Runs {@code callback} as this transaction closes, committed or not. */
    void addCloseCallback(CloseCallback callback);

    @FunctionalInterface
    interface CloseCallback {
        void onClose(TransactionContext transaction, boolean committed);
    }
}
