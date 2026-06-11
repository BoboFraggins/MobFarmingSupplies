package net.bobofraggins.mobfarmingsupplies.client.model.fabric;

import java.util.LinkedHashMap;
import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterRenderer;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric implementation of {@code ExtraBlockModels}, backed by the
 * {@code net.fabricmc.fabric.api.client.model.loading.v1} extra-model loading API.
 */
public final class ExtraBlockModelsImpl {

    private static final Map<Identifier, ExtraModelKey<BlockStateModelPart>> KEYS = new LinkedHashMap<>();

    static {
        register(FanBlockEntityRenderer.TURBINE_MODEL_ID);
        register(MobHarvesterRenderer.LEFT_ARM_FRONT_MODEL_ID);
        register(MobHarvesterRenderer.LEFT_ARM_SIDE_MODEL_ID);
        register(MobHarvesterRenderer.LEFT_ARM_REAR_MODEL_ID);
        register(MobHarvesterRenderer.RIGHT_ARM_FRONT_MODEL_ID);
        register(MobHarvesterRenderer.RIGHT_ARM_SIDE_MODEL_ID);
        register(MobHarvesterRenderer.RIGHT_ARM_REAR_MODEL_ID);
        register(MobHarvesterRenderer.HEAD_MODEL_ID);
    }

    private ExtraBlockModelsImpl() {}

    private static void register(Identifier modelId) {
        KEYS.put(modelId, ExtraModelKey.create(modelId::toString));
    }

    @Nullable
    public static BlockStateModelPart get(Identifier modelId) {
        ExtraModelKey<BlockStateModelPart> key = KEYS.get(modelId);
        if (key == null) return null;
        return ((FabricModelManager) Minecraft.getInstance().getModelManager()).getModel(key);
    }

    /** Registers a model-loading plugin that bakes all models declared above. */
    public static void registerModelLoadingPlugin() {
        ModelLoadingPlugin.register(context -> {
            for (Map.Entry<Identifier, ExtraModelKey<BlockStateModelPart>> entry : KEYS.entrySet()) {
                context.addModel(entry.getValue(), new SimpleBlockModelPart(entry.getKey()));
            }
        });
    }

    /**
     * Bakes a whole-block model as a {@link BlockStateModelPart}, mirroring NeoForge's
     * {@code SimpleUnbakedStandaloneModel.simpleModelWrapper}.
     */
    private record SimpleBlockModelPart(Identifier modelId) implements UnbakedExtraModel<BlockStateModelPart> {
        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(modelId);
        }

        @Override
        public BlockStateModelPart bake(ModelBaker baker) {
            return SimpleModelWrapper.bake(baker, modelId, BlockModelRotation.IDENTITY);
        }
    }
}
