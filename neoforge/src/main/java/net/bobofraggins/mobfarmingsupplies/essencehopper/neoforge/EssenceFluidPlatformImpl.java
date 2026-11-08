package net.bobofraggins.mobfarmingsupplies.essencehopper.neoforge;

import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.neoforge.register.NeoForgeOnlyRegistration;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public final class EssenceFluidPlatformImpl {

    private EssenceFluidPlatformImpl() {}

    public static FlowingFluid createSource(Essence essence) {
        return new BaseFlowingFluid.Source(NeoForgeOnlyRegistration.ESSENCE_FLUID_PROPS.get(essence));
    }

    public static FlowingFluid createFlowing(Essence essence) {
        return new BaseFlowingFluid.Flowing(NeoForgeOnlyRegistration.ESSENCE_FLUID_PROPS.get(essence));
    }
}
