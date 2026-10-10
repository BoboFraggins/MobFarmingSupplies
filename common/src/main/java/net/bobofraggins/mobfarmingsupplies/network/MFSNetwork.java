package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.utils.Env;
import dev.architectury.platform.Platform;
import dev.architectury.networking.NetworkManager;

/** Registers all network payloads. Called from {@link net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon#init()}. */
public final class MFSNetwork {

    private MFSNetwork() {}

    public static void register() {
        // Server to client: the client registers the receiver, a dedicated server just the type.
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(
                    NetworkManager.s2c(),
                    TankCapacitiesPacket.TYPE,
                    TankCapacitiesPacket.STREAM_CODEC,
                    TankCapacitiesPacket::handle);
        } else {
            NetworkManager.registerS2CPayloadType(TankCapacitiesPacket.TYPE, TankCapacitiesPacket.STREAM_CODEC);
        }
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                SetPushSidesPacket.TYPE,
                SetPushSidesPacket.STREAM_CODEC,
                SetPushSidesPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                SetHopperOffsetPacket.TYPE,
                SetHopperOffsetPacket.STREAM_CODEC,
                SetHopperOffsetPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                SetVoidExcessPacket.TYPE,
                SetVoidExcessPacket.STREAM_CODEC,
                SetVoidExcessPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                SetInhibitorOffsetPacket.TYPE,
                SetInhibitorOffsetPacket.STREAM_CODEC,
                SetInhibitorOffsetPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                OpenPicnicBasketPacket.TYPE,
                OpenPicnicBasketPacket.STREAM_CODEC,
                OpenPicnicBasketPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                SetScribingStatePacket.TYPE,
                SetScribingStatePacket.STREAM_CODEC,
                SetScribingStatePacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                SetSorterConfigPacket.TYPE,
                SetSorterConfigPacket.STREAM_CODEC,
                SetSorterConfigPacket::handle);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                SetOmniHopperConfigPacket.TYPE,
                SetOmniHopperConfigPacket.STREAM_CODEC,
                SetOmniHopperConfigPacket::handle);
    }
}
