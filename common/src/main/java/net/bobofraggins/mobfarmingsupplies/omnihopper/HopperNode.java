package net.bobofraggins.mobfarmingsupplies.omnihopper;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A block that moves resources from its INPUT sides to a set of OUTPUT targets: the
 * Omnidirectional Hopper (its own OUTPUT sides) and the Einstein-Rosen Bridge (the OUTPUT sides of
 * every loaded bridge on its channel). The platform transfer code works against this.
 */
public interface HopperNode {

    @Nullable Level getLevel();

    BlockPos getBlockPos();

    HopperSide getSide(Direction dir);

    /** Has somewhere to take from and somewhere to put. */
    boolean isActive();

    /** Whether an item may move (Item Filters); fluids, energy and chemicals always may. */
    boolean allowsItem(ItemStack stack);

    /** Where resources go, in round-robin order (see {@link net.bobofraggins.mobfarmingsupplies.logisticsorter.EvenSplit}). */
    List<HopperOutput> outputTargets();

    /** Guards against infinite loops when nodes feed each other. Returns false if already routing. */
    boolean beginRouting();

    void endRouting();

    /**
     * Called after something is delivered to {@code out} for real (never for a simulation or a
     * transfer that's rolled back).
     */
    default void delivered(HopperOutput out) {}
}
