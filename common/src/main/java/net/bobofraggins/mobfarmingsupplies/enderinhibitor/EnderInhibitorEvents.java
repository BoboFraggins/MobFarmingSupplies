package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.world.entity.Entity;


/**
 * Tracks active Ender Inhibitor positions and cancels Enderman natural teleports
 * that originate within {@value #RANGE} blocks (Chebyshev) of any inhibitor's
 * (possibly offset) effective center.
 *
 * <p>Where inhibitors are is kept in {@link InhibitorRegistry#ENDER}, populated by
 * {@link EnderInhibitorBlockEntity} on load, unload and offset changes.
 *
 * <p>Platform-specific teleport cancellation is wired in each loader's event handler:
 * NeoForge uses {@code NeoForgeEnderInhibitorEvents} (EntityTeleportEvent.EnderEntity);
 * Fabric uses a mixin ({@code EnderManMixin}) on {@code EnderMan#teleport(double, double, double)},
 * since no equivalent Fabric API event exists.
 */
public final class EnderInhibitorEvents {

    public static final int RANGE = InhibitorRegistry.RANGE;

    private EnderInhibitorEvents() {}

    /** Whether an Ender Inhibitor covers {@code entity}. */
    public static boolean inhibitorNearby(Entity entity) {
        return InhibitorRegistry.ENDER.covers(entity);
    }

    // ── Common event registration ───────────────────────────────────────────────

    /**
     * Registers cross-platform events. Called from {@link net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon#init()}.
     * Platform-specific teleport cancellation is registered separately by each loader.
     */
    public static void registerCommonEvents() {
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(level -> {
            InhibitorRegistry.ENDER.clear(level);
            InhibitorRegistry.WITHER.clear(level);
        });
    }
}
