package net.bobofraggins.mobfarmingsupplies.circuitinscriber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

/**
 * The Circuit Inscriber's item: its base with the arms half curled, as a placed one rests between
 * crafts. The arms are drawn by {@link CircuitInscriberRenderer}'s own joints, so the two match.
 */
public class CircuitInscriberItemRenderer implements SpecialModelRenderer<Void> {

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    @Override
    @Nullable
    public Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void submit(@Nullable Void argument, PoseStack poseStack, SubmitNodeCollector collector, int light,
                       int overlay, boolean hasFoil, int tint) {
        BlockStateModelPart base = ExtraBlockModels.get(CircuitInscriberRenderer.BASE_MODEL_ID);
        if (base != null) {
            collector.submitBlockModel(poseStack, Sheets.cutoutBlockItemSheet(), List.of(base), new int[0],
                    light, overlay, 0);
        }
        poseStack.pushPose();
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);
        CircuitInscriberRenderer.submitArms(CircuitInscriberRenderer.REST_CURL, Sheets.cutoutBlockItemSheet(),
                light, overlay, poseStack, collector);
        poseStack.popPose();
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new CircuitInscriberItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Void>> type() {
            return MAP_CODEC;
        }
    }
}
