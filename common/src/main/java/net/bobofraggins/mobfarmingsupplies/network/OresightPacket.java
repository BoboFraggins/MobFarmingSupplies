package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.oresight.OresightClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/** Server to client: the block the player's Oresight shows, or none (see {@code Oresight}). */
public record OresightPacket(Optional<Block> block) implements CustomPacketPayload {

    public static final Type<OresightPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("mobfarmingsupplies", "oresight"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OresightPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.BLOCK)), OresightPacket::block,
            OresightPacket::new);

    @Override
    public Type<OresightPacket> type() {
        return TYPE;
    }

    public static void handle(OresightPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> OresightClient.setTarget(packet.block().orElse(null)));
    }
}
