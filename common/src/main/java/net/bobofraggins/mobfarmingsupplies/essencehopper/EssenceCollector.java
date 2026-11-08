package net.bobofraggins.mobfarmingsupplies.essencehopper;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Sends a dying mob's essence to the nearest Essence Hopper that can take it, with a trail of
 * particles from the mob to the hopper. Each loaded hopper records itself here, so a death only
 * has to look at hoppers in reach. Common code (Architectury's death event), for both loaders.
 */
public final class EssenceCollector {

    /** A hopper further than this, on any axis, can't have the mob in its zone: its offset, plus the zone. */
    private static final int MAX_REACH = 12;
    private static final int PARTICLES = 12;
    private static final int MIN_FLIGHT_TICKS = 15, MAX_FLIGHT_TICKS = 30;

    private static final Map<ResourceKey<Level>, Set<BlockPos>> HOPPERS = new HashMap<>();

    private EssenceCollector() {}

    public static void register() {
        EntityEvent.LIVING_DEATH.register(EssenceCollector::onDeath);
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(level -> HOPPERS.remove(level.dimension()));
    }

    static void add(Level level, BlockPos pos) {
        HOPPERS.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(pos.immutable());
    }

    static void remove(Level level, BlockPos pos) {
        Set<BlockPos> hoppers = HOPPERS.get(level.dimension());
        if (hoppers != null) hoppers.remove(pos);
    }

    private static EventResult onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level)) return EventResult.pass();
        Essence essence = Essence.of(entity.getType());
        Set<BlockPos> hoppers = HOPPERS.get(level.dimension());
        if (essence == null || hoppers == null) return EventResult.pass();

        BlockPos at = entity.blockPosition();
        EssenceHopperBlockEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (BlockPos pos : hoppers) {
            if (Math.abs(pos.getX() - at.getX()) > MAX_REACH || Math.abs(pos.getY() - at.getY()) > MAX_REACH
                    || Math.abs(pos.getZ() - at.getZ()) > MAX_REACH || !level.isLoaded(pos)) continue;
            double dist = pos.distSqr(at);
            if (dist < nearestDist && level.getBlockEntity(pos) instanceof EssenceHopperBlockEntity hopper
                    && hopper.canCollect(essence, entity)) {
                nearest = hopper;
                nearestDist = dist;
            }
        }
        if (nearest != null) {
            nearest.collect(essence);
            sendTrail(level, entity, Vec3.atCenterOf(nearest.getBlockPos()), essence);
        }
        return EventResult.pass();
    }

    private static void sendTrail(ServerLevel level, LivingEntity from, Vec3 target, Essence essence) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < PARTICLES; i++) {
            int ticks = MIN_FLIGHT_TICKS + random.nextInt(MAX_FLIGHT_TICKS - MIN_FLIGHT_TICKS + 1);
            level.sendParticles(new EssenceTrailOptions(target, essence.particleColor(random), ticks),
                    from.getRandomX(0.5), from.getRandomY(), from.getRandomZ(0.5), 1, 0, 0, 0, 0);
        }
    }
}
