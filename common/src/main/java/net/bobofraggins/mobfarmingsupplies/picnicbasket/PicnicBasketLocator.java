package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Identifies where a Picnic Basket item-form {@link ItemStack} lives, captured once when the
 * item-form UI is opened so changes made through {@link PicnicBasketItemContainer} can be
 * written back to the correct place even if the stack's slot index shifts during the menu's
 * lifetime is not otherwise tracked.
 */
public sealed interface PicnicBasketLocator {

    ItemStack get(Player player);

    void set(Player player, ItemStack stack);

    record Hand(InteractionHand hand) implements PicnicBasketLocator {
        @Override
        public ItemStack get(Player player) {
            return player.getItemInHand(hand);
        }

        @Override
        public void set(Player player, ItemStack stack) {
            player.setItemInHand(hand, stack);
        }
    }

    record PlayerSlot(int index) implements PicnicBasketLocator {
        @Override
        public ItemStack get(Player player) {
            return player.getInventory().getItem(index);
        }

        @Override
        public void set(Player player, ItemStack stack) {
            player.getInventory().setItem(index, stack);
        }
    }

    /** A basket worn in a Curios (NeoForge) or Trinkets Updated (Fabric) accessory slot. */
    record Accessory() implements PicnicBasketLocator {
        @Override
        public ItemStack get(Player player) {
            return PicnicBasketAccessoryPlatform.findAccessoryBasket(player);
        }

        @Override
        public void set(Player player, ItemStack stack) {
            PicnicBasketAccessoryPlatform.writeAccessoryBasket(player, stack);
        }
    }
}
