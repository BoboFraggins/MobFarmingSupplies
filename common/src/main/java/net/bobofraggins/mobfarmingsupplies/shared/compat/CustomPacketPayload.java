package net.bobofraggins.mobfarmingsupplies.shared.compat;

import net.minecraft.resources.ResourceLocation;

/**
 * Stand-in for 1.20.5+'s payload interface: a packet with a typed id. Sent and received through
 * {@link MFSPackets}, which turns it into an Architectury 9 buffer packet.
 */
public interface CustomPacketPayload {

    Type<? extends CustomPacketPayload> type();

    record Type<T extends CustomPacketPayload>(ResourceLocation id) {}
}
