package net.bobofraggins.mobfarmingsupplies.wardeninhibitor;

import net.bobofraggins.mobfarmingsupplies.enderinhibitor.InhibitorRegistry;
import net.minecraft.world.entity.Entity;

/**
 * What a Warden Inhibitor stops, for a Warden inside its area: charging or firing its sonic boom,
 * and giving nearby players Darkness. Applied by {@code SonicBoomMixin} and {@code WardenMixin}, in
 * common code, so it works the same on both loaders.
 */
public final class WardenInhibitorEvents {

    private WardenInhibitorEvents() {}

    public static boolean inhibited(Entity warden) {
        return InhibitorRegistry.WARDEN.covers(warden);
    }
}
