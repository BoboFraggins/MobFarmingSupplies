package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import net.minecraft.util.StringRepresentable;

/**
 * What a Logistic Sorter does on one of its six sides. Clicking a side in the UI cycles
 * NONE → INPUT → MATCH → NO_MATCH → NONE.
 */
public enum SideMode implements StringRepresentable {
    /** Not attached. */
    NONE("none", 0),
    /** Items come in here — pulled from the adjacent inventory, or pushed in by it. */
    INPUT("input", 0xFF3C6EE6),
    /** Items that pass the filters go out here. */
    MATCH("match", 0xFF3CB43C),
    /** Items that fail the filters go out here. */
    NO_MATCH("no_match", 0xFFD23C3C);

    private final String name;
    private final int color;

    SideMode(String name, int color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String getSerializedName() { return name; }

    /** ARGB colour for this mode's UI button and connector (unused for NONE). */
    public int color() { return color; }

    public SideMode next() { return values()[(ordinal() + 1) % values().length]; }

    public boolean isOutput() { return this == MATCH || this == NO_MATCH; }

    public static SideMode byOrdinal(int ordinal) {
        SideMode[] v = values();
        return ordinal >= 0 && ordinal < v.length ? v[ordinal] : NONE;
    }
}
