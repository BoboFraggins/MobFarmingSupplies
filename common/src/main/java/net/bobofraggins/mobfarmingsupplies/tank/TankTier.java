package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.Codec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.util.StringRepresentable;

/**
 * Tank tiers. Each holds the configured multiplier times the one below (see {@link TankCapacities}).
 * A tank is upgraded by surrounding it with the next tier's material (see {@link TankUpgradeRecipe}), keeping its contents.
 */
public enum TankTier implements StringRepresentable {
    BASIC("basic"),
    GOLD("gold"),
    DIAMOND("diamond"),
    EMERALD("emerald");

    public static final Codec<TankTier> CODEC = StringRepresentable.fromEnum(TankTier::values);

    private final String name;

    TankTier(String name) {
        this.name = name;
    }

    /** Capacity in mB. */
    public long capacity() {
        return TankCapacities.of(this);
    }

    /** This tier's tank block. Only call after registration (e.g. at render time). */
    public TankBlock block() {
        return switch (this) {
            case BASIC -> Registration.TANK.get();
            case GOLD -> Registration.GOLD_TANK.get();
            case DIAMOND -> Registration.DIAMOND_TANK.get();
            case EMERALD -> Registration.EMERALD_TANK.get();
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
