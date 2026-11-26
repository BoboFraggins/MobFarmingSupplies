package net.bobofraggins.mobfarmingsupplies.gametest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.gametest.framework.TestFunction;

/**
 * Every game test of the mod. Each loader hands this list to its game test framework: Forge from
 * {@code forge.gametest.ForgeGameTests}, Fabric from its {@code fabric-gametest} entrypoint.
 * Run with {@code ./gradlew :forge:runGameTestServer} (or {@code :fabric:}); the server runs them
 * all, logs each failure and stops.
 */
public final class MFSGameTests {

    private MFSGameTests() {}

    public static Collection<TestFunction> all() {
        List<TestFunction> tests = new ArrayList<>();
        tests.addAll(DataLoadTests.all());
        tests.addAll(BlockTests.all());
        tests.addAll(MenuTests.all());
        tests.addAll(RecipeTests.all());
        tests.addAll(FeatureTests.all());
        return tests;
    }
}
