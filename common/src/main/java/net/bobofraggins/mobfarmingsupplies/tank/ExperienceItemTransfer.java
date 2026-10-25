package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.PylonExperience;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.ExperienceSyringeItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Experience between a block's experience tank (the Experience Pylon, the XP Powered Incubator)
 * and a held tank in bucket mode or an Experience Syringe, on a right-click. One holding experience
 * empties into the block; an empty one fills up from it, where the block lets experience be taken.
 */
public final class ExperienceItemTransfer {

    private static final long MB_PER_XP = PylonExperience.MB_PER_XP;

    private ExperienceItemTransfer() {}

    /** Whether this is a held container these transfers handle. */
    public static boolean handles(ItemStack stack) {
        return stack.getItem() instanceof TankBlockItem || stack.getItem() instanceof ExperienceSyringeItem;
    }

    /** Whether the held tank or syringe holds experience (so it would empty rather than fill). */
    public static boolean holdsExperience(ItemStack stack) {
        if (stack.getItem() instanceof ExperienceSyringeItem) {
            return stack.getOrDefault(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), 0) > 0;
        }
        return !stack.getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY).isEmpty();
    }

    /**
     * The held item after the transfer, or null if nothing moved.
     *
     * @param canTake whether an empty one may fill up from the block
     */
    @Nullable
    public static ItemStack transfer(TankBlockEntity be, ItemStack stack, boolean canTake) {
        return stack.getItem() instanceof TankBlockItem
                ? withTank(be, stack, canTake)
                : withSyringe(be, stack, canTake);
    }

    /** A bucket-mode tank's contents after emptying into or filling from the block. */
    @Nullable
    private static ItemStack withTank(TankBlockEntity be, ItemStack stack, boolean canTake) {
        TankContents c = stack.getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY);
        if (!c.bucketMode()) return null;
        ItemStack result = stack.copyWithCount(1);
        if (!c.isEmpty()) {
            long stored = be.insert(c.storedFluid(), c.amount(), false); // the block refuses anything but experience
            if (stored <= 0) return null;
            long left = c.amount() - stored;
            result.set(Registration.TANK_CONTENTS.get(), left > 0
                    ? new TankContents(c.storedFluid(), left, true)
                    : new TankContents(FluidStack.empty(), 0, true));
        } else {
            if (!canTake) return null;
            FluidStack taken = be.extract(TankBlockItem.capacityOf(stack), false);
            if (taken.isEmpty()) return null;
            result.set(Registration.TANK_CONTENTS.get(),
                    new TankContents(taken.copyWithAmount(1), taken.getAmount(), true));
        }
        return result;
    }

    /**
     * A syringe after emptying into or filling from the block. The syringe counts whole
     * experience points, so only whole points' worth of fluid moves.
     */
    @Nullable
    private static ItemStack withSyringe(TankBlockEntity be, ItemStack stack, boolean canTake) {
        int stored = stack.getOrDefault(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), 0);
        ItemStack result = stack.copyWithCount(1);
        if (stored > 0) {
            FluidStack fluid = be.isLocked() ? be.getStoredFluid() : FluidStack.create(Registration.XP_JUICE_SOURCE.get(), 1);
            long points = Math.min(stored, be.insert(fluid, (long) stored * MB_PER_XP, true) / MB_PER_XP);
            if (points <= 0) return null;
            be.insert(fluid, points * MB_PER_XP, false);
            setSyringeXp(result, stored - (int) points);
        } else {
            if (!canTake) return null;
            long points = Math.min(ExperienceSyringeItem.CAPACITY, be.getAmount() / MB_PER_XP);
            if (points <= 0) return null;
            be.extract(points * MB_PER_XP, false);
            setSyringeXp(result, (int) points);
        }
        return result;
    }

    private static void setSyringeXp(ItemStack syringe, int xp) {
        if (xp > 0) syringe.set(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(), xp);
        else syringe.remove(Registration.EXPERIENCE_SYRINGE_STORED_XP.get());
    }
}
