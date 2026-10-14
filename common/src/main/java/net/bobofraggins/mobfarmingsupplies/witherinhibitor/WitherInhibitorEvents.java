package net.bobofraggins.mobfarmingsupplies.witherinhibitor;

import net.bobofraggins.mobfarmingsupplies.enderinhibitor.InhibitorRegistry;
import net.minecraft.world.entity.Entity;

/**
 * What a Wither Inhibitor stops, for a Wither inside its area: showing its boss bar, the explosion
 * when it finishes being summoned, and firing its skulls. Applied by {@code WitherBossMixin}, in
 * common code, so it works the same on both loaders.
 */
public final class WitherInhibitorEvents {

    private WitherInhibitorEvents() {}

    public static boolean inhibited(Entity wither) {
        return InhibitorRegistry.WITHER.covers(wither);
    }
}
