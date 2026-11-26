package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.world.item.DyeColor;

/** Colour helpers 1.20.5+ has and 1.20.1 doesn't. */
public final class Colors {

    private Colors() {}

    /** A dye's texture tint as opaque ARGB (1.20.5+'s {@code DyeColor.getTextureDiffuseColor()}). */
    public static int diffuse(DyeColor color) {
        float[] c = color.getTextureDiffuseColors();
        return 0xFF000000 | Math.round(c[0] * 255) << 16 | Math.round(c[1] * 255) << 8 | Math.round(c[2] * 255);
    }

    public static float red(int argb) {
        return (argb >> 16 & 0xFF) / 255f;
    }

    public static float green(int argb) {
        return (argb >> 8 & 0xFF) / 255f;
    }

    public static float blue(int argb) {
        return (argb & 0xFF) / 255f;
    }

    public static float alpha(int argb) {
        return (argb >>> 24) / 255f;
    }
}
