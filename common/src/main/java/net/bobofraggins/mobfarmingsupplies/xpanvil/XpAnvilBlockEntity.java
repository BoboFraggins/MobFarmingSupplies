package net.bobofraggins.mobfarmingsupplies.xpanvil;

import dev.architectury.fluid.FluidStack;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The XP Powered Anvil: whatever's in its input slot is repaired {@value #REPAIR_PER_TICK} points
 * a tick, at a millibucket of experience a point, from its tank (experience fluids only, up to
 * {@value #CAPACITY} mB; pipes fill it, and it keeps it when broken). Once whole, it moves to the
 * output slot. It waits while the tank is empty or the output slot is full.
 */
public class XpAnvilBlockEntity extends TankBlockEntity {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    /** 16 buckets. */
    public static final long CAPACITY = 16_000;
    /** Durability repaired per tick; each point costs {@value #MB_PER_POINT} mB. */
    public static final int REPAIR_PER_TICK = 10;
    public static final int MB_PER_POINT = 1;
    /** Repair progress is synced to the window in thousandths. */
    public static final int PROGRESS_SCALE = 1000;

    private static final String TAG_ITEMS = "Items";

    final SimpleContainer items = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            XpAnvilBlockEntity.this.setChanged();
        }
    };

    /** How whole the input is (thousandths), and the tank's amount in two 15-bit halves, for the window. */
    final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> wholeness(items.getItem(INPUT));
                case 1 -> (int) Math.min(amount, Integer.MAX_VALUE) & 0x7FFF;
                default -> (int) Math.min(amount, Integer.MAX_VALUE) >>> 15;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 3;
        }
    };

    public XpAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("anvil"), pos, state);
    }

    @Override
    public boolean canHold(FluidStack fluid) {
        return ExperiencePylonBlockEntity.isExperience(fluid);
    }

    @Override
    public long getCapacity() {
        return CAPACITY;
    }

    /** How much of its durability an item has, in thousandths (an empty slot is 0). */
    private static int wholeness(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return 0;
        return (stack.getMaxDamage() - stack.getDamageValue()) * PROGRESS_SCALE / stack.getMaxDamage();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, XpAnvilBlockEntity be) {
        ItemStack input = be.items.getItem(INPUT);
        if (input.isEmpty()) return;
        boolean changed = false;
        if (input.isDamaged()) {
            int points = (int) Math.min(Math.min(REPAIR_PER_TICK, input.getDamageValue()), be.amount / MB_PER_POINT);
            if (points <= 0) return;
            input.setDamageValue(input.getDamageValue() - points);
            be.amount -= (long) points * MB_PER_POINT;
            if (be.amount <= 0) {
                be.amount = 0;
                be.storedFluid = FluidStack.empty();
            }
            changed = true;
        }
        if (!input.isDamaged() && be.items.getItem(OUTPUT).isEmpty()) {
            be.items.setItem(OUTPUT, input);
            be.items.setItem(INPUT, ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6f,
                    level.getRandom().nextFloat() * 0.1f + 0.9f);
            return; // setItem has saved it
        }
        // Not this.setChanged(): a tank's tells clients and capabilities every time, and this runs every tick.
        if (changed) setChanged(level, pos, state);
    }

    /** The input and output slots, for the loaders' item handlers. */
    public SimpleContainer items() {
        return items;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new XpAnvilMenu(id, inv, this);
    }

    /** Set as the block starts to fall: the falling block carries the slots, so they aren't dropped here. */
    private boolean falling;

    void startFalling() {
        falling = true;
    }

    /** Drops what's in the slots when the block goes (but not when it's falling: it takes them along). */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide() && !falling) Containers.dropContents(level, pos, items);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        List<ItemStack> stacks = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) stacks.add(items.getItem(i));
        output.store(TAG_ITEMS, ItemStack.OPTIONAL_CODEC.listOf(), stacks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read(TAG_ITEMS, ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < Math.min(list.size(), 2); i++) items.setItem(i, list.get(i));
        });
    }

    /** Clients need nothing from it: the window sends the slots and the tank. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return new CompoundTag();
    }
}
