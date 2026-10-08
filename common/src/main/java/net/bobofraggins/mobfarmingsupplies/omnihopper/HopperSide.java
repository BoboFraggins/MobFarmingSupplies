package net.bobofraggins.mobfarmingsupplies.omnihopper;

import net.minecraft.util.StringRepresentable;

/**
 * What an Omnidirectional Hopper does on one of its six sides, for every kind of resource at once.
 * Clicking a side in the UI cycles NONE → INPUT → OUTPUT → NONE.
 */
public enum HopperSide implements StringRepresentable {
    /** Not attached. */
    NONE("none", 0),
    /** Resources come in here — pulled from the neighbour, or pushed in by it. */
    INPUT("input", 0xFF3C6EE6),
    /** Resources go out here. */
    OUTPUT("output", 0xFF3CB43C);

    private final String name;
    private final int color;

    HopperSide(String name, int color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String getSerializedName() { return name; }

    /** ARGB colour for this mode's UI button and connector (unused for NONE). */
    public int color() { return color; }

    public HopperSide next() { return values()[(ordinal() + 1) % values().length]; }

    public static HopperSide byOrdinal(int ordinal) {
        HopperSide[] v = values();
        return ordinal >= 0 && ordinal < v.length ? v[ordinal] : NONE;
    }
}
