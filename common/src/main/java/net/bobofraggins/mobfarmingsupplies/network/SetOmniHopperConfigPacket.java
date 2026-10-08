package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client → server: set a Omnidirectional Hopper's side modes and AND/OR mode.
 *
 * @param sides   six {@link net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide} ordinals,
 *                2 bits each, indexed by {@code Direction#get3DDataValue}
 * @param andMode true = AND, false = OR
 */
public record SetOmniHopperConfigPacket(BlockPos pos, int sides, boolean andMode) implements CustomPacketPayload {

    public static final Type<SetOmniHopperConfigPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "set_omnidirectional_hopper_config"));

    public static final StreamCodec<FriendlyByteBuf, SetOmniHopperConfigPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetOmniHopperConfigPacket::pos,
                    ByteBufCodecs.VAR_INT, SetOmniHopperConfigPacket::sides,
                    ByteBufCodecs.BOOL,    SetOmniHopperConfigPacket::andMode,
                    SetOmniHopperConfigPacket::new);

    @Override
    public Type<SetOmniHopperConfigPacket> type() {
        return TYPE;
    }

    public static void handle(SetOmniHopperConfigPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
            if (player.blockPosition().distSqr(packet.pos()) > 64) return;
            if (player.level().getBlockEntity(packet.pos()) instanceof OmniHopperBlockEntity be) {
                be.setConfig(packet.sides() & 0xFFF, packet.andMode());
            }
        });
    }
}
