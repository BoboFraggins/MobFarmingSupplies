package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client → server: set a Logistic Sorter's side modes and AND/OR mode.
 *
 * @param sides   six {@link net.bobofraggins.mobfarmingsupplies.logisticsorter.SideMode} ordinals,
 *                2 bits each, indexed by {@code Direction#get3DDataValue}
 * @param andMode true = AND, false = OR
 */
public record SetSorterConfigPacket(BlockPos pos, int sides, boolean andMode) implements CustomPacketPayload {

    public static final Type<SetSorterConfigPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "set_sorter_config"));

    public static final StreamCodec<FriendlyByteBuf, SetSorterConfigPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetSorterConfigPacket::pos,
                    ByteBufCodecs.VAR_INT, SetSorterConfigPacket::sides,
                    ByteBufCodecs.BOOL,    SetSorterConfigPacket::andMode,
                    SetSorterConfigPacket::new);

    @Override
    public Type<SetSorterConfigPacket> type() {
        return TYPE;
    }

    public static void handle(SetSorterConfigPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
            if (player.blockPosition().distSqr(packet.pos()) > 64) return;
            if (player.level().getBlockEntity(packet.pos()) instanceof LogisticSorterBlockEntity be) {
                be.setConfig(packet.sides() & 0xFFF, packet.andMode());
            }
        });
    }
}
