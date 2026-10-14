package net.bobofraggins.mobfarmingsupplies.magnet.fabric;

import net.minecraft.world.entity.item.ItemEntity;

/** Fabric side of {@code MagnetRemoteMovement}: Fabric has no such convention. */
public final class MagnetRemoteMovementImpl {

    private MagnetRemoteMovementImpl() {}

    public static boolean isBlocked(ItemEntity item) {
        return false;
    }
}
