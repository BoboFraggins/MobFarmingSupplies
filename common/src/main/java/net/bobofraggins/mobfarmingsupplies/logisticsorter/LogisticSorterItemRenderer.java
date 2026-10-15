package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.render.SpecialModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Item renderer for the Logistic Sorter: draws the block model for the configuration saved on
 * the item (its connectors in their side colours, and the active/inactive body), so a sorter
 * picked up from the world looks exactly as it did when placed. An unconfigured item shows the
 * bare inactive body.
 */
public class LogisticSorterItemRenderer implements SpecialModelRenderer<BlockState> {

    @Override
    public BlockState extractArgument(ItemStack stack) {
        BlockState state = Registration.LOGISTIC_SORTER.get().defaultBlockState();
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return state;
        CompoundTag tag = data.getUnsafe(); // read-only access
        int sides = tag.getInt("Sides");
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
        for (Tag t : tag.getList("Filters", Tag.TAG_COMPOUND)) {
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
