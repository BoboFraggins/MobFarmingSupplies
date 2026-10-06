package net.bobofraggins.mobfarmingsupplies.neoforge.glamping.present;

import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentEndermanHandler;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Enderman;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * NeoForge spawn-event listener for the Present enderman easter egg — the actual logic lives in
 * the cross-platform {@link PresentEndermanHandler}; this class only wires it to NeoForge's spawn event.
 */
public class PresentSpawnEvents {

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Enderman enderman)) return;

        EntitySpawnReason spawnType = event.getSpawnType();
        if (spawnType == EntitySpawnReason.COMMAND
                || spawnType == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnType == EntitySpawnReason.BUCKET
                || spawnType == EntitySpawnReason.DISPENSER) return;

        PresentEndermanHandler.tryGivePresent(enderman, event.getLevel().getRandom());
    }
}
