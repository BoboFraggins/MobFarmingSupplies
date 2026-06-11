package net.bobofraggins.mobfarmingsupplies.xpjuice;

import net.bobofraggins.mobfarmingsupplies.register.NeoForgeOnlyRegistration;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** Source and Flowing variants of the XP Juice fluid. */
public final class XpJuiceFluid {

    private XpJuiceFluid() {}

    public static class Source extends BaseFlowingFluid.Source {
        public Source() {
            super(NeoForgeOnlyRegistration.XP_JUICE_FLUID_PROPS);
        }
    }

    public static class Flowing extends BaseFlowingFluid.Flowing {
        public Flowing() {
            super(NeoForgeOnlyRegistration.XP_JUICE_FLUID_PROPS);
        }
    }
}
