package net.bobofraggins.mobfarmingsupplies.disenchanter;

import dev.architectury.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * The XP Powered Disenchanter: every {@value #TIME} ticks, drawing {@value #EXPERIENCE} mB of
 * experience bit by bit (and pausing while the tank is short), it pulls a random enchantment off
 * the item in the top slot onto a book from the book slot, as an enchanted book in the output
 * slot. An item with no enchantments left (or an enchanted book down to its last one) moves to
 * the output slot. It waits while there's no book or the output slot is full.
 *
 * <p>Its tank is a tank's ({@link TankBlockEntity}, experience fluids only, up to {@value #CAPACITY}
 * mB): pipes fill it, and it keeps it, as {@code tank_contents}, when broken.
 */
public class DisenchanterBlockEntity extends TankBlockEntity {

    public static final int ITEM = 0;
    public static final int BOOK = 1;
    public static final int OUTPUT = 2;
    public static final int SLOTS = 3;

    /** 10 seconds per enchantment. */
    public static final int TIME = 200;
    /** mB of experience per enchantment. */
    public static final int EXPERIENCE = 250;
    /** 16 buckets. */
    public static final long CAPACITY = 16_000;
    /** Progress is synced to the window in thousandths. */
    public static final int PROGRESS_SCALE = 1000;

    private static final String TAG_ITEMS = "Items";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_PAID = "Paid";

    final SimpleContainer items = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            DisenchanterBlockEntity.this.setChanged();
        }
    };

    private int progress;
    private int paid;
    /** Whether it's pulling an enchantment off right now (for the runes). Synced to clients, not saved. */
    private boolean working;
    private static final String TAG_WORKING = "Working";

    /** Progress in thousandths, and the tank's amount in two 15-bit halves, for the window. */
    final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            int mb = (int) Math.min(amount, Integer.MAX_VALUE);
            return switch (index) {
                case 0 -> progress * PROGRESS_SCALE / TIME;
                case 1 -> mb & 0x7FFF;
                default -> mb >>> 15;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 3;
        }
    };

    /** Client only: a vanilla enchanting table's book animation, which the renderer draws. */
    @Nullable private EnchantingTableBlockEntity book;

    public DisenchanterBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("disenchanter"), pos, state);
    }

    @Override
    public boolean canHold(FluidStack fluid) {
        return ExperiencePylonBlockEntity.isExperience(fluid);
    }

    @Override
    public long getCapacity() {
        return CAPACITY;
    }

    // ── What it takes ─────────────────────────────────────────────────────────

    /** What the top slot takes: anything enchanted or enchantable, and enchanted books. */
    public static boolean isDisenchantable(ItemStack stack) {
        return stack.isEnchanted() || stack.isEnchantable() || !enchantments(stack).isEmpty();
    }

    /** What the book slot takes: plain books. */
    public static boolean isBook(ItemStack stack) {
        return stack.is(Items.BOOK);
    }

    /** The item's enchantments, or an enchanted book's stored ones. */
    private static ItemEnchantments enchantments(ItemStack stack) {
        return stack.getOrDefault(stack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS
                : DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    /** Whether there's nothing left worth pulling: no enchantments, or an enchanted book's last one. */
    private static boolean isDone(ItemStack stack) {
        int left = enchantments(stack).size();
        return left == 0 || (stack.is(Items.ENCHANTED_BOOK) && left == 1);
    }

    // ── Ticking ────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, DisenchanterBlockEntity be) {
        int before = be.progress;
        be.work(level, pos, state);
        // Working while the bake moved on this tick (a finished one resets progress to 0, so counts too).
        be.setWorking(be.progress != before);
    }

    /** Tells clients when it starts or stops working, so the runes come and go. */
    private void setWorking(boolean now) {
        if (now == working || level == null) return;
        working = now;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /** Whether the runes should be flying (client side, from the last update). */
    public boolean isWorking() {
        return working;
    }

    private void work(Level level, BlockPos pos, BlockState state) {
        DisenchanterBlockEntity be = this;
        ItemStack item = be.items.getItem(ITEM);
        if (item.isEmpty()) {
            if (be.progress != 0 || be.paid != 0) {
                be.progress = 0;
                be.paid = 0;
                setChanged(level, pos, state);
            }
            return;
        }
        ItemStack output = be.items.getItem(OUTPUT);
        if (isDone(item)) {
            if (!output.isEmpty()) return;
            be.items.setItem(OUTPUT, item);
            be.items.setItem(ITEM, ItemStack.EMPTY);
            be.progress = 0;
            be.paid = 0;
            return;
        }
        if (!isBook(be.items.getItem(BOOK)) || !output.isEmpty()) return;
        // Draw this tick's share of the experience; wait if the tank can't cover it.
        int owed = (int) ((long) EXPERIENCE * (be.progress + 1) / TIME) - be.paid;
        if (owed > 0) {
            if (be.amount < owed) return;
            be.amount -= owed;
            if (be.amount <= 0) {
                be.amount = 0;
                be.storedFluid = FluidStack.empty();
            }
            be.paid += owed;
        }
        if (++be.progress >= TIME) {
            be.progress = 0;
            be.paid = 0;
            be.items.setItem(OUTPUT, be.pullEnchantment(level, item));
            be.items.removeItem(BOOK, 1);
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f,
                    level.getRandom().nextFloat() * 0.1f + 0.9f);
            return; // the slots have saved it
        }
        // Not this.setChanged(): a tank's tells clients and capabilities every time, and this runs every tick.
        setChanged(level, pos, state);
    }

    /** Takes a random enchantment off {@code item} and returns it on an enchanted book. */
    private ItemStack pullEnchantment(Level level, ItemStack item) {
        List<Object2IntMap.Entry<Holder<Enchantment>>> all = new ArrayList<>(enchantments(item).entrySet());
        Object2IntMap.Entry<Holder<Enchantment>> chosen = all.get(level.getRandom().nextInt(all.size()));
        Holder<Enchantment> enchantment = chosen.getKey();
        EnchantmentHelper.updateEnchantments(item, enchantments -> enchantments.removeIf(enchantment::equals));
        ItemEnchantments.Mutable onBook = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        onBook.set(enchantment, chosen.getIntValue());
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.set(DataComponents.STORED_ENCHANTMENTS, onBook.toImmutable());
        return book;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DisenchanterBlockEntity be) {
        if (be.book == null) {
            be.book = new EnchantingTableBlockEntity(pos, Blocks.ENCHANTING_TABLE.defaultBlockState());
            be.book.setLevel(level);
        }
        EnchantingTableBlockEntity.bookAnimationTick(level, pos, state, be.book);
    }

    /** The floating book's animation, for the renderer; null until the first client tick. */
    @Nullable
    EnchantingTableBlockEntity book() {
        return book;
    }

    // ── Menu, saving ───────────────────────────────────────────────────────────

    /** The slots, for the loaders' item handlers. */
    public SimpleContainer items() {
        return items;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new DisenchanterMenu(id, inv, this);
    }

    /** Drops what's in the slots when the block goes. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) Containers.dropContents(level, pos, items);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        List<ItemStack> stacks = new ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) stacks.add(items.getItem(i));
        output.store(TAG_ITEMS, ItemStack.OPTIONAL_CODEC.listOf(), stacks);
        output.putInt(TAG_PROGRESS, progress);
        output.putInt(TAG_PAID, paid);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read(TAG_ITEMS, ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < Math.min(list.size(), SLOTS); i++) items.setItem(i, list.get(i));
        });
        progress = input.getIntOr(TAG_PROGRESS, 0);
        paid = input.getIntOr(TAG_PAID, 0);
        working = input.getBooleanOr(TAG_WORKING, false); // only update packets carry it
    }

    /** Clients only need to know whether it's working (for the runes); the window sends the rest. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_WORKING, working);
        return tag;
    }
}
