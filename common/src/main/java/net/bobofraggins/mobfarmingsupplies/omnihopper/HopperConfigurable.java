package net.bobofraggins.mobfarmingsupplies.omnihopper;

/**
 * The settings the Omnidirectional Hopper screen edits — implemented by the hopper and the
 * Einstein-Rosen Bridge, so both share the screen and its config packet.
 */
public interface HopperConfigurable {

    /** The six side modes, 2 bits each, indexed by {@code Direction#get3DDataValue}. */
    int packedSides();

    boolean isAndMode();

    void setConfig(int packedSides, boolean andMode);
}
