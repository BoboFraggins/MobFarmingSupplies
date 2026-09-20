package net.bobofraggins.mobfarmingsupplies.glamping.present;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/**
 * The Present item. The wrap interaction itself lives in {@link PresentWrapEvents}, not an
 * override here — see that class's javadoc for why. Vanilla's own generic {@link BlockItem}
 * placement handling already restores wrapped state from the {@code minecraft:block_entity_data}
 * component onto the placed {@link PresentBlockEntity}, so this class needs no {@code placeBlock}
 * override either.
 */
public class PresentBlockItem extends BlockItem {

    public PresentBlockItem(Block block, Item.Properties props) {
        super(block, props);
    }

    // -------------------------------------------------------------------------
    // Display — item name includes wrapped block type
    // -------------------------------------------------------------------------

    @Override
    public Component getName(ItemStack stack) {
        Component base = super.getName(stack);
        Optional<String> blockId = wrappedBlockId(stack);
        if (blockId.isEmpty()) return base;
        return blockId.flatMap(id -> Optional.ofNullable(Identifier.tryParse(id)))
                .map(rl -> BuiltInRegistries.BLOCK.getValue(rl))
                .map(block -> {
                    Item item = block.asItem();
                    Component wrappedName =
                            item == Items.AIR ? block.getName() : item.getName(new ItemStack(item));
                    return (Component) Component.empty()
                            .append(base)
                            .append(" (")
                            .append(wrappedName)
                            .append(")");
                })
                .orElse(base);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay lines,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, tooltipAdder, flag);
        if (hasWrappedBlock(stack)) {
            tooltipAdder.accept(Component.translatable("item.mobfarmingsupplies.present.tooltip_unwrap"));
        } else {
            tooltipAdder.accept(Component.translatable("item.mobfarmingsupplies.present.tooltip_wrap"));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public static boolean hasWrappedBlock(ItemStack stack) {
        return wrappedBlockId(stack).isPresent();
    }

    private static Optional<String> wrappedBlockId(ItemStack stack) {
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return Optional.empty();
        CompoundTag wrapped = data.copyTagWithoutId().getCompoundOrEmpty(PresentBlockEntity.TAG_WRAPPED_STATE);
        return wrapped.getString("Name");
    }
}
