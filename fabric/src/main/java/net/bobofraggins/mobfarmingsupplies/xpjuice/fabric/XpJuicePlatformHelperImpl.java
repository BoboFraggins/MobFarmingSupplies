package net.bobofraggins.mobfarmingsupplies.xpjuice.fabric;

import net.bobofraggins.mobfarmingsupplies.xpjuice.FabricXpJuiceFluid;
import net.minecraft.world.level.material.FlowingFluid;

/** Fabric implementation of {@link net.bobofraggins.mobfarmingsupplies.xpjuice.XpJuicePlatformHelper}. */
public final class XpJuicePlatformHelperImpl {

    private XpJuicePlatformHelperImpl() {}

    public static FlowingFluid createSourceFluid() {
        return new FabricXpJuiceFluid.Source();
    }

    public static FlowingFluid createFlowingFluid() {
        return new FabricXpJuiceFluid.Flowing();
    }
}
