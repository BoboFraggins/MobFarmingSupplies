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

/**
 * Client → server: nudge the Absorption Hopper's pickup-area offset by ±1 on one axis.
 *
 * @param pos   block position of the hopper
 * @param axis  0 = X, 1 = Y, 2 = Z
 * @param delta +1 or -1
 */
public record SetHopperOffsetPacket(BlockPos pos, int axis, int delta) implements CustomPacketPayload {

    public static final Type<SetHopperOffsetPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "set_hopper_offset"));

    public static final StreamCodec<FriendlyByteBuf, SetHopperOffsetPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetHopperOffsetPacket::pos,
                    ByteBufCodecs.INT,     SetHopperOffsetPacket::axis,
                    ByteBufCodecs.INT,     SetHopperOffsetPacket::delta,
                    SetHopperOffsetPacket::new);

    @Override
    public Type<SetHopperOffsetPacket> type() {
        return TYPE;
    }

    public static void handle(SetHopperOffsetPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
            if (player.blockPosition().distSqr(packet.pos()) > 64) return;
            BlockEntity be = player.level().getBlockEntity(packet.pos());
            if (be instanceof IAbsorptionHopperBlockEntity handler) {
                handler.adjustOffset(packet.axis(), packet.delta());
            }
        });
    }
}
