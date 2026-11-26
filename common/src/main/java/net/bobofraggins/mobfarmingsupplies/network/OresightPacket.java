package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.oresight.OresightClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ByteBufCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.bobofraggins.mobfarmingsupplies.shared.compat.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/** Server to client: the block the player's Oresight shows, or none (see {@code Oresight}). */
public record OresightPacket(Optional<Block> block) implements CustomPacketPayload {

    public static final Type<OresightPacket> TYPE =
            new Type<>(new ResourceLocation("mobfarmingsupplies", "oresight"));

    public static final StreamCodec<FriendlyByteBuf, OresightPacket> STREAM_CODEC = StreamCodec.composite(
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
