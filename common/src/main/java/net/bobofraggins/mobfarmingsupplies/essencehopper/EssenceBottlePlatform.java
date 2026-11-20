package net.bobofraggins.mobfarmingsupplies.essencehopper;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Bottles of essence against any block's fluid inventory (capability/storage), on either loader. */
public final class EssenceBottlePlatform {

    private EssenceBottlePlatform() {}

    /**
     * Takes a bottle's worth ({@link EssenceBottleItem#MB}) of an essence out of the fluid
     * inventory at {@code pos}, seen from {@code side}.
     *
     * @return the essence taken, or null if it holds no essence it would give a whole bottle of
     */
    @ExpectPlatform
    @Nullable
    public static Essence extractBottle(Level level, BlockPos pos, Direction side) {
        throw new AssertionError("Missing platform implementation");
    }

    /**
     * Puts a bottle's worth of {@code essence} into the fluid inventory at {@code pos}, seen from
     * {@code side}, if it takes all of it.
     */
    @ExpectPlatform
    public static boolean insertBottle(Level level, BlockPos pos, Direction side, Essence essence) {
        throw new AssertionError("Missing platform implementation");
    }
}
