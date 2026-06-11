package net.bobofraggins.mobfarmingsupplies.xpjuice.neoforge;

import net.bobofraggins.mobfarmingsupplies.xpjuice.XpJuiceFluid;
import net.minecraft.world.level.material.FlowingFluid;

/** NeoForge implementation of {@link net.bobofraggins.mobfarmingsupplies.xpjuice.XpJuicePlatformHelper}. */
public final class XpJuicePlatformHelperImpl {

    private XpJuicePlatformHelperImpl() {}

    public static FlowingFluid createSourceFluid() {
        return new XpJuiceFluid.Source();
    }

    public static FlowingFluid createFlowingFluid() {
        return new XpJuiceFluid.Flowing();
    }
}
