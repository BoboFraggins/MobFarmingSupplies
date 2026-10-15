package net.bobofraggins.mobfarmingsupplies.picnicbasket.fabric;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketItemUtils;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric implementation of {@code PicnicBasketAccessoryPlatform}, backed by Trinkets
 * ({@code dev.emi.trinkets}).
 *
 * <p>Trinkets is a soft (optional) dependency. This class is only ever loaded after
 * {@code PicnicBasketAccessoryAvailability.isLoaded()} confirms it is present — see that
 * class's javadoc for why that check has to happen <em>before</em> calling in here, not just
 * via a try/catch inside these methods.
 */
public final class PicnicBasketAccessoryPlatformImpl {

    private PicnicBasketAccessoryPlatformImpl() {}

    public static ItemStack findAccessoryBasket(Player player) {
        TrinketComponent trinkets = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinkets == null) return ItemStack.EMPTY;
        for (Tuple<SlotReference, ItemStack> slot : trinkets.getAllEquipped()) {
            ItemStack stack = slot.getB();
            if (PicnicBasketItemUtils.isBasket(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static void writeAccessoryBasket(Player player, ItemStack stack) {
        TrinketComponent trinkets = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinkets == null) return;
        for (Tuple<SlotReference, ItemStack> slot : trinkets.getAllEquipped()) {
            if (PicnicBasketItemUtils.isBasket(slot.getB())) {
                slot.getA().inventory().setItem(slot.getA().index(), stack);
                return;
            }
        }
    }
}
