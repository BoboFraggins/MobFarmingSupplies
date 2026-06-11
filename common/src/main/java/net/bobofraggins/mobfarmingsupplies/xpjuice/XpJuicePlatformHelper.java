package net.bobofraggins.mobfarmingsupplies.xpjuice;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Platform bridge for XP Juice fluid instantiation.
 *
 * <p>NeoForge: returns {@code XpJuiceFluid.Source} / {@code XpJuiceFluid.Flowing}
 * (which extend NeoForge's {@code BaseFlowingFluid}).
 * Fabric: returns {@code FabricXpJuiceFluid.Source} / {@code FabricXpJuiceFluid.Flowing}
 * (which extend vanilla's {@code FlowingFluid}).
 */
public final class XpJuicePlatformHelper {

    private XpJuicePlatformHelper() {}

    @ExpectPlatform
    public static FlowingFluid createSourceFluid() {
        throw new AssertionError("Missing platform implementation");
    }

    @ExpectPlatform
    public static FlowingFluid createFlowingFluid() {
        throw new AssertionError("Missing platform implementation");
    }
}
