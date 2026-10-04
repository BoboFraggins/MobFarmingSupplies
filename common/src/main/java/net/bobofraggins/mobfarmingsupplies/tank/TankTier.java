package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.Codec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.util.StringRepresentable;

/**
 * Tank tiers. Each holds four times the one below; a tank is upgraded by surrounding it with the
 * next tier's material (see {@link TankUpgradeRecipe}), keeping its contents.
 */
public enum TankTier implements StringRepresentable {
    BASIC("basic", 64_000L),
    GOLD("gold", 256_000L),
    DIAMOND("diamond", 1_024_000L),
    EMERALD("emerald", 4_096_000L);

    public static final Codec<TankTier> CODEC = StringRepresentable.fromEnum(TankTier::values);

    private final String name;
    private final long capacity;

    TankTier(String name, long capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    /** Capacity in mB. */
    public long capacity() {
        return capacity;
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
