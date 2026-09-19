package net.bobofraggins.mobfarmingsupplies.glamping.magichat;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * NeoForge spawn-event listener for the Magic Hat's zombie easter egg — the actual
 * "5% chance, surprise mob inside" logic lives in the cross-platform
 * {@link MagicHatZombieHandler}; this class only wires it to NeoForge's spawn event.
 */
public class MagicHatSpawnEvents {

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie)) return;

        EntitySpawnReason spawnType = event.getSpawnType();
        if (spawnType == EntitySpawnReason.COMMAND
                || spawnType == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnType == EntitySpawnReason.BUCKET
                || spawnType == EntitySpawnReason.DISPENSER) return;

        MagicHatZombieHandler.tryEquipMagicHat(zombie, event.getLevel().getRandom());
    }
}
