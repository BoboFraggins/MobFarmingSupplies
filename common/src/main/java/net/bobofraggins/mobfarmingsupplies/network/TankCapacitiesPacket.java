package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.tank.TankCapacities;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ByteBufCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.bobofraggins.mobfarmingsupplies.shared.compat.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server to client: the server's tank sizes, so fill levels and tooltips match (see {@link TankCapacities}). */
public record TankCapacitiesPacket(int baseBuckets, int upgradeMultiplier) implements CustomPacketPayload {

    public static final Type<TankCapacitiesPacket> TYPE =
            new Type<>(new ResourceLocation("mobfarmingsupplies", "tank_capacities"));

    public static final StreamCodec<FriendlyByteBuf, TankCapacitiesPacket> STREAM_CODEC = StreamCodec.composite(
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
