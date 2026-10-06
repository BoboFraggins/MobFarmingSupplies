package net.bobofraggins.mobfarmingsupplies.picnicbasket.fabric;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Spice of Life: Carrot Edition's integration is NeoForge-only; permanent no-op on Fabric. */
public final class SolCarrotBridgeImpl {

    private SolCarrotBridgeImpl() {}

    public static List<ItemStack> preferUneatenFoods(Player player, List<ItemStack> candidates) {
        return candidates;
    }
}
