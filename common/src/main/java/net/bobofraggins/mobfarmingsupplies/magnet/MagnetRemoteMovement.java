package net.bobofraggins.mobfarmingsupplies.magnet;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Whether another mod has marked a dropped item as not to be moved from afar (on NeoForge, the
 * common {@code PreventRemoteMovement} entry in its persistent data; Fabric has no such
 * convention).
 */
public final class MagnetRemoteMovement {

    private MagnetRemoteMovement() {}

    @ExpectPlatform
    public static boolean isBlocked(ItemEntity item) {
        throw new AssertionError("Missing platform implementation");
    }
}
