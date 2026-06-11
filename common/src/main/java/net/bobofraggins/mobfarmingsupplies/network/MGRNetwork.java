package net.bobofraggins.mobfarmingsupplies.network;

import dev.architectury.networking.NetworkManager;

/** Registers all server-bound network payloads. Called from {@link net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon#init()}. */
public final class MGRNetwork {

    private MGRNetwork() {}

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
    }
}
