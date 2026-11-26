package net.bobofraggins.mobfarmingsupplies.forge.headpike;

import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikes;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

/** Keeps the mobs whose heads are on a Head Pike from appearing on their own near it (see {@link HeadPikes}). */
public class HeadPikeSpawnEvents {

    @SubscribeEvent
    public void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (HeadPikes.refuses(event.getLevel(), event.getEntity(), event.getSpawnType(),
                event.getX(), event.getY(), event.getZ())) {
            event.setSpawnCancelled(true);
        }
    }
}
