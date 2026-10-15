package net.bobofraggins.mobfarmingsupplies.client.model.fabric;

import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric implementation of {@code ExtraBlockModels}, backed by the
 * {@code net.fabricmc.fabric.api.client.model.loading.v1} extra-model loading API.
 */
public final class ExtraBlockModelsImpl {

    private ExtraBlockModelsImpl() {}

    @Nullable
    public static BakedModel get(ResourceLocation modelId) {
        return Minecraft.getInstance().getModelManager().getModel(modelId);
    }

    /** Registers a model-loading plugin that bakes {@link ExtraBlockModels#IDS}. */
    public static void registerModelLoadingPlugin() {
        ModelLoadingPlugin.register(context -> context.addModels(ExtraBlockModels.IDS));
    }
}
