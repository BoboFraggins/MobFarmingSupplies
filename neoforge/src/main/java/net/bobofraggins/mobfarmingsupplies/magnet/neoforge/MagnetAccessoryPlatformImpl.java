package net.bobofraggins.mobfarmingsupplies.magnet.neoforge;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/** NeoForge side of {@code MagnetAccessoryPlatform}: the magnet in a Curios slot. Only loaded with Curios present. */
public final class MagnetAccessoryPlatformImpl {

    private MagnetAccessoryPlatformImpl() {}

    public static ItemStack findWornMagnet(Player player) {
        ICuriosItemHandler inv = CuriosApi.getCuriosInventoryOrNull(player);
        if (inv == null) return ItemStack.EMPTY;
        return inv.findFirstCurio(worn -> worn.is(Registration.SUPERCONDUCTING_MAGNET.get()))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }

    public static void replaceWornMagnet(Player player, ItemStack stack) {
        ICuriosItemHandler inv = CuriosApi.getCuriosInventoryOrNull(player);
        if (inv == null) return;
        inv.findFirstCurio(worn -> worn.is(Registration.SUPERCONDUCTING_MAGNET.get())).ifPresent(result ->
                inv.setEquippedCurio(result.slotContext().identifier(), result.slotContext().index(), stack));
    }
}
