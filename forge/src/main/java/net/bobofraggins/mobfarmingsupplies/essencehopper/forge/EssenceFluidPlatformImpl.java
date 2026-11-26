package net.bobofraggins.mobfarmingsupplies.essencehopper.forge;

import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.forge.register.ForgeOnlyRegistration;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public final class EssenceFluidPlatformImpl {

    private EssenceFluidPlatformImpl() {}

    public static FlowingFluid createSource(Essence essence) {
        return new ForgeFlowingFluid.Source(ForgeOnlyRegistration.ESSENCE_FLUID_PROPS.get(essence));
    }

    public static FlowingFluid createFlowing(Essence essence) {
        return new ForgeFlowingFluid.Flowing(ForgeOnlyRegistration.ESSENCE_FLUID_PROPS.get(essence));
    }
}
