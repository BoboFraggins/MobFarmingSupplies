package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.utils.Env;
import dev.architectury.platform.Platform;
import dev.architectury.networking.NetworkManager;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSPackets;

/** Registers all network payloads. Called from {@link net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon#init()}. */
public final class MFSNetwork {

    private MFSNetwork() {}

    public static void register() {
        // Server to client: the client registers the receiver, a dedicated server just the type.
        if (Platform.getEnvironment() == Env.CLIENT) {
            MFSPackets.registerReceiver(
                    NetworkManager.s2c(),
                    TankCapacitiesPacket.TYPE,
                    TankCapacitiesPacket.STREAM_CODEC,
                    TankCapacitiesPacket::handle);
            MFSPackets.registerReceiver(
                    NetworkManager.s2c(),
                    OresightPacket.TYPE,
                    OresightPacket.STREAM_CODEC,
                    OresightPacket::handle);
        } else {
            MFSPackets.registerType(TankCapacitiesPacket.TYPE, TankCapacitiesPacket.STREAM_CODEC);
            MFSPackets.registerType(OresightPacket.TYPE, OresightPacket.STREAM_CODEC);
        }
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                SetPushSidesPacket.TYPE,
                SetPushSidesPacket.STREAM_CODEC,
                SetPushSidesPacket::handle);
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                SetHopperOffsetPacket.TYPE,
                SetHopperOffsetPacket.STREAM_CODEC,
                SetHopperOffsetPacket::handle);
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                SetVoidExcessPacket.TYPE,
                SetVoidExcessPacket.STREAM_CODEC,
                SetVoidExcessPacket::handle);
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                SetBlockBreakerSilkTouchPacket.TYPE,
                SetBlockBreakerSilkTouchPacket.STREAM_CODEC,
                SetBlockBreakerSilkTouchPacket::handle);
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                SetInhibitorOffsetPacket.TYPE,
                SetInhibitorOffsetPacket.STREAM_CODEC,
                SetInhibitorOffsetPacket::handle);
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                OpenPicnicBasketPacket.TYPE,
                OpenPicnicBasketPacket.STREAM_CODEC,
                OpenPicnicBasketPacket::handle);
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                ToggleMagnetPacket.TYPE,
                ToggleMagnetPacket.STREAM_CODEC,
                ToggleMagnetPacket::handle);
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                SetScribingStatePacket.TYPE,
                SetScribingStatePacket.STREAM_CODEC,
                SetScribingStatePacket::handle);
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                SetSorterConfigPacket.TYPE,
                SetSorterConfigPacket.STREAM_CODEC,
                SetSorterConfigPacket::handle);
        MFSPackets.registerReceiver(
                NetworkManager.c2s(),
                SetOmniHopperConfigPacket.TYPE,
                SetOmniHopperConfigPacket.STREAM_CODEC,
                SetOmniHopperConfigPacket::handle);
    }
}
