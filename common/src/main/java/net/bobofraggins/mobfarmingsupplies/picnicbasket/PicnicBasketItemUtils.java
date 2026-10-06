package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import org.jetbrains.annotations.Nullable;

/**
 * Reads and writes a Picnic Basket item-form {@link ItemStack}'s
 * {@link DataComponents#BLOCK_ENTITY_DATA} by round-tripping it through a throwaway,
 * unattached {@link PicnicBasketBlockEntity}. Since block and item forms share the exact
 * same NBT shape, this also means an item-form basket's contents transfer for free when
 * placed as a block, and a block's contents transfer for free when broken (see
 * {@link PicnicBasketBlock#getDrops}).
 */
public final class PicnicBasketItemUtils {

    private PicnicBasketItemUtils() {}

    /** Decodes {@code stack}'s block-entity data into a fresh throwaway block entity. */
    public static PicnicBasketBlockEntity readBasketData(Player player, ItemStack stack) {
        PicnicBasketBlockEntity temp = new PicnicBasketBlockEntity(
                BlockPos.ZERO, Registration.PICNIC_BASKET.get().defaultBlockState());
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return temp;
        HolderLookup.Provider registries = player.level().registryAccess();
        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, registries, data.copyTagWithoutId());
        temp.loadCustomOnly(input);
        return temp;
    }

    /** Encodes {@code temp}'s current state back into {@code stack}'s block-entity data. */
    public static void writeBasketData(Player player, ItemStack stack, PicnicBasketBlockEntity temp) {
        HolderLookup.Provider registries = player.level().registryAccess();
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        temp.saveCustomOnly(output);
        BlockItem.setBlockEntityData(stack, temp.getType(), output);
    }

    public static boolean isBasket(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Registration.PICNIC_BASKET_ITEM.get();
    }

    /**
     * Canonical scan for a basket carried by {@code player}: main inventory (0-35) → offhand
     * → accessory slot (Curios/Trinkets). Used identically by the auto-feed handler, the
     * "open worn basket" keybind, and (indirectly) item-form interactions. Returns {@code null}
     * if no basket is found anywhere.
     */
    @Nullable
    public static PicnicBasketLocator findBasketLocator(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            if (isBasket(inv.getItem(i))) return new PicnicBasketLocator.PlayerSlot(i);
        }
        if (isBasket(inv.getItem(Inventory.SLOT_OFFHAND))) {
            return new PicnicBasketLocator.PlayerSlot(Inventory.SLOT_OFFHAND);
        }
        if (PicnicBasketAccessoryAvailability.isLoaded()
                && isBasket(PicnicBasketAccessoryPlatform.findAccessoryBasket(player))) {
            return new PicnicBasketLocator.Accessory();
        }
        return null;
    }
}
