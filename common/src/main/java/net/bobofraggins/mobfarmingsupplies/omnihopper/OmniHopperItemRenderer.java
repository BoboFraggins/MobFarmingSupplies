package net.bobofraggins.mobfarmingsupplies.omnihopper;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Item renderer for the Omnidirectional Hopper: draws the block model for the side configuration
 * saved on the item (its connectors in their side colours), so a hopper picked up from the world
 * looks exactly as it did when placed. An unconfigured item shows the bare body.
 */
public class OmniHopperItemRenderer implements SpecialModelRenderer<BlockState> {

    private final Supplier<? extends Block> block;

    /** @param block the Omnidirectional Hopper or Einstein-Rosen Bridge — both have the same side properties */
    public OmniHopperItemRenderer(Supplier<? extends Block> block) {
        this.block = block;
    }

    @Override
    public BlockState extractArgument(ItemStack stack) {
        BlockState state = block.get().defaultBlockState();
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return state;
        int sides = data.getUnsafe().getInt("Sides"); // read-only access
        for (Direction d : Direction.values()) {
            state = state.setValue(OmniHopperBlock.SIDES.get(d), OmniHopperBlockEntity.unpackSide(sides, d));
        }
        return state;
    }

    @Override
    public void submit(
            @Nullable BlockState state,
            ItemDisplayContext context,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            boolean hasFoil) {
        if (state == null) return;
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, buffers, packedLight, packedOverlay);
    }
}
