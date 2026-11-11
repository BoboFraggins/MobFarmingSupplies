package net.bobofraggins.mobfarmingsupplies.runestone.neoforge;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/** NeoForge side of {@code RunestoneAccessoryPlatform}: runestones in a Curios slot. Only loaded with Curios present. */
public final class RunestoneAccessoryPlatformImpl {

    private RunestoneAccessoryPlatformImpl() {}

    public static boolean isWearing(Player player, Item item) {
        ICuriosItemHandler inv = CuriosApi.getCuriosInventoryOrNull(player);
        return inv != null && inv.isEquipped(item);
    }
}
