package net.bobofraggins.mobfarmingsupplies.magnet.neoforge;

import net.minecraft.world.entity.item.ItemEntity;

/** NeoForge side of {@code MagnetRemoteMovement}. */
public final class MagnetRemoteMovementImpl {

    private MagnetRemoteMovementImpl() {}

    public static boolean isBlocked(ItemEntity item) {
        return item.getPersistentData().contains("PreventRemoteMovement");
    }
}
