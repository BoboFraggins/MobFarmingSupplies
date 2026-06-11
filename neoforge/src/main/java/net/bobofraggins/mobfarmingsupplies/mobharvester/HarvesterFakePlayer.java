package net.bobofraggins.mobfarmingsupplies.mobharvester;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Fake player used by the Mob Harvester to attack mobs.
 *
 * <p>Extends {@link FakePlayer} so it can access the {@code protected}
 * {@code attackStrengthTicker} field on {@link net.minecraft.world.entity.LivingEntity},
 * which is needed to reset attack strength to maximum between successive hits in the
 * same tick (one per mob in the kill zone).
 *
 * <p>Attacks are attributed to this fake player so kills count as player kills,
 * enabling Looting and other player-kill-specific drop mechanics.
 *
 * <p>The fake player is parked far below the machine (y − 100) to avoid interfering
 * with the world.
 */
public class HarvesterFakePlayer extends FakePlayer {

    private static final UUID FAKE_UUID = UUID.fromString("d3a1b2c4-e5f6-7890-abcd-ef0102030405");
    private static final GameProfile PROFILE = new GameProfile(FAKE_UUID, "[MobHarvester]");

    public HarvesterFakePlayer(ServerLevel level) {
        super(level, PROFILE);
    }

    /**
     * Resets the attack-strength ticker to its maximum so the next {@link #attack} call
     * deals full damage regardless of how recently the previous attack occurred.
     * Must be called after each {@link #attack} when hitting multiple targets in one tick.
     */
    public void resetAttackStrength() {
        this.attackStrengthTicker = 100;
    }

    // ── Factory ───────────────────────────────────────────────────────────────────

    /**
     * Returns the cached fake player, creating it if the weak reference has been
     * collected. Repositions the fake player below {@code machinePos} each call.
     */
    public static WeakReference<HarvesterFakePlayer> get(
            WeakReference<HarvesterFakePlayer> previous, ServerLevel level, BlockPos machinePos) {
        HarvesterFakePlayer fp = previous.get();
        if (fp == null) {
            fp = new HarvesterFakePlayer(level);
        }
        fp.setPos(machinePos.getX() + 0.5, machinePos.getY() - 100.0, machinePos.getZ() + 0.5);
        return new WeakReference<>(fp);
    }

    /** Returns {@code true} if {@code player} was created by the Mob Harvester. */
    public static boolean isHarvesterFakePlayer(Object player) {
        return player instanceof HarvesterFakePlayer;
    }
}
