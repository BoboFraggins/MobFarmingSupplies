package net.bobofraggins.mobfarmingsupplies.experiencesyringe;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Exposes the Experience Syringe as a fluid {@link net.neoforged.neoforge.transfer.ResourceHandler}{@code <FluidResource>}.
 *
 * <p>Extends {@link ItemAccessResourceHandler} so that insert/extract changes are propagated
 * back to the player's inventory through the transaction system via {@link ItemAccess#exchange}.
 * XP storage is presented as the {@code c:experience} fluid at the ratio defined in
 * {@link ExperienceSyringeItem} (1 XP = 20 mB).
 */
public class ExperienceSyringeFluidHandler extends ItemAccessResourceHandler<FluidResource> {

    private static final TagKey<Fluid> EXPERIENCE_FLUID_TAG =
            TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "experience"));

    public ExperienceSyringeFluidHandler(ItemAccess itemAccess) {
        super(itemAccess, 1);
    }

    private int storedXpFrom(ItemResource resource) {
        return resource.toStack(1).getOrDefault(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), 0);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        int stored = storedXpFrom(accessResource);
        if (stored == 0) return FluidResource.EMPTY;
        Fluid xpFluid = Registration.XP_JUICE_SOURCE.get();
        return xpFluid == Fluids.EMPTY ? FluidResource.EMPTY : FluidResource.of(xpFluid);
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        return ExperienceSyringeItem.xpToMb(storedXpFrom(accessResource));
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        int newXp = ExperienceSyringeItem.mbToXp(newAmount);
        ItemStack updated = accessResource.toStack(1);
        if (newXp <= 0) {
            updated.remove(Registration.EXPERIENCE_SYRINGE_STORED_XP.get());
        } else {
            updated.set(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), newXp);
        }
        return ItemResource.of(updated);
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return ExperienceSyringeItem.xpToMb(ExperienceSyringeItem.CAPACITY);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return !resource.isEmpty() && resource.toStack(1).is(EXPERIENCE_FLUID_TAG);
    }
}
