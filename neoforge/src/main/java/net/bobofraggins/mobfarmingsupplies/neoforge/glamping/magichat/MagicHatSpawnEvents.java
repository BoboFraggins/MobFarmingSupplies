package net.bobofraggins.mobfarmingsupplies.neoforge.glamping.magichat;

import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatZombieHandler;

/**
 * NeoForge spawn-event listener for the Magic Hat's zombie easter egg — the actual
 * "5% chance, surprise mob inside" logic lives in the cross-platform
 * {@link MagicHatZombieHandler}; this class only wires it to NeoForge's spawn event.
 */
public class MagicHatSpawnEvents {

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie)) return;

        MobSpawnType spawnType = event.getSpawnType();
        if (spawnType == MobSpawnType.COMMAND
                || spawnType == MobSpawnType.SPAWN_EGG
                || spawnType == MobSpawnType.BUCKET
                || spawnType == MobSpawnType.DISPENSER) return;

        MagicHatZombieHandler.tryEquipMagicHat(zombie, event.getLevel().getRandom());
    }
}
