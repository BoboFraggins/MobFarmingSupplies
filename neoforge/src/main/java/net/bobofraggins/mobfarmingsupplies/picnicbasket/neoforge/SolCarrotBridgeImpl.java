package net.bobofraggins.mobfarmingsupplies.picnicbasket.neoforge;

import net.bobofraggins.mobfarmingsupplies.external.solcarrot.SolCarrotIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SolCarrotBridgeImpl {

    private SolCarrotBridgeImpl() {}

    public static List<ItemStack> preferUneatenFoods(Player player, List<ItemStack> candidates) {
        if (!SolCarrotIntegration.isInstalled()) return candidates;
        List<ItemStack> uneaten = new ArrayList<>();
        for (ItemStack stack : candidates) {
            if (SolCarrotIntegration.isNewFood(player, stack)) uneaten.add(stack);
        }
        return uneaten.isEmpty() ? candidates : uneaten;
    }
}
