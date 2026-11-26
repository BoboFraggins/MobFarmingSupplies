package net.bobofraggins.mobfarmingsupplies.forge.gametest;

import java.util.Collection;
import net.bobofraggins.mobfarmingsupplies.gametest.MFSGameTests;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;

/** Hands the mod's game tests ({@link MFSGameTests}) to Forge. Development only: not in the release jar. */
@GameTestHolder("mobfarmingsupplies")
public final class ForgeGameTests {

    @GameTestGenerator
    public static Collection<TestFunction> all() {
        return MFSGameTests.all();
    }
}
