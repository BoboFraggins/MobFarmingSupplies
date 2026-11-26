package net.bobofraggins.mobfarmingsupplies.forge.xpjuice;

import net.bobofraggins.mobfarmingsupplies.forge.register.ForgeOnlyRegistration;
import net.minecraftforge.fluids.ForgeFlowingFluid;

/** Source and Flowing variants of the XP Juice fluid. */
public final class XpJuiceFluid {

    private XpJuiceFluid() {}

    public static class Source extends ForgeFlowingFluid.Source {
        public Source() {
            super(ForgeOnlyRegistration.XP_JUICE_FLUID_PROPS);
        }
    }

    public static class Flowing extends ForgeFlowingFluid.Flowing {
        public Flowing() {
            super(ForgeOnlyRegistration.XP_JUICE_FLUID_PROPS);
        }
    }
}
