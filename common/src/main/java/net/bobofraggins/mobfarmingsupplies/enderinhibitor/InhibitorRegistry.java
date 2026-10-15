package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Where one kind of inhibitor (Ender or Wither) is active: each loaded inhibitor's block position,
 * mapped to the centre of the area it protects (its position plus its offset), per dimension. Lets
 * a mob check in a few comparisons, rather than a block scan, whether an inhibitor covers it.
 */
public final class InhibitorRegistry {

    /** How far an inhibitor reaches from the centre of its area, in each direction. */
    public static final int RANGE = 8;

    public static final InhibitorRegistry ENDER = new InhibitorRegistry();
    public static final InhibitorRegistry WITHER = new InhibitorRegistry();

    private final Map<ResourceKey<Level>, Map<BlockPos, BlockPos>> inhibitors = new HashMap<>();

    private InhibitorRegistry() {}

    /** Records (or moves) the inhibitor at {@code pos}, covering the area around {@code center}. */
    public void add(Level level, BlockPos pos, BlockPos center) {
        inhibitors.computeIfAbsent(level.dimension(), k -> new HashMap<>()).put(pos.immutable(), center.immutable());
    }

    public void remove(Level level, BlockPos pos) {
        Map<BlockPos, BlockPos> map = inhibitors.get(level.dimension());
        if (map != null) map.remove(pos);
    }

    public void clear(Level level) {
        inhibitors.remove(level.dimension());
    }

    /** Whether an inhibitor's area covers {@code entity}. */
    public boolean covers(Entity entity) {
        Map<BlockPos, BlockPos> map = inhibitors.get(entity.level().dimension());
        if (map == null || map.isEmpty()) return false;
        BlockPos point = entity.blockPosition();
        for (BlockPos center : map.values()) {
            if (Math.abs(center.getX() - point.getX()) <= RANGE
                    && Math.abs(center.getY() - point.getY()) <= RANGE
                    && Math.abs(center.getZ() - point.getZ()) <= RANGE) {
                return true;
            }
        }
        return false;
    }
}
