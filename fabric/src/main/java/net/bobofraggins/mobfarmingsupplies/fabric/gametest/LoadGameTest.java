package net.bobofraggins.mobfarmingsupplies.fabric.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Development only: the game test server ({@code ./gradlew :fabric:runGameTestServer}) needs at
 * least one test, and getting this far means the mod and its data loaded.
 */
public class LoadGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void loads(GameTestHelper helper) {
        helper.succeed();
    }
}
