package net.bobofraggins.mobfarmingsupplies.network;

import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.magnet.MagnetPull;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.bobofraggins.mobfarmingsupplies.shared.compat.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client → server: switch the player's Superconducting Magnet on or off, from the toggle key. No
 * payload: the server finds the magnet itself.
 */
public record ToggleMagnetPacket() implements CustomPacketPayload {

    public static final Type<ToggleMagnetPacket> TYPE =
            new Type<>(new ResourceLocation(MobFarmingSuppliesCommon.MODID, "toggle_magnet"));

    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ToggleMagnetPacket> STREAM_CODEC =
            StreamCodec.unit(new ToggleMagnetPacket());

    @Override
    public Type<ToggleMagnetPacket> type() {
        return TYPE;
    }

    public static void handle(ToggleMagnetPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (ctx.getPlayer() instanceof ServerPlayer player) MagnetPull.toggleFound(player);
        });
    }
}
