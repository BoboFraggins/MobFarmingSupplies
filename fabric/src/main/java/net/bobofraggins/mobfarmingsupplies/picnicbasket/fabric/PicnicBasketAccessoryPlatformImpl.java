package net.bobofraggins.mobfarmingsupplies.picnicbasket.fabric;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric implementation of {@code PicnicBasketAccessoryPlatform}, backed by Trinkets Updated
 * ({@code eu.pb4.trinkets}).
 *
 * <p>Trinkets Updated is a soft (optional) dependency. This class is only ever loaded after
 * {@code PicnicBasketAccessoryAvailability.isLoaded()} confirms it is present — see that
 * class's javadoc for why that check has to happen <em>before</em> calling in here, not just
 * via a try/catch inside these methods.
 */
public final class PicnicBasketAccessoryPlatformImpl {

    private PicnicBasketAccessoryPlatformImpl() {}

    public static ItemStack findAccessoryBasket(Player player) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        if (attachment == null) return ItemStack.EMPTY;
        for (TrinketSlotAccess slot : attachment.allEquipped(false)) {
            ItemStack stack = slot.get();
            if (PicnicBasketItemUtils.isBasket(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static void writeAccessoryBasket(Player player, ItemStack stack) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        if (attachment == null) return;
        for (TrinketSlotAccess slot : attachment.allEquipped(false)) {
            if (PicnicBasketItemUtils.isBasket(slot.get())) {
                slot.set(stack);
                return;
            }
        }
    }
}
