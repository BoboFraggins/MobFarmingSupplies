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
 * Client → server: update which output sides are enabled on an Absorption Hopper.
 *
 * <p>The {@code sidesMask} is a 6-bit int where:
 * bit 0 = UP, 1 = DOWN, 2 = NORTH, 3 = SOUTH, 4 = EAST, 5 = WEST.
 */
public record SetPushSidesPacket(BlockPos pos, int sidesMask) implements CustomPacketPayload {

    public static final Type<SetPushSidesPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "set_push_sides"));

    public static final StreamCodec<FriendlyByteBuf, SetPushSidesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetPushSidesPacket::pos,
                    ByteBufCodecs.INT,     SetPushSidesPacket::sidesMask,
                    SetPushSidesPacket::new);

    @Override
    public Type<SetPushSidesPacket> type() {
        return TYPE;
    }

    public static void handle(SetPushSidesPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
            if (player.blockPosition().distSqr(packet.pos()) > 64) return;
            BlockEntity be = player.level().getBlockEntity(packet.pos());
            if (be instanceof IAbsorptionHopperBlockEntity handler) {
                handler.setPushSides(packet.sidesMask());
            }
        });
    }
}
