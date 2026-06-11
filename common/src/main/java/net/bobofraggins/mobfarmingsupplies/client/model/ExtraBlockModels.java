package net.bobofraggins.mobfarmingsupplies.client.model;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Cross-platform access to "extra" block models — whole block models that are not tied
 * to any blockstate or item, baked alongside the normal model set and looked up by id
 * at render time.
 *
 * <p>NeoForge implementation: {@code ExtraBlockModelsImpl} (standalone model API).
 * Fabric implementation: {@code ExtraBlockModelsImpl} (extra model loading API).
 *
 * <p>Both platform implementations register the model ids referenced by
 * {@code FanBlockEntityRenderer} and {@code MobHarvesterRenderer} eagerly via a static
 * initializer, so no separate "register" call is needed here.
 */
public final class ExtraBlockModels {

    private ExtraBlockModels() {}

    /**
     * Returns the baked model part for {@code modelId}, or {@code null} if it has not
     * been baked yet (e.g. queried before the first resource reload completes).
     */
    @ExpectPlatform
    @Nullable
    public static BlockStateModelPart get(Identifier modelId) {
        throw new AssertionError("Missing platform implementation");
    }
}
