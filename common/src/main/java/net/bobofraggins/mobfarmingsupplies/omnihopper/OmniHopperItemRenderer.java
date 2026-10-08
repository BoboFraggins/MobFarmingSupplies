package net.bobofraggins.mobfarmingsupplies.omnihopper;

import java.util.LinkedHashMap;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Item renderer for the Omnidirectional Hopper: draws the block model for the side configuration
 * saved on the item (its connectors in their side colours), so a hopper picked up from the world
 * looks exactly as it did when placed. An unconfigured item shows the bare body.
 */
public class OmniHopperItemRenderer implements SpecialModelRenderer<BlockState> {

    /** Model parts per block state — block models never change after bake. */
    /** Quads per block state, grouped by render type (the bridge's portal is translucent) — models never change after bake. */
    private final Map<BlockState, Map<RenderType, List<BakedQuad>>> quadCache = new HashMap<>();
    private final Supplier<? extends Block> block;

    /** @param block the Omnidirectional Hopper or Einstein-Rosen Bridge — both have the same side properties */
    public OmniHopperItemRenderer(Supplier<? extends Block> block) {
        this.block = block;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    @Override
    public BlockState extractArgument(ItemStack stack) {
        BlockState state = block.get().defaultBlockState();
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return state;
        int sides = data.copyTagWithoutId().getIntOr("Sides", 0);
        for (Direction d : Direction.values()) {
            state = state.setValue(OmniHopperBlock.SIDES.get(d), OmniHopperBlockEntity.unpackSide(sides, d));
        }
        return state;
    }

    @Override
    public void submit(
            @Nullable BlockState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            int packedOverlay,
            boolean hasFoil,
            int tint) {
        if (state == null) return;
        Map<RenderType, List<BakedQuad>> quads = quadCache.computeIfAbsent(state, st -> {
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(st);
            List<BlockStateModelPart> parts = new ArrayList<>();
            model.collectParts(RandomSource.create(), parts);
            Map<RenderType, List<BakedQuad>> byType = new LinkedHashMap<>();
            for (BlockStateModelPart part : parts) {
                for (Direction dir : Direction.values()) {
                    for (BakedQuad quad : part.getQuads(dir)) {
                        byType.computeIfAbsent(quad.materialInfo().itemRenderType(), t -> new ArrayList<>()).add(quad);
                    }
                }
                for (BakedQuad quad : part.getQuads(null)) {
                    byType.computeIfAbsent(quad.materialInfo().itemRenderType(), t -> new ArrayList<>()).add(quad);
                }
            }
            return byType;
        });
        quads.forEach((type, list) -> collector.submitCustomGeometry(poseStack, type, (pose, vc) -> {
            QuadInstance qi = new QuadInstance();
            qi.setColor(0xFFFFFFFF);
            qi.setLightCoords(packedLight);
            qi.setOverlayCoords(packedOverlay);
            for (BakedQuad quad : list) vc.putBakedQuad(pose, quad, qi);
        }));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<BlockState> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        @Nullable
        public SpecialModelRenderer<BlockState> bake(SpecialModelRenderer.BakingContext context) {
            return new OmniHopperItemRenderer(Registration.OMNI_HOPPER);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<BlockState>> type() {
            return MAP_CODEC;
        }
    }

    public record BridgeUnbaked() implements SpecialModelRenderer.Unbaked<BlockState> {
        public static final MapCodec<BridgeUnbaked> MAP_CODEC = MapCodec.unit(new BridgeUnbaked());

        @Override
        @Nullable
        public SpecialModelRenderer<BlockState> bake(SpecialModelRenderer.BakingContext context) {
            return new OmniHopperItemRenderer(Registration.EINSTEIN_ROSEN_BRIDGE);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<BlockState>> type() {
            return MAP_CODEC;
        }
    }
}
