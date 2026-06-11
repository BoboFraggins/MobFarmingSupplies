package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import net.minecraft.core.Direction;

/**
 * Common interface for the Absorption Hopper block entity.
 *
 * <p>Exposed so common-side packet handlers and UI classes can interact with the
 * block entity without a direct dependency on the NeoForge-specific implementation.
 * {@link net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity}
 * implements this in the NeoForge subproject.
 */
public interface IAbsorptionHopperBlockEntity {

    void setPushSides(int mask);

    int getPushSides();

    void adjustOffset(int axis, int delta);

    /** Maps a side-mask bit index (0–5) to its absolute world {@link Direction}. */
    static Direction bitToWorldDir(int bit) {
        return switch (bit) {
            case 0 -> Direction.UP;
            case 1 -> Direction.DOWN;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.EAST;
            default -> Direction.WEST;
        };
    }
}
