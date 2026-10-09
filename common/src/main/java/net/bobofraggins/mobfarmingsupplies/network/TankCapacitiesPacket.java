package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.tank.TankCapacities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server to client: the server's tank sizes, so fill levels and tooltips match (see {@link TankCapacities}). */
public record TankCapacitiesPacket(int baseBuckets, int upgradeMultiplier) implements CustomPacketPayload {

    public static final Type<TankCapacitiesPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("mobfarmingsupplies", "tank_capacities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TankCapacitiesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TankCapacitiesPacket::baseBuckets,
            ByteBufCodecs.VAR_INT, TankCapacitiesPacket::upgradeMultiplier,
            TankCapacitiesPacket::new);

    @Override
    public Type<TankCapacitiesPacket> type() {
        return TYPE;
    }

    public static void handle(TankCapacitiesPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> TankCapacities.set(packet.baseBuckets(), packet.upgradeMultiplier()));
    }
}
