package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Client → server: nudge the Ender Inhibitor's suppression-area offset by ±1 on one axis.
 *
 * @param pos   block position of the inhibitor
 * @param axis  0 = X, 1 = Y, 2 = Z
 * @param delta +1 or -1
 */
public record SetInhibitorOffsetPacket(BlockPos pos, int axis, int delta) implements CustomPacketPayload {

    public static final Type<SetInhibitorOffsetPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "set_inhibitor_offset"));

    public static final StreamCodec<FriendlyByteBuf, SetInhibitorOffsetPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetInhibitorOffsetPacket::pos,
                    ByteBufCodecs.INT,     SetInhibitorOffsetPacket::axis,
                    ByteBufCodecs.INT,     SetInhibitorOffsetPacket::delta,
                    SetInhibitorOffsetPacket::new);

    @Override
    public Type<SetInhibitorOffsetPacket> type() {
        return TYPE;
    }

    public static void handle(SetInhibitorOffsetPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
            if (player.blockPosition().distSqr(packet.pos()) > 64) return;
            BlockEntity be = player.level().getBlockEntity(packet.pos());
            if (be instanceof EnderInhibitorBlockEntity handler) {
                handler.adjustOffset(packet.axis(), packet.delta());
            }
        });
    }
}
