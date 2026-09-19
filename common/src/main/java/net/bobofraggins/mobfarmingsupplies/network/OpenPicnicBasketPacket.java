package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketItemContainer;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketItemUtils;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketLocator;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

/**
 * Client → server: open the item-form Picnic Basket UI for whichever basket the player is
 * currently carrying (main inventory, offhand, or an accessory slot), found via the same
 * canonical scan used by the auto-feed handler. Sent by the "open worn basket" keybind, since
 * worn/equipped items can't be right-clicked. Carries no payload — the server re-resolves the
 * basket itself rather than trusting a client-supplied location.
 */
public record OpenPicnicBasketPacket() implements CustomPacketPayload {

    public static final Type<OpenPicnicBasketPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "open_picnic_basket"));

    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, OpenPicnicBasketPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenPicnicBasketPacket());

    @Override
    public Type<OpenPicnicBasketPacket> type() {
        return TYPE;
    }

    public static void handle(OpenPicnicBasketPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
            PicnicBasketLocator locator = PicnicBasketItemUtils.findBasketLocator(player);
            if (locator == null) {
                player.sendOverlayMessage(Component.translatable("message.mobfarmingsupplies.picnic_basket.not_found"));
                return;
            }
            PicnicBasketItemContainer container = new PicnicBasketItemContainer(player, locator);
            MenuRegistry.openExtendedMenu(player,
                    new SimpleMenuProvider(
                            (syncId, inv, p) -> new PicnicBasketMenu(syncId, inv, container, null),
                            Component.translatable("item.mobfarmingsupplies.picnic_basket")),
                    buf -> buf.writeBoolean(false));
        });
    }
}
