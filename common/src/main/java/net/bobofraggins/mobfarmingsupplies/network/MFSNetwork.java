package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;

/** Registers all server-bound network payloads. Called from {@link net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon#init()}. */
public final class MFSNetwork {

    private MFSNetwork() {}

    public static void register() {
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
    }
}
