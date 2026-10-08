package net.bobofraggins.mobfarmingsupplies.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperOutput;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.minecraft.core.Direction;

/**
 * The loaded Einstein-Rosen Bridges on each channel, across every dimension. Bridges join when
 * they tick (so a bridge loaded from disk, or given a channel on placement, joins at once) and
 * leave when their block entity is removed or unloaded. Server thread only; cleared when the
 * server stops.
 */
public final class BridgeNetworks {

    private static final class Network {
        final Set<EinsteinRosenBridgeBlockEntity> bridges = new LinkedHashSet<>();
        boolean routing;
        int rotation;
    }

    private static final Map<Integer, Network> NETWORKS = new HashMap<>();

    private BridgeNetworks() {}

    static void join(EinsteinRosenBridgeBlockEntity bridge, int channel) {
        NETWORKS.computeIfAbsent(channel, c -> new Network()).bridges.add(bridge);
    }

    static void leave(EinsteinRosenBridgeBlockEntity bridge, int channel) {
        Network network = NETWORKS.get(channel);
        if (network == null) return;
        network.bridges.remove(bridge);
        if (network.bridges.isEmpty() && !network.routing) NETWORKS.remove(channel);
    }

    public static void clear() {
        NETWORKS.clear();
    }

    static List<EinsteinRosenBridgeBlockEntity> members(int channel) {
        Network network = NETWORKS.get(channel);
        return network == null ? List.of() : List.copyOf(network.bridges);
    }

    /** Has an INPUT side and an OUTPUT side somewhere among its loaded bridges. */
    static boolean isActive(int channel) {
        Network network = NETWORKS.get(channel);
        if (network == null) return false;
        boolean in = false, out = false;
        for (EinsteinRosenBridgeBlockEntity bridge : network.bridges) {
            for (Direction d : Direction.values()) {
                HopperSide mode = bridge.getSide(d);
                in |= mode == HopperSide.INPUT;
                out |= mode == HopperSide.OUTPUT;
            }
            if (in && out) return true;
        }
        return false;
    }

    /**
     * Every OUTPUT side of every loaded bridge on the channel, in round-robin order, so over time
     * every output gets its turn at the remainder of an uneven split.
     */
    static List<HopperOutput> outputs(int channel) {
        Network network = NETWORKS.get(channel);
        if (network == null) return List.of();
        List<HopperOutput> found = new ArrayList<>();
        for (EinsteinRosenBridgeBlockEntity bridge : network.bridges) {
            if (bridge.getLevel() == null || bridge.isRemoved()) continue;
            for (Direction d : Direction.values()) {
                if (bridge.getSide(d) == HopperSide.OUTPUT) {
                    found.add(new HopperOutput(bridge.getLevel(), bridge.getBlockPos(), d));
                }
            }
        }
        if (found.size() > 1) Collections.rotate(found, -Math.floorMod(network.rotation++, found.size()));
        return found;
    }

    /**
     * Guards against infinite loops: an OUTPUT can feed an INPUT of the same network, directly or
     * through other blocks. Per channel, so the whole network routes one thing at a time.
     */
    static boolean beginRouting(int channel) {
        Network network = NETWORKS.computeIfAbsent(channel, c -> new Network());
        if (network.routing) return false;
        network.routing = true;
        return true;
    }

    static void endRouting(int channel) {
        Network network = NETWORKS.get(channel);
        if (network == null) return;
        network.routing = false;
        if (network.bridges.isEmpty()) NETWORKS.remove(channel);
    }
}
