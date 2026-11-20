package net.bobofraggins.mobfarmingsupplies.essencehopper.fabric;

import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.essencehopper.EssenceBottleItem;
import net.bobofraggins.mobfarmingsupplies.tank.FabricTankFluidStorage;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** A bottle is {@link EssenceBottleItem#MB} millibuckets here too (see {@link FabricTankFluidStorage#DROPLETS_PER_MB}). */
@SuppressWarnings("UnstableApiUsage")
public final class EssenceBottlePlatformImpl {

    private static final long BOTTLE = EssenceBottleItem.MB * FabricTankFluidStorage.DROPLETS_PER_MB;

    private EssenceBottlePlatformImpl() {}

    @Nullable
    public static Essence extractBottle(Level level, BlockPos pos, Direction side) {
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, side);
        if (storage == null || !storage.supportsExtraction()) return null;
        for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
            if (view.getAmount() < BOTTLE) continue;
            FluidVariant variant = view.getResource();
            Essence essence = essenceOf(variant);
            if (essence == null) continue;
            try (Transaction tx = Transaction.openOuter()) {
                if (storage.extract(variant, BOTTLE, tx) == BOTTLE) {
                    tx.commit();
                    return essence;
                }
            }
        }
        return null;
    }

    public static boolean insertBottle(Level level, BlockPos pos, Direction side, Essence essence) {
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, side);
        if (storage == null || !storage.supportsInsertion()) return false;
        FluidVariant variant = FluidVariant.of(Registration.ESSENCE_SOURCES.get(essence).get());
        try (Transaction tx = Transaction.openOuter()) {
            if (storage.insert(variant, BOTTLE, tx) != BOTTLE) return false;
            tx.commit();
            return true;
        }
    }

    @Nullable
    private static Essence essenceOf(FluidVariant variant) {
        for (Essence essence : Essence.values()) {
            if (variant.isOf(Registration.ESSENCE_SOURCES.get(essence).get())) return essence;
        }
        return null;
    }
}
