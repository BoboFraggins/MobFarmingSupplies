package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * Block item for the Picnic Basket.
 *
 * <p>Right-click (not sneaking) always opens the item-form storage UI, whether or not a
 * block is targeted — matching the block form's chest-like feel. Sneak + right-click on a
 * block tries to place the block; if placement doesn't actually happen (e.g. non-replaceable
 * or occluded face), it falls back to toggling auto-feed instead of silently failing. Sneak +
 * right-click with no block targeted toggles auto-feed directly.
 */
public class PicnicBasketItem extends BlockItem {

    public PicnicBasketItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            InteractionResult result = super.useOn(ctx);
            if (result.consumesAction()) return result;
            if (!ctx.getLevel().isClientSide() && player instanceof ServerPlayer sp) {
                toggleAutoFeed(sp, ctx.getHand());
            }
            return InteractionResult.SUCCESS;
        }
        if (!ctx.getLevel().isClientSide() && player instanceof ServerPlayer sp) {
            openItemMenu(sp, ctx.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            toggleAutoFeed(sp, hand);
        } else {
            openItemMenu(sp, hand);
        }
        return InteractionResult.SUCCESS;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private static void openItemMenu(ServerPlayer sp, InteractionHand hand) {
        PicnicBasketItemContainer container =
                new PicnicBasketItemContainer(sp, new PicnicBasketLocator.Hand(hand));
        MenuRegistry.openExtendedMenu(sp,
                new SimpleMenuProvider(
                        (syncId, inv, p) -> new PicnicBasketMenu(syncId, inv, container, null),
                        Component.translatable("item.mobfarmingsupplies.picnic_basket")),
                buf -> buf.writeBoolean(false));
    }

    private static void toggleAutoFeed(ServerPlayer sp, InteractionHand hand) {
        ItemStack stack = sp.getItemInHand(hand);
        if (stack.isEmpty()) return;
        PicnicBasketBlockEntity temp = PicnicBasketItemUtils.readBasketData(sp, stack);
        boolean newValue = !temp.isAutoFeed();
        temp.setAutoFeed(newValue);
        PicnicBasketItemUtils.writeBasketData(sp, stack, temp);
        sp.sendOverlayMessage(Component.translatable(newValue
                ? "message.mobfarmingsupplies.picnic_basket.autofeed_on"
                : "message.mobfarmingsupplies.picnic_basket.autofeed_off"));
    }

    // ── Tooltip ─────────────────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("item.mobfarmingsupplies.picnic_basket.tooltip"));
    }
}
