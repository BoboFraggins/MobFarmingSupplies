package net.bobofraggins.mobfarmingsupplies.colorworkstation;

import dev.architectury.fluid.FluidStack;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Color Workstation (lower half): a buffer of Essence of Color, up to {@value #CAPACITY} mB,
 * and sixteen dye outputs, one per vanilla dye. The outputs aren't stored: each shows as many of
 * its dye as the buffer pays for (up to a stack), and taking dyes, by hand or by hopper or pipe,
 * costs {@value #MB_PER_DYE} mB each. It keeps its essence when broken.
 */
public class ColorWorkstationBlockEntity extends TankBlockEntity {

    /** 16 buckets. */
    public static final long CAPACITY = 16_000;
    public static final int MB_PER_DYE = 10;

    /**
     * The dyes, by output slot, left to right and top to bottom: black top left, white bottom
     * right, matching the palette on the workstation's lid.
     */
    public static final List<Item> DYES = List.of(
            Items.BLACK_DYE, Items.BROWN_DYE, Items.GRAY_DYE, Items.RED_DYE,
            Items.BLUE_DYE, Items.MAGENTA_DYE, Items.PURPLE_DYE, Items.PINK_DYE,
            Items.GREEN_DYE, Items.ORANGE_DYE, Items.LIME_DYE, Items.YELLOW_DYE,
            Items.CYAN_DYE, Items.LIGHT_GRAY_DYE, Items.LIGHT_BLUE_DYE, Items.WHITE_DYE);

    /** The sixteen outputs, made from the buffer on demand. */
    final DyeOutputs dyes = new DyeOutputs();

    public ColorWorkstationBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("color_workstation"), pos, state);
    }

    @Override
    public boolean canHold(FluidStack fluid) {
        return Essence.of(fluid) == Essence.COLOR;
    }

    @Override
    public long getCapacity() {
        return CAPACITY;
    }

    /** How many dyes the buffer pays for right now. */
    public int dyesAvailable() {
        return (int) Math.min(amount / MB_PER_DYE, Integer.MAX_VALUE);
    }

    /** Sets the buffer to {@code mb} (for the outputs, and for undoing a pipe's transaction). */
    public void setAmount(long mb) {
        if (mb == amount) return;
        amount = Math.max(0, Math.min(mb, CAPACITY));
        if (amount == 0) storedFluid = FluidStack.empty();
        else if (storedFluid.isEmpty()) storedFluid = FluidStack.create(net.bobofraggins.mobfarmingsupplies.register.Registration.ESSENCE_SOURCES.get(Essence.COLOR).get(), 1);
        notifyFluidChanged();
    }

    /** The sixteen dye outputs, for the loaders' item handlers. */
    public Container dyes() {
        return dyes;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ColorWorkstationMenu(id, inv, this);
    }

    /**
     * The outputs as a container: slot {@code i} holds {@link #DYES}{@code [i]}, as many as the
     * buffer pays for up to a stack. Taking from one spends the buffer; setting one (as a menu or
     * a rolled-back transaction does) moves the buffer by the difference. Nothing can be put in.
     */
    final class DyeOutputs implements Container {

        @Override
        public int getContainerSize() {
            return DYES.size();
        }

        @Override
        public boolean isEmpty() {
            return dyesAvailable() == 0;
        }

        private int count() {
            return Math.min(dyesAvailable(), DYES.get(0).getDefaultMaxStackSize());
        }

        @Override
        public ItemStack getItem(int slot) {
            int count = count();
            return count == 0 || slot < 0 || slot >= DYES.size() ? ItemStack.EMPTY : new ItemStack(DYES.get(slot), count);
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            int taken = Math.min(count, count());
            if (taken <= 0 || slot < 0 || slot >= DYES.size()) return ItemStack.EMPTY;
            setAmount(amount - (long) taken * MB_PER_DYE);
            return new ItemStack(DYES.get(slot), taken);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return removeItem(slot, count());
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot < 0 || slot >= DYES.size()) return;
            if (!stack.isEmpty() && !stack.is(DYES.get(slot))) return;
            setAmount(amount + (long) (stack.getCount() - count()) * MB_PER_DYE);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public void setChanged() {
            ColorWorkstationBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return Container.stillValidBlockEntity(ColorWorkstationBlockEntity.this, player);
        }

        @Override
        public void clearContent() {}
    }
}
