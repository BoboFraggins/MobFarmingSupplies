package net.bobofraggins.mobfarmingsupplies.picnicbasket.neoforge;

import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * NeoForge implementation of {@code PicnicBasketAccessoryPlatform}, backed by Curios.
 * Curios is a soft (optional) dependency — every entry point is guarded so the mod keeps
 * working with Curios absent.
 */
public final class PicnicBasketAccessoryPlatformImpl {

    private PicnicBasketAccessoryPlatformImpl() {}

    public static ItemStack findAccessoryBasket(Player player) {
        try {
            ICuriosItemHandler inv = player.getCapability(CuriosCapability.INVENTORY);
            if (inv == null) return ItemStack.EMPTY;
            for (var entry : inv.getCurios().entrySet()) {
                IDynamicStackHandler handler = entry.getValue().getStacks();
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (PicnicBasketItemUtils.isBasket(stack)) return stack;
                }
            }
        } catch (NoClassDefFoundError | Exception ignored) {
            // Curios not installed — skip
        }
        return ItemStack.EMPTY;
    }

    public static void writeAccessoryBasket(Player player, ItemStack stack) {
        try {
            ICuriosItemHandler inv = player.getCapability(CuriosCapability.INVENTORY);
            if (inv == null) return;
            for (var entry : inv.getCurios().entrySet()) {
                IDynamicStackHandler handler = entry.getValue().getStacks();
                for (int i = 0; i < handler.getSlots(); i++) {
                    if (PicnicBasketItemUtils.isBasket(handler.getStackInSlot(i))) {
                        handler.setStackInSlot(i, stack);
                        return;
                    }
                }
            }
        } catch (NoClassDefFoundError | Exception ignored) {
            // Curios not installed — skip
        }
    }
}
