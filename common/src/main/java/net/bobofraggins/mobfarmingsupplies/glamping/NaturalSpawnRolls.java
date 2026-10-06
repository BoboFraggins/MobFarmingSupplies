package net.bobofraggins.mobfarmingsupplies.glamping;

/**
 * Switch for the natural-spawn easter-egg rolls (Magic Hat zombies, Present endermen).
 *
 * <p>The Clone-O-Matic runs a mob's spawn setup inside {@link #without} and then rolls with its own,
 * lower chances, so the rate is the same on both loaders: NeoForge's {@code FinalizeSpawnEvent}
 * doesn't fire for a direct {@code finalizeSpawn} call, while the Fabric mixins would.
 */
public final class NaturalSpawnRolls {

    /** Per thread, so a world-generation spawn on another thread is never affected. */
    private static final ThreadLocal<Boolean> SUPPRESSED = ThreadLocal.withInitial(() -> false);

    private NaturalSpawnRolls() {}

    /** Runs {@code action} with the natural-spawn rolls switched off. */
    public static void without(Runnable action) {
        boolean previous = SUPPRESSED.get();
        SUPPRESSED.set(true);
        try {
            action.run();
        } finally {
            SUPPRESSED.set(previous);
        }
    }

    public static boolean suppressed() {
        return SUPPRESSED.get();
    }
}
