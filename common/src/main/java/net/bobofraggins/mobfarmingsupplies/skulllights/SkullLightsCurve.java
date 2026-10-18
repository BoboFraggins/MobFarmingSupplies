package net.bobofraggins.mobfarmingsupplies.skulllights;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Where a String of Skull Lights hangs: a cord between its two hooks, sagging in a parabola, with
 * skulls hung from it at even spacing. Shared by the server (which puts light where the skulls
 * are) and the renderer, so both agree.
 */
public final class SkullLightsCurve {

    /** The longest a string may be, hook to hook, in blocks. */
    public static final double MAX_SPAN = 16.0;
    /** About how far apart the skulls hang along the cord, in blocks. */
    static final double SKULL_SPACING = 1.5;
    /** How far the cord dips at its middle: a fixed part plus a share of its length. */
    private static final double SAG_BASE = 0.15;
    private static final double SAG_PER_BLOCK = 0.08;
    /** How many straight pieces the cord is drawn and measured with. */
    static final int SEGMENTS = 48;
    /** How far below the cord a skull's centre hangs (a half-size skull is a quarter block tall). */
    public static final double SKULL_DROP = 0.125;

    /** A point on the cord where a skull hangs, and the cord's direction there. */
    public record Hang(int index, Vec3 point, Vec3 tangent) {
        public Vec3 skullCentre() {
            return point.subtract(0, SKULL_DROP, 0);
        }
    }

    private SkullLightsCurve() {}

    /** Where the cord ties onto a hook in {@code hookPos} that sticks out of its wall towards {@code facing}. */
    public static Vec3 attachPoint(BlockPos hookPos, Direction facing) {
        return Vec3.atCenterOf(hookPos).add(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(-0.3));
    }

    /** The point {@code t} (0 to 1) of the way along the cord from {@code a} to {@code b}. */
    public static Vec3 point(Vec3 a, Vec3 b, double t) {
        double sag = SAG_BASE + SAG_PER_BLOCK * a.distanceTo(b);
        return a.lerp(b, t).subtract(0, sag * 4 * t * (1 - t), 0);
    }

    /** The cord as {@link #SEGMENTS} + 1 points from {@code a} to {@code b}. */
    public static Vec3[] points(Vec3 a, Vec3 b) {
        Vec3[] points = new Vec3[SEGMENTS + 1];
        for (int i = 0; i <= SEGMENTS; i++) points[i] = point(a, b, (double) i / SEGMENTS);
        return points;
    }

    /** Where the skulls hang from {@code a} to {@code b}, evenly along the cord, none at the very ends. */
    public static List<Hang> hangs(Vec3 a, Vec3 b) {
        Vec3[] points = points(a, b);
        double[] along = new double[points.length];
        for (int i = 1; i < points.length; i++) along[i] = along[i - 1] + points[i].distanceTo(points[i - 1]);
        double length = along[points.length - 1];
        int count = (int) Math.max(1, Math.round(length / SKULL_SPACING));
        List<Hang> hangs = new ArrayList<>(count);
        int segment = 1;
        for (int k = 0; k < count; k++) {
            double target = (k + 0.5) * length / count;
            while (segment < points.length - 1 && along[segment] < target) segment++;
            double into = (target - along[segment - 1]) / Math.max(1e-6, along[segment] - along[segment - 1]);
            Vec3 point = points[segment - 1].lerp(points[segment], into);
            Vec3 tangent = points[segment].subtract(points[segment - 1]).normalize();
            hangs.add(new Hang(k, point, tangent));
        }
        return hangs;
    }
}
