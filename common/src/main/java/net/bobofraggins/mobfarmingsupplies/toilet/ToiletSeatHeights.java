package net.bobofraggins.mobfarmingsupplies.toilet;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

/**
 * Where a rider's feet go relative to a {@link ToiletSeatEntity}'s position (the seat point, at hip
 * height just above the bowl rim).
 *
 * <p>Vanilla describes how an entity rides with its vehicle attachment point, and those come in
 * two kinds. Humanoids carry a large one (from {@code ridingOffset}: 0.6 for illagers and players,
 * 0.7 for zombies, skeletons and piglins, 0.875 for wither skeletons) that drops them into a
 * bent-legged sitting pose; everything else has none, or a small one (spiders) meant for riding
 * something bigger. On a toilet:
 * <ul>
 *   <li>humanoids all sit with their hips on the seat point, exactly where a player sits, whatever
 *       their own offset - otherwise a zombie would sink 0.1 blocks lower than a player;</li>
 *   <li>everything else stands with its feet on the seat point, i.e. on the rim - a spider's small
 *       offset would otherwise sink it into the bowl;</li>
 *   <li>a few small or squishy mobs look better settled down into the bowl ({@link #INTO_BOWL}).</li>
 * </ul>
 */
final class ToiletSeatHeights {

    /** A seated player's vehicle attachment height: the reference every sitting humanoid is matched to. */
    private static final double HUMANOID_HIP_HEIGHT = 0.6;
    /** Riding offsets at least this deep belong to humanoids that sit with bent legs (a player's is 0.35). */
    private static final double SITTING_POSE_THRESHOLD = 0.3;

    /** Extra drop, in blocks, for mobs that look better nestled into the bowl than perched on its rim. */
    private static final Map<EntityType<?>, Double> INTO_BOWL = Map.of(
            EntityType.SLIME, -0.15,
            EntityType.MAGMA_CUBE, -0.15,
            EntityType.FROG, -0.1,
            EntityType.AXOLOTL, -0.1,
            EntityType.TURTLE, -0.1);

    private ToiletSeatHeights() {}

    /** Offset of {@code rider}'s feet from the seat's position, in blocks (negative = below). */
    static double feetOffset(Entity rider, ToiletSeatEntity seat) {
        // 1.20.1 has riding offsets (negative, below the seat) where 1.20.5+ has attachment heights.
        double attachment = -rider.getMyRidingOffset();
        double offset = attachment >= SITTING_POSE_THRESHOLD ? -HUMANOID_HIP_HEIGHT : 0.0;
        return offset + INTO_BOWL.getOrDefault(rider.getType(), 0.0);
    }
}
