package net.bobofraggins.mobfarmingsupplies.picnicbasket.fabric;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric implementation of {@code PicnicBasketAccessoryPlatform}, backed by Trinkets Updated
 * ({@code eu.pb4.trinkets}). Trinkets Updated is a soft (optional) dependency — every entry
 * point is guarded so the mod keeps working with it absent.
 */
public final class PicnicBasketAccessoryPlatformImpl {

    private PicnicBasketAccessoryPlatformImpl() {}

    public static ItemStack findAccessoryBasket(Player player) {
        try {
            TrinketAttachment attachment = TrinketsApi.getAttachment(player);
            if (attachment == null) return ItemStack.EMPTY;
            for (TrinketSlotAccess slot : attachment.allEquipped(false)) {
                ItemStack stack = slot.get();
                if (PicnicBasketItemUtils.isBasket(stack)) return stack;
            }
        } catch (NoClassDefFoundError | Exception ignored) {
            // Trinkets Updated not installed — skip
        }
        return ItemStack.EMPTY;
    }

    public static void writeAccessoryBasket(Player player, ItemStack stack) {
        try {
            TrinketAttachment attachment = TrinketsApi.getAttachment(player);
            if (attachment == null) return;
            for (TrinketSlotAccess slot : attachment.allEquipped(false)) {
                if (PicnicBasketItemUtils.isBasket(slot.get())) {
                    slot.set(stack);
                    return;
                }
            }
        } catch (NoClassDefFoundError | Exception ignored) {
            // Trinkets Updated not installed — skip
        }
    }
}
