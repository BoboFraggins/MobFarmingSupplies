package net.bobofraggins.mobfarmingsupplies.mobharvester.fabric;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

/**
 * Fake player used by the Mob Harvester to attack mobs on Fabric.
 *
 * <p>Extends Fabric API's {@link FakePlayer} (itself a {@code ServerPlayer}) so attacks
 * are attributed to a player, enabling XP orb drops, Looting, and other
 * player-kill-specific drop mechanics.
 *
 * <p>The fake player is parked far below the machine (y − 100) to avoid interfering
 * with the world.
 */
public class FabricHarvesterFakePlayer extends FakePlayer {

    private static final UUID FAKE_UUID = UUID.fromString("d3a1b2c4-e5f6-7890-abcd-ef0102030405");
    private static final GameProfile PROFILE = new GameProfile(FAKE_UUID, "[MobHarvester]");

    public FabricHarvesterFakePlayer(ServerLevel level) {
        super(level, PROFILE);
    }

    /**
     * Resets the attack-strength ticker to its maximum so the next {@code attack} call
     * deals full damage regardless of how recently the previous attack occurred.
     * Must be called before each {@code attack} when hitting multiple targets in one tick.
     */
    public void resetAttackStrength() {
        this.attackStrengthTicker = 100;
    }
}
