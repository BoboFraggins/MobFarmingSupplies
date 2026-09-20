package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * Glass bottle / water bottle / bottle o' enchanting transfer logic for the Tank, shared by
 * both the direct block right-click ({@link TankBlock#useItemOn}) and the Tank UI's
 * transfer-slot mechanism ({@link TankBlockEntity#tickFluidTransfer}).
 *
 * <p>Bottles are plain vanilla items, not registered fluid-container capabilities/storages on
 * either loader — the generic platform fluid-item transfer path
 * ({@link TankBlockEntityPlatform#tryTransferFluidWithItem}) only covers buckets and other
 * items that actually expose that capability/storage, which bottles never do. This logic has
 * to run as a dedicated, bottle-specific check before (or instead of) that generic path.
 */
public final class TankBottleTransfer {

    private static final long BOTTLE_MB = 250;
    private static final TagKey<Fluid> EXPERIENCE_TAG =
            TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "experience"));

    private TankBottleTransfer() {}

    /**
     * Whether {@code stack} is one of the bottle items this class handles. Used by
     * {@link TankMenu} to accept bottles into the transfer-in slot even on platforms (NeoForge)
     * where they expose no generic fluid-item capability at all — {@link #tryTransfer} alone
     * being correct doesn't help if the slot's own placement filter rejects the item before it
     * ever reaches that logic.
     */
    public static boolean isBottle(ItemStack stack) {
        return stack.is(Items.GLASS_BOTTLE) || stack.is(Items.POTION) || stack.is(Items.EXPERIENCE_BOTTLE);
    }

    /**
     * Attempts a bottle-based transfer between {@code input} and {@code be}.
     *
     * @return the resulting item stack if a transfer happened, or {@code null} if {@code input}
     *         isn't a bottle-shaped item or no transfer was currently possible (wrong fluid,
     *         tank full/empty, ...)
     */
    @Nullable
    public static ItemStack tryTransfer(TankBlockEntity be, ItemStack input) {
        if (input.is(Items.GLASS_BOTTLE)) {
            FluidStack simulated = be.extract(BOTTLE_MB, true);
            if (simulated.getAmount() >= BOTTLE_MB) {
                if (simulated.getRawFluid() == Fluids.WATER) {
                    be.extract(BOTTLE_MB, false);
                    return PotionContents.createItemStack(Items.POTION, Potions.WATER);
                }
                if (simulated.getFluid().builtInRegistryHolder().is(EXPERIENCE_TAG)) {
                    be.extract(BOTTLE_MB, false);
                    return new ItemStack(Items.EXPERIENCE_BOTTLE);
                }
            }
            return null;
        }

        if (input.is(Items.POTION)) {
            PotionContents contents = input.get(DataComponents.POTION_CONTENTS);
            if (contents == null || !contents.is(Potions.WATER)) return null;
            FluidStack water = FluidStack.create(Fluids.WATER, BOTTLE_MB);
            long inserted = be.insert(water, BOTTLE_MB, true);
            if (inserted >= BOTTLE_MB) {
                be.insert(water, BOTTLE_MB, false);
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            return null;
        }

        if (input.is(Items.EXPERIENCE_BOTTLE)) {
            FluidStack xp = FluidStack.create(Registration.XP_JUICE_SOURCE.get(), BOTTLE_MB);
            long inserted = be.insert(xp, BOTTLE_MB, true);
            if (inserted >= BOTTLE_MB) {
                be.insert(xp, BOTTLE_MB, false);
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            return null;
        }

        return null;
    }
}
