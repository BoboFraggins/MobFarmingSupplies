package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks active Ender Inhibitor positions and cancels Enderman natural teleports
 * that originate within {@value #RANGE} blocks (Chebyshev) of any inhibitor.
 *
 * <p>The inhibitor registry (INHIBITORS map + add/remove/check) lives here in common
 * and is populated by {@link EnderInhibitorBlockEntity} on load/unload.
 *
 * <p>Platform-specific teleport cancellation is wired in each loader's event handler:
 * NeoForge uses {@code NeoForgeEnderInhibitorEvents} (EntityTeleportEvent.EnderEntity);
 * Fabric uses a mixin ({@code EnderManMixin}) on {@code EnderMan#teleport(double, double, double)},
 * since no equivalent Fabric API event exists.
 */
public final class EnderInhibitorEvents {

    public static final int RANGE = 8;

    private EnderInhibitorEvents() {}

    // ── Inhibitor registry ──────────────────────────────────────────────────────

    private static final Map<ResourceKey<Level>, Set<BlockPos>> INHIBITORS = new HashMap<>();

    public static void addInhibitor(Level level, BlockPos pos) {
        INHIBITORS.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(pos.immutable());
    }

    public static void removeInhibitor(Level level, BlockPos pos) {
        Set<BlockPos> set = INHIBITORS.get(level.dimension());
        if (set != null) set.remove(pos);
    }

    public static boolean inhibitorNearby(Entity entity) {
        Set<BlockPos> inhibitors = INHIBITORS.get(entity.level().dimension());
        if (inhibitors == null || inhibitors.isEmpty()) return false;
        BlockPos center = entity.blockPosition();
        for (BlockPos inh : inhibitors) {
            if (Math.abs(inh.getX() - center.getX()) <= RANGE
                    && Math.abs(inh.getY() - center.getY()) <= RANGE
                    && Math.abs(inh.getZ() - center.getZ()) <= RANGE) {
                return true;
            }
        }
        return false;
    }

    // ── Common event registration ───────────────────────────────────────────────

    /**
     * Registers cross-platform events. Called from {@link net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon#init()}.
     * Platform-specific teleport cancellation is registered separately by each loader.
     */
    public static void registerCommonEvents() {
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(level -> INHIBITORS.remove(level.dimension()));
    }
}
