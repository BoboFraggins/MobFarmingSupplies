package net.bobofraggins.mobfarmingsupplies.shared.compat;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Typed payload networking over Architectury 9's buffer packets (1.20.1 has no payload types):
 * the receiver decodes with the payload's {@link StreamCodec}, senders encode with the codec
 * registered for the payload's type.
 */
public final class MFSPackets {

    private static final Map<CustomPacketPayload.Type<?>, StreamCodec<FriendlyByteBuf, ?>> CODECS = new HashMap<>();

    private MFSPackets() {}

    public static <T extends CustomPacketPayload> void registerReceiver(NetworkManager.Side side,
            CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec,
            BiConsumer<T, NetworkManager.PacketContext> handler) {
        registerType(type, codec);
        NetworkManager.registerReceiver(side, type.id(), (buf, ctx) -> handler.accept(codec.decode(buf), ctx));
    }

    /** Records how to encode a payload this side only sends. */
    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void registerType(CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec) {
        CODECS.put(type, (StreamCodec<FriendlyByteBuf, ?>) (StreamCodec<?, ?>) codec);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        NetworkManager.sendToServer(payload.type().id(), encode(payload));
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        NetworkManager.sendToPlayer(player, payload.type().id(), encode(payload));
    }

    public static void sendToPlayers(Iterable<ServerPlayer> players, CustomPacketPayload payload) {
        for (ServerPlayer player : players) sendToPlayer(player, payload);
    }

    @SuppressWarnings("unchecked")
    private static FriendlyByteBuf encode(CustomPacketPayload payload) {
        StreamCodec<FriendlyByteBuf, CustomPacketPayload> codec =
                (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) CODECS.get(payload.type());
        if (codec == null) throw new IllegalStateException("Unregistered packet " + payload.type().id());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        codec.encode(buf, payload);
        return buf;
    }
}
