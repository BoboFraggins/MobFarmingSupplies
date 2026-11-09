package net.bobofraggins.mobfarmingsupplies.essencehopper;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * The essences an Essence Hopper can extract. Each one registers a fluid, a bucket and an Essence
 * Filter from here (see {@code Registration}); its art comes from
 * {@code blockbench/essence_assets.py} and its filter's recipe is an {@code upgrading} recipe.
 */
public enum Essence {
    AIR("air", EntityType.BREEZE, 0xFF87CEEB),
    BACON("bacon", EntityType.PIG, 0xFFFFA6C1),
    COLOR("color", EntityType.SHEEP, RainbowColor.RAINBOW),
    FIRE("fire", EntityType.BLAZE, 0xFFE8281E),
    FROST("frost", EntityType.SNOW_GOLEM, 0xFF9BD6FF),
    GROWTH("growth", EntityType.COW, 0xFFFFB627),
    SIGHT("sight", EntityType.BAT, 0xFFF2F2F2),
    SPACETIME("spacetime", EntityType.ENDERMAN, 0xFF8A2BE2),
    UNDEATH("undeath", EntityType.ZOMBIE, 0xFF7BE02A),
    VENOM("venom", EntityType.SPIDER, 0xFF1F6B2E),
    WATER("water", EntityType.SQUID, 0xFF2F7BFF),
    WITHER("wither", EntityType.WITHER_SKELETON, 0xFF4A4A4E);

    /** Holds the marker colour, since an enum's constants can't refer to its own static fields. */
    private static final class RainbowColor {
        /** ARGB with no alpha, which no real essence colour has: this essence is every colour. */
        static final int RAINBOW = 0;
    }

    /** How long the gauge of a rainbow essence takes to go through every hue. */
    private static final long RAINBOW_CYCLE_MILLIS = 6000L;

    private final String id;
    private final EntityType<?> mob;
    private final int color;

    Essence(String id, EntityType<?> mob, int color) {
        this.id = id;
        this.mob = mob;
        this.color = color;
    }

    /** The mob it comes from. */
    public EntityType<?> mob() { return mob; }

    /** The registry id of its fluid and fluid block. */
    public String fluidId() { return "essence_of_" + id; }

    public String bucketId() { return fluidId() + "_bucket"; }

    public String filterId() { return "essence_filter_" + id; }

    /** ARGB, for one of its particles: a rainbow essence's are each a random hue. */
    public int particleColor(RandomSource random) {
        return color == RainbowColor.RAINBOW ? hue(random.nextFloat()) : color;
    }

    /** ARGB, for the Essence Hopper's gauge right now: a rainbow essence's cycles through the hues. */
    public int gaugeColor() {
        return color == RainbowColor.RAINBOW
                ? hue((Util.getMillis() % RAINBOW_CYCLE_MILLIS) / (float) RAINBOW_CYCLE_MILLIS)
                : color;
    }

    private static int hue(float hue) {
        return 0xFF000000 | Mth.hsvToRgb(hue, 0.85f, 1f);
    }

    /** The essence {@code mob} yields, or null if none. */
    @Nullable
    public static Essence of(EntityType<?> mob) {
        for (Essence essence : values()) {
            if (essence.mob == mob) return essence;
        }
        return null;
    }

    /** The essence {@code fluid} holds, or null if it isn't one. */
    @Nullable
    public static Essence of(FluidStack fluid) {
        for (Essence essence : values()) {
            if (Registration.ESSENCE_SOURCES.get(essence).get() == fluid.getFluid()) return essence;
        }
        return null;
    }
}
