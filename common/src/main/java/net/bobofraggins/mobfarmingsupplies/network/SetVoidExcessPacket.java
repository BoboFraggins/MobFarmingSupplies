package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.IAbsorptionHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Client → server: turn an Absorption Hopper's Void Excess setting on or off. */
public record SetVoidExcessPacket(BlockPos pos, boolean on) implements CustomPacketPayload {

    public static final Type<SetVoidExcessPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "set_void_excess"));

    public static final StreamCodec<FriendlyByteBuf, SetVoidExcessPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetVoidExcessPacket::pos,
                    ByteBufCodecs.BOOL,    SetVoidExcessPacket::on,
                    SetVoidExcessPacket::new);

    @Override
    public Type<SetVoidExcessPacket> type() {
        return TYPE;
    }

    public static void handle(SetVoidExcessPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
            if (player.blockPosition().distSqr(packet.pos()) > 64) return;
            BlockEntity be = player.level().getBlockEntity(packet.pos());
            if (be instanceof IAbsorptionHopperBlockEntity handler) {
                handler.setVoidExcess(packet.on());
            }
        });
    }
}
