package net.bobofraggins.mobfarmingsupplies.bridge;

import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.bobofraggins.mobfarmingsupplies.shared.sides.OrientedBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** Einstein-Rosen Bridge block item; its name shows its channel (also what Jade shows for the block). */
public class EinsteinRosenBridgeBlockItem extends OrientedBlockItem {

    public EinsteinRosenBridgeBlockItem(Block block, Properties props) {
        super(block, props);
    }

    /** "Einstein-Rosen Bridge (1A2B3C4D)", or just the name while no channel is assigned. */
    public static Component nameFor(int channel) {
        return channel == 0
                ? Component.translatable("block.mobfarmingsupplies.einstein_rosen_bridge")
                : Component.translatable("block.mobfarmingsupplies.einstein_rosen_bridge.channel",
                        String.format("%08X", channel));
    }

    @Override
    public Component getName(ItemStack stack) {
        return nameFor(stack.getOrDefault(Registration.BRIDGE_CHANNEL.get(), 0));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display,
            Consumer<Component> consumer, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, display, consumer, flag);
        consumer.accept(Component.translatable("item.mobfarmingsupplies.einstein_rosen_bridge.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
