package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Client → server: the Filter Scribing Terminal's control state changed.
 *
 * @param containerId the open terminal menu's id (ignored if the player has since closed it)
 * @param matcher     the item shown in the matcher slot (empty to clear)
 * @param negate      the Is / Is Not toggle
 * @param selected    index of the selected matcher row, or -1
 */
public record SetScribingStatePacket(int containerId, ItemStack matcher, boolean negate, int selected)
        implements CustomPacketPayload {

    public static final Type<SetScribingStatePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "set_scribing_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetScribingStatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,           SetScribingStatePacket::containerId,
                    ItemStack.OPTIONAL_STREAM_CODEC, SetScribingStatePacket::matcher,
                    ByteBufCodecs.BOOL,              SetScribingStatePacket::negate,
                    ByteBufCodecs.VAR_INT,           SetScribingStatePacket::selected,
                    SetScribingStatePacket::new);

    @Override
    public Type<SetScribingStatePacket> type() {
        return TYPE;
    }

    public static void handle(SetScribingStatePacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
            if (player.containerMenu instanceof FilterScribingTerminalMenu menu
                    && menu.containerId == packet.containerId()) {
                menu.setScribingState(packet.matcher(), packet.negate(), packet.selected());
            }
        });
    }
}
