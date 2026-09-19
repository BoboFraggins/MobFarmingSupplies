package net.bobofraggins.mobfarmingsupplies.picnicbasket.neoforge;

import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketItemUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * NeoForge implementation of {@code PicnicBasketAccessoryPlatform}, backed by Curios.
 *
 * <p>Uses {@link ICuriosItemHandler#findFirstCurio}/{@code setEquippedCurio} rather than
 * walking {@code IDynamicStackHandler} directly — that type extends NeoForge's old
 * {@code IItemHandlerModifiable} capability interface, which NeoForge removed entirely in
 * 26.3 (replaced by the Transfer API), so Curios' 26.2-era API jar can't even resolve it
 * against a 26.3 classpath. The higher-level {@code SlotResult}-based methods avoid that
 * type entirely and still compile fine.
 *
 * <p>Curios is a soft (optional) dependency. This class is only ever loaded after
 * {@code PicnicBasketAccessoryAvailability.isLoaded()} confirms Curios is present —
 * see that class's javadoc for why that check has to happen <em>before</em> calling in here,
 * not just via a try/catch inside these methods.
 */
public final class PicnicBasketAccessoryPlatformImpl {

    private PicnicBasketAccessoryPlatformImpl() {}

    public static ItemStack findAccessoryBasket(Player player) {
        ICuriosItemHandler inv = CuriosApi.getCuriosInventoryOrNull(player);
        if (inv == null) return ItemStack.EMPTY;
        return inv.findFirstCurio(PicnicBasketItemUtils::isBasket)
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }

    public static void writeAccessoryBasket(Player player, ItemStack stack) {
        ICuriosItemHandler inv = CuriosApi.getCuriosInventoryOrNull(player);
        if (inv == null) return;
        inv.findFirstCurio(PicnicBasketItemUtils::isBasket).ifPresent(result ->
                inv.setEquippedCurio(result.slotContext().identifier(), result.slotContext().index(), stack));
    }
}
