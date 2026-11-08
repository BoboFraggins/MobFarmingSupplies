package net.bobofraggins.mobfarmingsupplies.essencehopper;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.material.FlowingFluid;

/** Makes the essences' fluids, which flow like lava and can't form new sources, for each loader. */
public final class EssenceFluidPlatform {

    private EssenceFluidPlatform() {}

    @ExpectPlatform
    public static FlowingFluid createSource(Essence essence) {
        throw new AssertionError("Missing platform implementation");
    }

    @ExpectPlatform
    public static FlowingFluid createFlowing(Essence essence) {
        throw new AssertionError("Missing platform implementation");
    }
}
