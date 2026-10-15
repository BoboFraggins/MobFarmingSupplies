package net.bobofraggins.mobfarmingsupplies.client.model.neoforge;

import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.Nullable;

/** NeoForge implementation of {@code ExtraBlockModels}, backed by standalone (additional) models. */
public final class ExtraBlockModelsImpl {

    private ExtraBlockModelsImpl() {}

    @Nullable
    public static BakedModel get(ResourceLocation modelId) {
        ModelManager models = Minecraft.getInstance().getModelManager();
        BakedModel model = models.getModel(ModelResourceLocation.standalone(modelId));
        return model == models.getMissingModel() ? null : model;
    }

    /** Registers {@link ExtraBlockModels#IDS} to be loaded and baked as standalone models. */
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation id : ExtraBlockModels.IDS) {
            event.register(ModelResourceLocation.standalone(id));
        }
    }
}
