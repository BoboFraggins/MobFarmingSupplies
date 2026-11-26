package net.bobofraggins.mobfarmingsupplies.network;

import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSStreamCodecs;
import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.blockbreaker.BlockBreakerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ByteBufCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.bobofraggins.mobfarmingsupplies.shared.compat.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Client → server: turn a Block Breaker's Silk Touch on or off. */
public record SetBlockBreakerSilkTouchPacket(BlockPos pos, boolean on) implements CustomPacketPayload {

    public static final Type<SetBlockBreakerSilkTouchPacket> TYPE =
            new Type<>(new ResourceLocation(MobFarmingSuppliesCommon.MODID, "set_block_breaker_silk_touch"));

    public static final StreamCodec<FriendlyByteBuf, SetBlockBreakerSilkTouchPacket> STREAM_CODEC =
            StreamCodec.composite(
                    MFSStreamCodecs.BLOCK_POS, SetBlockBreakerSilkTouchPacket::pos,
                    ByteBufCodecs.BOOL,    SetBlockBreakerSilkTouchPacket::on,
                    SetBlockBreakerSilkTouchPacket::new);

    @Override
    public Type<SetBlockBreakerSilkTouchPacket> type() {
        return TYPE;
    }

    public static void handle(SetBlockBreakerSilkTouchPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;
            if (player.blockPosition().distSqr(packet.pos()) > 64) return;
            if (player.level().getBlockEntity(packet.pos()) instanceof BlockBreakerBlockEntity breaker) {
                breaker.setSilkTouch(packet.on());
            }
        });
    }
}
