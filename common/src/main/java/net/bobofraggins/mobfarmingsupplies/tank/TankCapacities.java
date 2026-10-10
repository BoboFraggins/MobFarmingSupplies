package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.network.TankCapacitiesPacket;

/**
 * Tank capacities: the basic tank's size, multiplied once per upgrade tier. Both come from the
 * server config ({@code tank.baseCapacity}, {@code tank.upgradeMultiplier}).
 *
 * <p>Clients draw fill levels and tooltips from the same numbers, but Fabric doesn't send server
 * config to clients, so the server sets these when it starts and sends them to every player who
 * joins ({@link TankCapacitiesPacket}). In single-player the client and server share them.
 */
public final class TankCapacities {

    private static final long MB_PER_BUCKET = 1000L;

    private static volatile long baseMb = MFSConfig.DEFAULT_TANK_BASE_CAPACITY * MB_PER_BUCKET;
    private static volatile int multiplier = MFSConfig.DEFAULT_TANK_UPGRADE_MULTIPLIER;

    private TankCapacities() {}

    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(server ->
                set(MFSConfig.getTankBaseCapacity(), MFSConfig.getTankUpgradeMultiplier()));
        PlayerEvent.PLAYER_JOIN.register(player ->
                NetworkManager.sendToPlayer(player, new TankCapacitiesPacket(baseBuckets(), multiplier)));
    }

    /** Sets the basic tank's size in buckets and the multiplier per upgrade tier. */
    public static void set(int baseBuckets, int upgradeMultiplier) {
        baseMb = Math.max(1, baseBuckets) * MB_PER_BUCKET;
        multiplier = Math.max(1, upgradeMultiplier);
    }

    public static int baseBuckets() {
        return (int) (baseMb / MB_PER_BUCKET);
    }

    /**
     * Capacity of {@code tier} in mB: the base size times the multiplier once per tier above basic,
     * capped at {@link Integer#MAX_VALUE} mB (about 2.1 million buckets), which fluid APIs can handle.
     */
    public static long of(TankTier tier) {
        long capacity = baseMb;
        for (int i = 0; i < tier.ordinal() && capacity < Integer.MAX_VALUE; i++) capacity *= multiplier;
        return Math.min(capacity, Integer.MAX_VALUE);
    }
}
