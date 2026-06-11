package net.bobofraggins.mobfarmingsupplies.client.model.neoforge;

import java.util.LinkedHashMap;
import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntityRenderer;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge implementation of {@code ExtraBlockModels}, backed by the
 * {@code net.neoforged.neoforge.client.model.standalone} standalone-model API.
 */
public final class ExtraBlockModelsImpl {

    private static final Map<Identifier, StandaloneModelKey<BlockStateModelPart>> KEYS = new LinkedHashMap<>();

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
        KEYS.put(modelId, new StandaloneModelKey<>(modelId::toString));
    }

    @Nullable
    public static BlockStateModelPart get(Identifier modelId) {
        StandaloneModelKey<BlockStateModelPart> key = KEYS.get(modelId);
        return key == null ? null : Minecraft.getInstance().getModelManager().getStandaloneModel(key);
    }

    /** Registers the unbaked models declared above with the standalone model loader. */
    public static void onRegisterStandalone(ModelEvent.RegisterStandalone event) {
        for (Map.Entry<Identifier, StandaloneModelKey<BlockStateModelPart>> entry : KEYS.entrySet()) {
            event.register(entry.getValue(), SimpleUnbakedStandaloneModel.simpleModelWrapper(entry.getKey()));
        }
    }
}
