package net.bobofraggins.mobfarmingsupplies.neoforge.glamping.present;

import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentEndermanHandler;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.EnderMan;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * NeoForge spawn-event listener for the Present enderman easter egg — the actual logic lives in
 * the cross-platform {@link PresentEndermanHandler}; this class only wires it to NeoForge's spawn event.
 */
public class PresentSpawnEvents {

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof EnderMan enderman)) return;

        MobSpawnType spawnType = event.getSpawnType();
        if (spawnType == MobSpawnType.COMMAND
                || spawnType == MobSpawnType.SPAWN_EGG
                || spawnType == MobSpawnType.BUCKET
                || spawnType == MobSpawnType.DISPENSER) return;

        PresentEndermanHandler.tryGivePresent(enderman, event.getLevel().getRandom());
    }
}
