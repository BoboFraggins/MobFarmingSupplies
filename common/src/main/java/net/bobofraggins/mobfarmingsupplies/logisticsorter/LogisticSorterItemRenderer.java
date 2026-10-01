package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
 * Item renderer for the Logistic Sorter: draws the block model for the configuration saved on
 * the item (its connectors in their side colours, and the active/inactive body), so a sorter
 * picked up from the world looks exactly as it did when placed. An unconfigured item shows the
 * bare inactive body.
 */
public class LogisticSorterItemRenderer implements SpecialModelRenderer<BlockState> {

    /** Model parts per block state — block models never change after bake. */
    private final Map<BlockState, List<BlockStateModelPart>> partsCache = new HashMap<>();

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    @Override
    public BlockState extractArgument(ItemStack stack) {
        BlockState state = Registration.LOGISTIC_SORTER.get().defaultBlockState();
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return state;
        CompoundTag tag = data.copyTagWithoutId();
        int sides = tag.getIntOr("Sides", 0);
        boolean in = false, out = false;
        for (Direction d : Direction.values()) {
            SideMode mode = LogisticSorterBlockEntity.unpackSide(sides, d);
            state = state.setValue(LogisticSorterBlock.SIDES.get(d), mode);
            in |= mode == SideMode.INPUT;
            out |= mode.isOutput();
        }
        // Same "configured and ready" rule as LogisticSorterBlockEntity#isActive. The filter
        // slots only accept Item Filters, so any non-empty saved slot counts as a filter.
        boolean anyFilter = false;
        for (Tag t : tag.getListOrEmpty("Filters")) {
            if (t instanceof CompoundTag c && !c.isEmpty()) {
                anyFilter = true;
                break;
            }
        }
        return state.setValue(LogisticSorterBlock.ACTIVE, in && out && anyFilter);
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
        List<BlockStateModelPart> parts = partsCache.computeIfAbsent(state, s -> {
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(s);
            List<BlockStateModelPart> out = new ArrayList<>();
            model.collectParts(RandomSource.create(), out);
            return out;
        });
        collector.submitCustomGeometry(poseStack, Sheets.cutoutBlockItemSheet(), (pose, vc) -> {
            QuadInstance qi = new QuadInstance();
            qi.setColor(0xFFFFFFFF);
            qi.setLightCoords(packedLight);
            qi.setOverlayCoords(packedOverlay);
            for (BlockStateModelPart part : parts) {
                for (Direction dir : Direction.values()) {
                    for (var quad : part.getQuads(dir)) vc.putBakedQuad(pose, quad, qi);
                }
                for (var quad : part.getQuads(null)) vc.putBakedQuad(pose, quad, qi);
            }
        });
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<BlockState> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        @Nullable
        public SpecialModelRenderer<BlockState> bake(SpecialModelRenderer.BakingContext context) {
            return new LogisticSorterItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<BlockState>> type() {
            return MAP_CODEC;
        }
    }
}
