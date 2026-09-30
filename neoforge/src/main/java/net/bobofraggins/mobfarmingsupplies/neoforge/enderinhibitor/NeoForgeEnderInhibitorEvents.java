package net.bobofraggins.mobfarmingsupplies.neoforge.enderinhibitor;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorEvents;

/**
 * NeoForge-specific listener that cancels natural Enderman teleports near an Ender Inhibitor.
 *
 * <p>No direct Architectury or Fabric API equivalent exists for this event;
 * see {@link EnderInhibitorEvents} for the Fabric TODO.
 *
 * <p>Registered on {@code NeoForge.EVENT_BUS} from
 * {@link net.bobofraggins.mobfarmingsupplies.neoforge.MobFarmingSupplies}.
 */
public final class NeoForgeEnderInhibitorEvents {

    private NeoForgeEnderInhibitorEvents() {}

    @SubscribeEvent
    public static void onEnderTeleport(EntityTeleportEvent.EnderEntity event) {
        if (EnderInhibitorEvents.inhibitorNearby(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
