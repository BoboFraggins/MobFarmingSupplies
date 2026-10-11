package net.bobofraggins.mobfarmingsupplies.stirlingengine;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Stirling Engine tiers. Each doubles the one below's output, buffer and push rate, and gives the
 * flywheel its material's colour. An engine is upgraded by surrounding it with the next tier's
 * material, as a Tank is.
 */
public enum StirlingEngineTier implements StringRepresentable {
    BASIC("basic", 1, 0xC8D2DE),
    IRON("iron", 2, 0xD8D8D8),
    GOLD("gold", 4, 0xFAD64A),
    DIAMOND("diamond", 8, 0x5DECF5),
    EMERALD("emerald", 16, 0x17DD62);

    public static final Codec<StirlingEngineTier> CODEC = StringRepresentable.fromEnum(StirlingEngineTier::values);

    private final String name;
    private final int multiplier;
    private final int flywheelColour;

    StirlingEngineTier(String name, int multiplier, int flywheelColour) {
        this.name = name;
        this.multiplier = multiplier;
        this.flywheelColour = flywheelColour;
    }

    public int multiplier() {
        return multiplier;
    }

    /** RGB, for the flywheel's tinted faces. */
    public int flywheelColour() {
        return flywheelColour;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
