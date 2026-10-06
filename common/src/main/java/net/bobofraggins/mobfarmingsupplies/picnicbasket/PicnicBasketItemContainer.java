package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Ephemeral 54-slot container backing an item-form Picnic Basket's menu. Loads its initial
 * contents from the source {@link ItemStack} (located via {@link PicnicBasketLocator}) and
 * writes any change back to that same stack immediately.
 *
 * <p>Server-side only — the client always uses a throwaway empty {@link SimpleContainer}
 * for item-form menus, since vanilla's slot-sync packets repopulate it regardless of backing.
 */
public class PicnicBasketItemContainer extends SimpleContainer {

    private final Player player;
    private final PicnicBasketLocator locator;
    private boolean loading;

    public PicnicBasketItemContainer(Player player, PicnicBasketLocator locator) {
        super(PicnicBasketBlockEntity.SLOT_COUNT);
        this.player = player;
        this.locator = locator;
        loading = true;
        PicnicBasketBlockEntity temp = PicnicBasketItemUtils.readBasketData(player, locator.get(player));
        for (int i = 0; i < getContainerSize(); i++) {
            setItem(i, temp.inventory.getItem(i));
        }
        loading = false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading) persist();
    }

    private void persist() {
        ItemStack stack = locator.get(player);
        if (stack.isEmpty()) return;
        PicnicBasketBlockEntity temp = PicnicBasketItemUtils.readBasketData(player, stack);
        for (int i = 0; i < getContainerSize(); i++) {
            temp.inventory.setItem(i, getItem(i));
        }
        PicnicBasketItemUtils.writeBasketData(player, stack, temp);
        locator.set(player, stack);
    }
}
