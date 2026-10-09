package net.bobofraggins.mobfarmingsupplies.toilet.fabric;

import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.bobofraggins.mobfarmingsupplies.advancement.MFSTriggers;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Item storage on every side of a Toilet: accepts any item and voids it. Holds nothing, so
 * nothing can be extracted.
 */
@SuppressWarnings("UnstableApiUsage")
public class ToiletItemStorage implements InsertionOnlyStorage<ItemVariant> {

    private final Level level;
    private final BlockPos pos;

    public ToiletItemStorage(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) return 0;
        // "Royal Flush": credit nearby players for what actually went down, not for simulations.
        String item = BuiltInRegistries.ITEM.getKey(resource.getItem()).toString();
        transaction.addOuterCloseCallback(result -> {
            if (result.wasCommitted()) MFSTriggers.triggerNear(MFSTriggers.TOILET_FLUSHED, level, pos, item);
        });
        return maxAmount;
    }
}
