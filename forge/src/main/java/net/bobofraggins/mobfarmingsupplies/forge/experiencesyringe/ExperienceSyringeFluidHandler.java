package net.bobofraggins.mobfarmingsupplies.forge.experiencesyringe;

import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.ExperienceSyringeItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/**
 * Exposes the Experience Syringe as a fluid handler: its stored XP is the {@code c:experience}
 * fluid at the ratio defined in {@link ExperienceSyringeItem} (1 XP = 20 mB). Changes the stack it
 * was made for, which {@link #getContainer} returns.
 */
public class ExperienceSyringeFluidHandler implements IFluidHandlerItem {

    private static final TagKey<Fluid> EXPERIENCE_FLUID_TAG =
            TagKey.create(Registries.FLUID, new ResourceLocation("c", "experience"));

    private final ItemStack container;

    public ExperienceSyringeFluidHandler(ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    private int storedXp() {
        return ItemComponents.getOrDefault(container, Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), 0);
    }

    private int capacityMb() {
        return ExperienceSyringeItem.xpToMb(ExperienceSyringeItem.CAPACITY);
    }

    private void setStoredMb(int mb) {
        int newXp = ExperienceSyringeItem.mbToXp(mb);
        if (newXp <= 0) {
            ItemComponents.remove(container, Registration.EXPERIENCE_SYRINGE_STORED_XP.get());
        } else {
            ItemComponents.set(container, Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), newXp);
        }
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        int stored = storedXp();
        Fluid xpFluid = Registration.XP_JUICE_SOURCE.get();
        if (stored == 0 || xpFluid == Fluids.EMPTY) return FluidStack.EMPTY;
        return new FluidStack(xpFluid, ExperienceSyringeItem.xpToMb(stored));
    }

    @Override
    public int getTankCapacity(int tank) {
        return capacityMb();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.isEmpty() && net.bobofraggins.mobfarmingsupplies.forge.transfer.api.fluid.ForgeFluids.is(stack, EXPERIENCE_FLUID_TAG);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || !isFluidValid(0, resource)) return 0;
        int stored = ExperienceSyringeItem.xpToMb(storedXp());
        int filled = Math.min(resource.getAmount(), capacityMb() - stored);
        if (filled <= 0) return 0;
        if (action.execute()) setStoredMb(stored + filled);
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !net.bobofraggins.mobfarmingsupplies.forge.transfer.api.fluid.ForgeFluids.isSameFluidSameComponents(resource, getFluidInTank(0))) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack stored = getFluidInTank(0);
        if (container.getCount() != 1 || stored.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
        int drained = Math.min(maxDrain, stored.getAmount());
        if (action.execute()) setStoredMb(stored.getAmount() - drained);
        return net.bobofraggins.mobfarmingsupplies.forge.transfer.api.fluid.ForgeFluids.copyWithAmount(stored, drained);
    }
}
