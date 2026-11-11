package net.bobofraggins.mobfarmingsupplies.muffler;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * The Mufflers loaded on this client, and whether one covers a point. Sounds are played on each
 * client, so that's where they're muted: {@code SoundEngineMufflerMixin} asks {@link #muffles}
 * before playing a sound from somewhere in the world.
 *
 * <p>Holds no client classes, so the block entity can refer to it on a dedicated server. Weakly
 * held: a block entity dropped with its world without being removed doesn't linger.
 */
public final class MufflerSounds {

    private static final Set<MufflerBlockEntity> LOADED = Collections.newSetFromMap(new WeakHashMap<>());

    private MufflerSounds() {}

    static void add(MufflerBlockEntity muffler) {
        LOADED.add(muffler);
    }

    static void remove(MufflerBlockEntity muffler) {
        LOADED.remove(muffler);
    }

    /**
     * Whether a Muffler in {@code level} covers the point: within {@link MufflerBlockEntity#RANGE}
     * blocks of its area's centre (its position plus its offset) on every axis.
     */
    public static boolean muffles(Level level, double x, double y, double z) {
        if (LOADED.isEmpty() || level == null) return false;
        int bx = Mth.floor(x), by = Mth.floor(y), bz = Mth.floor(z);
        for (MufflerBlockEntity muffler : LOADED) {
            if (muffler.isRemoved() || muffler.getLevel() != level) continue;
            BlockPos pos = muffler.getBlockPos();
            int range = muffler.areaRange();
            if (Math.abs(pos.getX() + muffler.getOffsetX() - bx) <= range
                    && Math.abs(pos.getY() + muffler.getOffsetY() - by) <= range
                    && Math.abs(pos.getZ() + muffler.getOffsetZ() - bz) <= range) {
                return true;
            }
        }
        return false;
    }
}
