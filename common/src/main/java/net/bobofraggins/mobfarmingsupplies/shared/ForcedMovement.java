package net.bobofraggins.mobfarmingsupplies.shared;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Moves an entity directly, for mobs that ignore a velocity push: squids on land cancel their own
 * horizontal velocity every tick, and flyers such as phantoms overwrite theirs from their AI.
 */
public final class ForcedMovement {

    private ForcedMovement() {}

    /**
     * Moves {@code entity} by ({@code dx}, {@code dy}, {@code dz}) without relying on its own
     * velocity, stopping at anything solid. Done in two half-steps so a thin block can't be
     * skipped. Uses setPos rather than Entity#move, which mustn't be called from a block's
     * entityInside (it would add to the movement list Minecraft is iterating when it calls us).
     */
    public static void displace(Level level, Entity entity, double dx, double dy, double dz) {
        double hx = dx / 2, hy = dy / 2, hz = dz / 2;
        for (int step = 0; step < 2; step++) {
            if (!level.noCollision(entity, entity.getBoundingBox().move(hx, hy, hz))) return;
            entity.setPos(entity.getX() + hx, entity.getY() + hy, entity.getZ() + hz);
        }
    }
}
