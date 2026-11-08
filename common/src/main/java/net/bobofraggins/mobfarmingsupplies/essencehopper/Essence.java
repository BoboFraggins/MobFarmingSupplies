package net.bobofraggins.mobfarmingsupplies.essencehopper;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * The essences an Essence Hopper can extract. Each one registers a fluid, a bucket and an Essence
 * Filter from here (see {@code Registration}); its art comes from
 * {@code blockbench/essence_assets.py} and its filter's recipe is an {@code upgrading} recipe.
 */
public enum Essence {
    VENOM("venom", EntityType.SPIDER, 0xFF1F6B2E);

    private final String id;
    private final EntityType<?> mob;
    private final int color;

    Essence(String id, EntityType<?> mob, int color) {
        this.id = id;
        this.mob = mob;
        this.color = color;
    }

    /** The registry id of its fluid and fluid block. */
    public String fluidId() { return "essence_of_" + id; }

    public String bucketId() { return fluidId() + "_bucket"; }

    public String filterId() { return "essence_filter_" + id; }

    /** ARGB, for its particles and the Essence Hopper's gauge. */
    public int color() { return color; }

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
