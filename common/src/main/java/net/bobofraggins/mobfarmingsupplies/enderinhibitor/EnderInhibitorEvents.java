package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks active Ender Inhibitor positions and cancels Enderman natural teleports
 * that originate within {@value #RANGE} blocks (Chebyshev) of any inhibitor's
 * (possibly offset) effective center.
 *
 * <p>The inhibitor registry (INHIBITORS map + add/remove/check) lives here in common
 * and is populated by {@link EnderInhibitorBlockEntity} on load/unload/offset-change.
 * Each inhibitor is keyed by its block position, with the value being the effective
 * center of its suppression area (block position + configured offset).
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

    private static final Map<ResourceKey<Level>, Map<BlockPos, BlockPos>> INHIBITORS = new HashMap<>();

    /** Registers (or updates) an inhibitor's effective suppression center. */
    public static void addInhibitor(Level level, BlockPos pos, BlockPos center) {
        INHIBITORS.computeIfAbsent(level.dimension(), k -> new HashMap<>())
                .put(pos.immutable(), center.immutable());
    }

    public static void removeInhibitor(Level level, BlockPos pos) {
        Map<BlockPos, BlockPos> map = INHIBITORS.get(level.dimension());
        if (map != null) map.remove(pos);
    }

    public static boolean inhibitorNearby(Entity entity) {
        Map<BlockPos, BlockPos> inhibitors = INHIBITORS.get(entity.level().dimension());
        if (inhibitors == null || inhibitors.isEmpty()) return false;
        BlockPos point = entity.blockPosition();
        for (BlockPos center : inhibitors.values()) {
            if (Math.abs(center.getX() - point.getX()) <= RANGE
                    && Math.abs(center.getY() - point.getY()) <= RANGE
                    && Math.abs(center.getZ() - point.getZ()) <= RANGE) {
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
