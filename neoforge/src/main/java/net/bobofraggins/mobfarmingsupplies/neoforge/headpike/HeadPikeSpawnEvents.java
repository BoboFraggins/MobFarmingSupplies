package net.bobofraggins.mobfarmingsupplies.neoforge.headpike;

import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/** Keeps the mobs whose heads are on a Head Pike from appearing on their own near it (see {@link HeadPikes}). */
public class HeadPikeSpawnEvents {

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (HeadPikes.refuses(event.getLevel(), event.getEntity(), event.getSpawnType(),
                event.getX(), event.getY(), event.getZ())) {
            event.setSpawnCancelled(true);
        }
    }
}
