package net.bobofraggins.mobfarmingsupplies.essencehopper;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Essence Hopper's block entity: the Absorption Hopper's zone, push sides and tank,
 * without its pickups: its tank fills with the essence of mobs that die in its zone, chosen by the
 * Essence Filter in its slot (see {@link EssenceCollector}).
 *
 * <p>Slot {@value #UPGRADE_SLOT} of the inherited inventory holds the filter, so it's saved, and
 * kept by the item when the block is broken, as an Absorption Hopper's contents are.
 */
public class EssenceHopperBlockEntity extends AbsorptionHopperBlockEntity {

    public static final int UPGRADE_SLOT = 0;

    public EssenceHopperBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("essence_hopper"), pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) EssenceCollector.add(level, worldPosition);
    }

    // Called by NeoForge IBlockEntityExtension when the chunk containing this BE unloads.
    // No @Override because onChunkUnloaded() is a NeoForge extension, not vanilla.
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide()) EssenceCollector.remove(level, worldPosition);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) EssenceCollector.remove(level, worldPosition);
        super.preRemoveSideEffects(pos, state);
    }

    /** Collecting happens when a mob dies, not by searching the zone. */
    @Override
    protected void pickupPhase(Level level, BlockPos pos) {
    }

    /** Whether {@code mob}, dying, would give this hopper its {@code essence}. */
    boolean canCollect(Essence essence, LivingEntity mob) {
        return inventory.getItem(UPGRADE_SLOT).getItem() instanceof EssenceFilterItem filter
                && filter.essence() == essence
                && pickupBox().intersects(mob.getBoundingBox())
                && tankAmount < TANK_CAPACITY
                && (tankFluid.isEmpty() || Essence.of(tankFluid) == essence);
    }

    /** One death's worth of {@code essence} into the tank, as much as fits. */
    void collect(Essence essence) {
        if (tankFluid.isEmpty()) {
            // In full: in a block entity, "Registration" is a type it inherits, not the mod's.
            tankFluid = FluidStack.create(
                    net.bobofraggins.mobfarmingsupplies.register.Registration.ESSENCE_SOURCES.get(essence).get(), 1);
        }
        tankAmount = Math.min(TANK_CAPACITY, tankAmount + MFSConfig.getEssencePerDeath());
        setChanged();
        transferSchedule().reset();
    }

    /** The only item here is its filter, which stays put. */
    @Override
    protected boolean pushItems(Level level, BlockPos pos) {
        return false;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new EssenceHopperMenu(syncId, playerInventory, this);
    }
}
