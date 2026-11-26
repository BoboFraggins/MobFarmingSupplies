package net.bobofraggins.mobfarmingsupplies.forge.enderinhibitor;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorEvents;

/**
 * NeoForge-specific listener that cancels natural Enderman teleports near an Ender Inhibitor.
 *
 * <p>No direct Architectury or Fabric API equivalent exists for this event;
 * see {@link EnderInhibitorEvents} for the Fabric TODO.
 *
 * <p>Registered on {@code MinecraftForge.EVENT_BUS} from
 * {@link net.bobofraggins.mobfarmingsupplies.forge.MobFarmingSupplies}.
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
