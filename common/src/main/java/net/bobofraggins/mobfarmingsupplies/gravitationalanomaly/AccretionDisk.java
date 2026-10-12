package net.bobofraggins.mobfarmingsupplies.gravitationalanomaly;

/**
 * Where the accretion disk appears, and how bright, when the black hole is seen from a given
 * direction. Pure maths, with nothing from Minecraft, so it can be tried out on its own.
 *
 * <p>Units are pixels, centred on the black hole, with the disk in the y = 0 plane turning
 * clockwise seen from above. The black sphere is the hole's shadow: light passing closer than its
 * radius falls in. Everything else is placed where a Schwarzschild black hole would show it. Light
 * from each point of the disk reaches the viewer twice: directly (the primary image, which lifts
 * the far side of the disk over the top of the shadow) and after swinging round the far side of
 * the hole (the secondary image, a thin ring hugging the shadow). Both are flat images on the
 * plane through the centre facing the viewer.
 *
 * <p>The disk's true radii are chosen so that, seen from straight above, it spans exactly from
 * the shadow's edge to {@link #OUTER_RADIUS}.
 */
public final class AccretionDisk {

    /** The shadow's radius: the black sphere, 8 pixels across. */
    public static final float SHADOW_RADIUS = 4f;
    /** The disk's outer radius seen from straight above: 36 pixels across. */
    public static final float OUTER_RADIUS = 18f;
    /** Rings of vertices from the inner edge to the outer, and vertices around each. */
    public static final int RINGS = 13;
    public static final int SEGMENTS = 128;

    // Light bending is worked out in units of the Schwarzschild radius, rs.
    private static final double CRITICAL_IMPACT = 1.5 * Math.sqrt(3);
    private static final double PX_PER_RS = SHADOW_RADIUS / CRITICAL_IMPACT;
    /** Light that has swung round further than this is as good as on the shadow's edge. */
    private static final double MAX_SWEEP = 2 * Math.PI + 0.5;
    private static final int SAMPLES = 90;

    /** How strongly the disk's motion brightens its approaching side (0 for none). */
    private static final double BEAMING = 0.6;
    /** Secondary images are dimmer. */
    private static final float SECONDARY_DIM = 0.8f;
    /**
     * The disk has some thickness: within this of edge-on (sine of the angle) both its top and
     * its underside show, so the lensed far side arcs over the shadow and under it at once.
     */
    private static final float THICKNESS = (float) Math.sin(Math.toRadians(7));
    /** How much closer together vertices are round the back of the disk, which lensing stretches. */
    private static final double BACK_CROWDING = 0.75;

    /** Each ring's true radius (rs), and its light-bending table: sweep → impact (px), cos emission. */
    private static final double[] RADIUS = new double[RINGS];
    private static final double[][] SWEEP = new double[RINGS][];
    private static final double[][] IMPACT = new double[RINGS][];
    private static final double[][] COS_EMIT = new double[RINGS][];

    /** Tracing the light takes a moment, so it's done once, off the render thread. */
    private static final java.util.concurrent.CompletableFuture<Void> TABLES =
            java.util.concurrent.CompletableFuture.runAsync(AccretionDisk::buildTables);

    private AccretionDisk() {}

    /** Starts working out the light bending, if it hasn't started already. */
    public static void warmUp() {}

    /** Whether the light bending has been worked out; until then there's nothing to draw. */
    public static boolean ready() {
        return TABLES.isDone() && !TABLES.isCompletedExceptionally();
    }

    private static void buildTables() {
        double inner = radiusSeenAt(SHADOW_RADIUS / PX_PER_RS);
        double outer = radiusSeenAt(OUTER_RADIUS / PX_PER_RS);
        for (int i = 0; i < RINGS; i++) {
            RADIUS[i] = inner + (outer - inner) * i / (RINGS - 1);
            buildTable(i);
        }
    }

    /** One image of the disk: its vertices ring by ring, see {@link #index}. */
    public static final class Image {
        /** Whether this is a secondary image, drawn under the primary ones. */
        public final boolean secondary;
        /** x, y, z of each vertex, in pixels from the centre. */
        public final float[] position = new float[RINGS * SEGMENTS * 3];
        /** The disk's colour and opacity (ARGB), for blending over what's behind. */
        public final int[] colour = new int[RINGS * SEGMENTS];
        /** The glow added on top for the swirls (ARGB, alpha unused). */
        public final int[] glow = new int[RINGS * SEGMENTS];

        Image(boolean secondary) {
            this.secondary = secondary;
        }
    }

    /** The disk from one direction: its images, secondaries first, and each segment's angle round it. */
    public static final class View {
        public final java.util.List<Image> images = new java.util.ArrayList<>();
        /** The angle round the disk of each segment, radians, increasing clockwise seen from above. */
        public final float[] angle = new float[SEGMENTS];
    }

    /** Index of a vertex in an {@link Image}'s arrays. */
    public static int index(int ring, int segment) {
        return ring * SEGMENTS + segment;
    }

    /**
     * The disk seen from direction (ox, oy, oz), a unit vector from the centre to the viewer.
     * Call only once {@link #ready()}.
     */
    public static View view(float ox, float oy, float oz) {
        View view = new View();
        // Segments crowd round the back of the disk, away from the viewer.
        double front = Math.atan2(oz, ox);
        for (int seg = 0; seg < SEGMENTS; seg++) {
            double t = 2 * Math.PI * seg / SEGMENTS;
            view.angle[seg] = (float) (front + t + BACK_CROWDING * Math.sin(t));
        }
        float flat = (float) Math.sqrt(Math.max(0, 1 - oy * oy));
        float edge = (float) Math.sqrt(1 - THICKNESS * THICKNESS);
        // Seen from (nearly) edge-on, the top and underside each show as if from just above or below.
        boolean top = oy > -THICKNESS, under = oy < THICKNESS;
        Image[] primary = new Image[2], secondary = new Image[2];
        for (int side = 0; side < 2; side++) {
            if (side == 0 ? !top : !under) continue;
            float y = oy, scale = 1;
            if (Math.abs(oy) < THICKNESS) {
                y = side == 0 ? THICKNESS : -THICKNESS;
                scale = flat < 1e-6 ? 0 : edge / flat;
            }
            primary[side] = new Image(false);
            secondary[side] = new Image(true);
            fill(view, ox * scale, y, oz * scale, primary[side], secondary[side]);
        }
        for (Image image : secondary) if (image != null) view.images.add(image);
        for (Image image : primary) if (image != null) view.images.add(image);
        return view;
    }

    private static void fill(View view, double ox, double oy, double oz, Image primary, Image secondary) {
        for (int ring = 0; ring < RINGS; ring++) {
            float s = (float) ring / (RINGS - 1);
            double speed = BEAMING * orbitSpeed(RADIUS[ring]);
            for (int seg = 0; seg < SEGMENTS; seg++) {
                double phi = view.angle[seg];
                double px = Math.cos(phi), pz = Math.sin(phi);
                double cosPsi = Math.max(-1, Math.min(1, px * ox + pz * oz));
                double psi = Math.acos(cosPsi);
                // The direction, across the viewer's sky, from the centre toward this point.
                double qx = px - cosPsi * ox, qy = -cosPsi * oy, qz = pz - cosPsi * oz;
                double qLength = Math.sqrt(qx * qx + qy * qy + qz * qz);
                if (qLength < 1e-6) {
                    // Straight behind or in front: any direction across the sky will do.
                    qx = -oz; qy = 0; qz = ox;
                    qLength = Math.max(1e-6, Math.sqrt(qx * qx + qz * qz));
                }
                qx /= qLength; qy /= qLength; qz /= qLength;
                // The way light sets off from the point, across its line of sight, toward the viewer.
                double tx = ox - cosPsi * px, ty = oy, tz = oz - cosPsi * pz;
                double tLength = Math.sqrt(tx * tx + ty * ty + tz * tz);
                if (tLength < 1e-6) {
                    tx = qx; tz = qz;
                } else {
                    tx /= tLength; tz /= tLength;
                }
                for (int n = 0; n < 2; n++) {
                    Image image = n == 0 ? primary : secondary;
                    double sweep = n == 0 ? psi : 2 * Math.PI - psi;
                    double sign = n == 0 ? 1 : -1;
                    double[] hit = lookup(ring, sweep);
                    double impact = hit[0], cosEmit = hit[1];
                    int i = index(ring, seg);
                    image.position[i * 3] = (float) (sign * impact * qx);
                    image.position[i * 3 + 1] = (float) (sign * impact * qy);
                    image.position[i * 3 + 2] = (float) (sign * impact * qz);

                    // The direction the light leaves in, and how much the gas's motion along it
                    // brightens and heats it. The gas moves clockwise seen from above.
                    double sinEmit = Math.sqrt(Math.max(0, 1 - cosEmit * cosEmit));
                    double kx = cosEmit * px + sinEmit * sign * tx;
                    double kz = cosEmit * pz + sinEmit * sign * tz;
                    double toward = -Math.sin(phi) * kx + Math.cos(phi) * kz;
                    double shift = 1 / (1 - speed * toward);
                    float brightness = (float) Math.max(0.35, Math.min(2.0, Math.pow(shift, 2.5)));
                    float heat = (float) Math.max(0, Math.min(1, s - 0.5 * Math.log(shift)));
                    float dim = n == 0 ? 1 : SECONDARY_DIM;

                    float alpha = opacity(s) * (0.8f + 0.2f * Math.min(1, brightness)) * dim;
                    float[] rgb = ramp(heat);
                    float base = 0.85f * Math.min(1, brightness);
                    image.colour[i] = argb(alpha, rgb[0] * base, rgb[1] * base, rgb[2] * base);
                    float glow = 0.7f * opacity(s) * brightness * dim;
                    image.glow[i] = argb(1, rgb[0] * glow, rgb[1] * glow, rgb[2] * glow);
                }
            }
        }
    }

    /** How fast gas orbits at radius r (rs), as a fraction of light speed. */
    private static double orbitSpeed(double r) {
        return Math.min(0.7, Math.sqrt(0.5 / (r - 1)));
    }

    /** Bright yellow at the inner edge, through orange, to red at the outer. */
    private static float[] ramp(float s) {
        float[] yellow = {1f, 0.88f, 0.28f}, orange = {1f, 0.53f, 0.12f}, red = {0.78f, 0.14f, 0.08f};
        float[] a = s < 0.5f ? yellow : orange, b = s < 0.5f ? orange : red;
        float t = s < 0.5f ? s * 2 : (s - 0.5f) * 2;
        return new float[] {a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
    }

    /** Opaque at the inner edge, fading to nothing at the outer. */
    private static float opacity(float s) {
        if (s <= 0.1f) return 1f;
        return (float) (0.5 + 0.5 * Math.cos(Math.PI * (s - 0.1) / 0.9));
    }

    private static int argb(float a, float r, float g, float b) {
        return channel(a) << 24 | channel(r) << 16 | channel(g) << 8 | channel(b);
    }

    private static int channel(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255)));
    }

    // ── Light bending ─────────────────────────────────────────────────────────────

    /** {impact (px), cos emission angle} for light from ring {@code ring} that sweeps round {@code sweep}. */
    private static double[] lookup(int ring, double sweep) {
        double[] sweeps = SWEEP[ring];
        int last = sweeps.length - 1;
        if (sweep >= sweeps[last]) return new double[] {IMPACT[ring][last], COS_EMIT[ring][last]};
        int lo = 0, hi = last;
        while (hi - lo > 1) {
            int mid = (lo + hi) >>> 1;
            if (sweeps[mid] <= sweep) lo = mid;
            else hi = mid;
        }
        double t = (sweep - sweeps[lo]) / Math.max(1e-12, sweeps[hi] - sweeps[lo]);
        return new double[] {IMPACT[ring][lo] + (IMPACT[ring][hi] - IMPACT[ring][lo]) * t,
                COS_EMIT[ring][lo] + (COS_EMIT[ring][hi] - COS_EMIT[ring][lo]) * t};
    }

    /**
     * Light leaving radius r: first heading outward with impact parameter rising from 0 to its
     * most, then heading inward (round the far side) with it falling toward the critical value.
     * Over that range the sweep rises steadily from 0, so it can be looked up.
     */
    private static void buildTable(int ring) {
        double r = RADIUS[ring];
        double most = r / Math.sqrt(1 - 1 / r);
        double[] sweeps = new double[2 * SAMPLES + 1];
        double[] impacts = new double[sweeps.length];
        double[] cosines = new double[sweeps.length];
        int n = 0;
        for (int k = 0; k <= SAMPLES; k++) {
            double angle = Math.PI / 2 * k / SAMPLES;
            sweeps[n] = k == 0 ? 0 : sweep(r, most * Math.sin(angle), false);
            impacts[n] = most * Math.sin(angle) * PX_PER_RS;
            cosines[n++] = Math.cos(angle);
        }
        for (int k = 1; k <= SAMPLES; k++) {
            double b = CRITICAL_IMPACT + (most - CRITICAL_IMPACT) * Math.pow(10, -7.0 * k / SAMPLES);
            double sweep = sweep(r, b, true);
            if (Double.isNaN(sweep)) break;
            sweeps[n] = Math.min(sweep, MAX_SWEEP);
            impacts[n] = b * PX_PER_RS;
            cosines[n++] = -Math.sqrt(Math.max(0, 1 - (b / most) * (b / most)));
            if (sweep >= MAX_SWEEP) break;
        }
        for (int k = 1; k < n; k++) sweeps[k] = Math.max(sweeps[k], sweeps[k - 1] + 1e-9);
        SWEEP[ring] = java.util.Arrays.copyOf(sweeps, n);
        IMPACT[ring] = java.util.Arrays.copyOf(impacts, n);
        COS_EMIT[ring] = java.util.Arrays.copyOf(cosines, n);
    }

    /** The true radius (rs) whose ring, seen from straight above, appears at {@code impact} (rs). */
    private static double radiusSeenAt(double impact) {
        double lo = 1.55, hi = 30;
        for (int i = 0; i < 40; i++) {
            double mid = (lo + hi) / 2;
            if (impactSeenFromAbove(mid) < impact) lo = mid;
            else hi = mid;
        }
        return (lo + hi) / 2;
    }

    /** The impact parameter (rs) of light from radius r that reaches a viewer straight above. */
    private static double impactSeenFromAbove(double r) {
        double lo = 0, hi = r / Math.sqrt(1 - 1 / r);
        for (int i = 0; i < 40; i++) {
            double mid = (lo + hi) / 2;
            if (sweep(r, mid, false) < Math.PI / 2) lo = mid;
            else hi = mid;
        }
        return (lo + hi) / 2;
    }

    /**
     * How far round (radians) light leaving radius r (rs) with impact parameter b swings before it
     * reaches a distant viewer, or NaN if it falls in. Traced with the usual trick for light near a
     * Schwarzschild black hole: a straight line bent by a pull of 1.5 h² / r⁴.
     */
    private static double sweep(double r, double b, boolean inward) {
        double radial = Math.sqrt(Math.max(0, r * r * r * r - b * b * (r * r - r)));
        double vx = inward ? -radial : radial, vy = r * b;
        double length = Math.hypot(vx, vy);
        vx /= length;
        vy /= length;
        double x = r, y = 0;
        double pull = 1.5 * (x * vy - y * vx) * (x * vy - y * vx);
        double turned = Math.atan2(vy, vx);
        double[] s = {x, y, vx, vy};
        double[] k1 = new double[4], k2 = new double[4], k3 = new double[4], k4 = new double[4], t = new double[4];
        for (int step = 0; step < 400_000; step++) {
            double d = Math.hypot(s[0], s[1]);
            if (d < 1.0005) return Double.NaN;
            if (d > 300 && s[0] * s[2] + s[1] * s[3] > 0) break;
            double h = 0.01 * d / Math.hypot(s[2], s[3]);
            double pvx = s[2], pvy = s[3];
            derivative(s, pull, k1);
            for (int j = 0; j < 4; j++) t[j] = s[j] + h / 2 * k1[j];
            derivative(t, pull, k2);
            for (int j = 0; j < 4; j++) t[j] = s[j] + h / 2 * k2[j];
            derivative(t, pull, k3);
            for (int j = 0; j < 4; j++) t[j] = s[j] + h * k3[j];
            derivative(t, pull, k4);
            for (int j = 0; j < 4; j++) s[j] += h / 6 * (k1[j] + 2 * k2[j] + 2 * k3[j] + k4[j]);
            turned += Math.atan2(pvx * s[3] - pvy * s[2], pvx * s[2] + pvy * s[3]);
            if (turned > MAX_SWEEP) return MAX_SWEEP;
        }
        return turned;
    }

    private static void derivative(double[] s, double pull, double[] out) {
        double d2 = s[0] * s[0] + s[1] * s[1];
        double a = -pull / (d2 * d2 * Math.sqrt(d2));
        out[0] = s[2];
        out[1] = s[3];
        out[2] = a * s[0];
        out[3] = a * s[1];
    }
}
