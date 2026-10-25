package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * Stores experience fluid (anything in {@code c:experience}, such as XP Juice) the way a
 * {@link TankBlockEntity} stores fluid, up to {@link #CAPACITY} mB. Pipes, buckets and the tank
 * fluid handlers on both loaders all go through the tank code, which asks {@link #canHold}.
 */
public class ExperiencePylonBlockEntity extends TankBlockEntity {

    public static final TagKey<Fluid> EXPERIENCE =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "experience"));

    /** The most an int can count, in mB. */
    public static final long CAPACITY = Integer.MAX_VALUE;

    public ExperiencePylonBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("experience_pylon"), pos, state);
    }

    @SuppressWarnings("deprecation") // builtInRegistryHolder(): no direct Fluid.is(TagKey)
    public static boolean isExperience(FluidStack fluid) {
        return !fluid.isEmpty() && fluid.getFluid().builtInRegistryHolder().is(EXPERIENCE);
    }

    @Override
    public boolean canHold(FluidStack fluid) {
        return isExperience(fluid);
    }

    @Override
    public long getCapacity() {
        return CAPACITY;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ExperiencePylonMenu(id, inv, worldPosition, transferContainer);
    }
}
