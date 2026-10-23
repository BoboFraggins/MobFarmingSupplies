package net.bobofraggins.mobfarmingsupplies.incubator;

import dev.architectury.fluid.FluidStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.TankTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * The XP Powered Incubator: a sample and four rows. Each row bakes its input into what its
 * {@link IncubatingRecipe} makes with the sample, drawing the recipe's experience bit by bit from
 * the incubator's tank and pausing while there isn't enough. The sample is never used up.
 *
 * <p>The tank is a tank's ({@link TankBlockEntity}, experience fluids only): pipes fill it, and
 * the incubator keeps it, as {@code tank_contents}, when broken, as it does the experience it was
 * crafted with.
 *
 * <p>The lid opens while anyone has the incubator's window open and shuts when the last of them
 * closes it, with a chest's sounds and timing.
 */
public class IncubatorBlockEntity extends TankBlockEntity {

    /** Slot 0 holds the sample; then four rows of an input and an output. */
    public static final int DNA_SLOT = 0;
    public static final int ROWS = 4;
    public static final int FIRST_INPUT = 1;
    public static final int FIRST_OUTPUT = FIRST_INPUT + ROWS;
    public static final int SLOTS = FIRST_OUTPUT + ROWS;

    /** Row progress is synced to the window in thousandths. */
    public static final int PROGRESS_SCALE = 1000;

    /** How far the lid swings open, in degrees: a chest's. */
    static final float OPEN_ANGLE = 90f;
    /** Share of the swing made each tick: a chest's. */
    private static final float SWING_PER_TICK = 0.1f;

    private static final String TAG_OPEN = "Open";
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_PAID = "Paid";
    private static final String TAG_SHOWN = "Shown";

    /** In the shown bits: a sample (its test tube); then each row with an input or output (its nest's egg). */
    private static final int SHOWN_TUBE = 1;

    /** Finds rows' recipes, remembering the last one each lookup found. Made on first use, once the type is registered. */
    @Nullable private RecipeManager.CachedCheck<IncubatingInput, IncubatingRecipe> recipes;

    final SimpleContainer items = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            IncubatorBlockEntity.this.setChanged();
            IncubatorBlockEntity.this.syncShownIfChanged();
        }
    };

    /** Ticks each row has baked its input for. */
    private final int[] progress = new int[ROWS];
    /** mB of experience each row has drawn for its current bake. */
    private final int[] paid = new int[ROWS];
    /** Each row's bake time, from its recipe (0 when it has none), for showing progress. */
    private final int[] bakeTime = new int[ROWS];

    /** Each row's progress in thousandths, for the menu to sync to the client. */
    final ContainerData progressData = new ContainerData() {
        @Override
        public int get(int row) {
            return bakeTime[row] <= 0 ? 0 : progress[row] * PROGRESS_SCALE / bakeTime[row];
        }

        @Override
        public void set(int row, int value) {}

        @Override
        public int getCount() {
            return ROWS;
        }
    };

    /**
     * The tank's amount, for the window's gauge. Window data is sent as 16-bit numbers, so it goes
     * as two 15-bit halves.
     */
    final ContainerData tankData = new ContainerData() {
        @Override
        public int get(int index) {
            int mb = (int) Math.min(amount, Integer.MAX_VALUE);
            return index == 0 ? mb & 0x7FFF : mb >>> 15;
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 2;
        }
    };

    /** Server: who has the window open. The lid is open while anyone does. */
    private final Set<UUID> viewers = new HashSet<>();
    /** Synced to clients, not saved: nobody has the window open when a world loads. */
    private boolean open;
    /** What the renderer shows inside: {@link #SHOWN_TUBE}, then a bit per row (1 << (row + 1)). Synced. */
    private int shown;
    /** Client only: how open the lid is, 0 to 1, this tick and last. */
    private float openness, prevOpenness;

    public IncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("incubator"), pos, state);
    }

    // ── The tank ───────────────────────────────────────────────────────────────

    @Override
    public boolean canHold(FluidStack fluid) {
        return ExperiencePylonBlockEntity.isExperience(fluid);
    }

    /** As much as a basic tank. */
    @Override
    public long getCapacity() {
        return TankTier.BASIC.capacity();
    }

    // ── Ticking ────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, IncubatorBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;
        boolean changed = false;
        for (int row = 0; row < ROWS; row++) {
            changed |= be.bake(server, row);
        }
        // Not this.setChanged(): a tank's tells clients and capabilities every time, and this runs every tick.
        if (changed) setChanged(level, pos, state);
    }

    /** One tick of a row's bake. Returns whether anything changed. */
    private boolean bake(ServerLevel level, int row) {
        ItemStack input = items.getItem(FIRST_INPUT + row);
        ItemStack output = items.getItem(FIRST_OUTPUT + row);
        IncubatingInput in = new IncubatingInput(items.getItem(DNA_SLOT), input, worldPosition);
        Optional<RecipeHolder<IncubatingRecipe>> found = input.isEmpty() || in.sample().isEmpty()
                ? Optional.empty() : recipes().getRecipeFor(in, level);
        if (found.isEmpty() || !found.get().value().fits(in, level, output)) {
            boolean wasBaking = progress[row] != 0 || paid[row] != 0 || bakeTime[row] != 0;
            progress[row] = 0;
            paid[row] = 0;
            bakeTime[row] = 0;
            return wasBaking;
        }
        IncubatingRecipe recipe = found.get().value();
        if (bakeTime[row] != recipe.time()) {
            // A different recipe from the one baking: start over (experience already drawn is spent).
            bakeTime[row] = recipe.time();
            progress[row] = 0;
            paid[row] = 0;
        }
        // Draw the experience this tick's share of the bake needs; wait if the tank can't cover it.
        int owed = (int) ((long) recipe.experience() * (progress[row] + 1) / recipe.time()) - paid[row];
        if (owed > 0) {
            if (amount < owed) return false;
            amount -= owed;
            if (amount <= 0) {
                amount = 0;
                storedFluid = FluidStack.empty();
            }
            paid[row] += owed;
        }
        if (++progress[row] >= recipe.time()) {
            ItemStack made = recipe.make(in, level, level.getRandom(), output);
            if (!made.isEmpty()) {
                if (output.isEmpty()) items.setItem(FIRST_OUTPUT + row, made);
                else output.grow(made.getCount());
                items.removeItem(FIRST_INPUT + row, 1);
            }
            progress[row] = 0;
            paid[row] = 0;
        }
        return true;
    }

    private RecipeManager.CachedCheck<IncubatingInput, IncubatingRecipe> recipes() {
        if (recipes == null) {
            // In full: in a block entity, "Registration" is a type it inherits, not the mod's.
            recipes = RecipeManager.createCheck(
                    net.bobofraggins.mobfarmingsupplies.register.Registration.INCUBATING_TYPE.get());
        }
        return recipes;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, IncubatorBlockEntity be) {
        be.prevOpenness = be.openness;
        be.openness = Mth.clamp(be.openness + (be.open ? SWING_PER_TICK : -SWING_PER_TICK), 0f, 1f);
    }

    // ── What shows inside ──────────────────────────────────────────────────────

    /** The test tube shows while there's a sample. */
    boolean showsTestTube() {
        return (shown & SHOWN_TUBE) != 0;
    }

    /** A row's egg shows while there's anything in its input or output slot. */
    boolean showsEgg(int row) {
        return (shown & 1 << (row + 1)) != 0;
    }

    private int computeShown() {
        int bits = items.getItem(DNA_SLOT).isEmpty() ? 0 : SHOWN_TUBE;
        for (int row = 0; row < ROWS; row++) {
            if (!items.getItem(FIRST_INPUT + row).isEmpty() || !items.getItem(FIRST_OUTPUT + row).isEmpty()) {
                bits |= 1 << (row + 1);
            }
        }
        return bits;
    }

    /** Tells clients when the test tube or an egg comes or goes. */
    private void syncShownIfChanged() {
        if (level == null || level.isClientSide()) return;
        int now = computeShown();
        if (now == shown) return;
        shown = now;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // ── Filling, for shift-clicks and automation ──────────────────────────────

    /**
     * Puts as much of {@code stack} in as fits, the way shift-clicking and pipes do: inputs are
     * spread over the input slots, one at a time to whichever matching slot has the fewest (an
     * empty slot matches; different items never share), and a sample goes in the sample slot if
     * it's empty. Shrinks {@code stack} by what went in, unless {@code simulate}.
     *
     * @return how many went in
     */
    static int insert(Container items, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        if (IncubatorMenu.isSample(stack)) {
            if (!items.getItem(DNA_SLOT).isEmpty()) return 0;
            if (!simulate) {
                items.setItem(DNA_SLOT, stack.copyWithCount(1));
                stack.shrink(1);
            }
            return 1;
        }
        if (!IncubatorMenu.isInput(stack)) return 0;
        int max = Math.min(stack.getMaxStackSize(), items.getMaxStackSize());
        int[] counts = new int[ROWS];
        boolean[] matches = new boolean[ROWS];
        for (int row = 0; row < ROWS; row++) {
            ItemStack held = items.getItem(FIRST_INPUT + row);
            counts[row] = held.getCount();
            matches[row] = held.isEmpty() || ItemStack.isSameItemSameComponents(held, stack);
        }
        int[] added = new int[ROWS];
        int moved = 0;
        while (moved < stack.getCount()) {
            int fewest = -1;
            for (int row = 0; row < ROWS; row++) {
                if (matches[row] && counts[row] < max && (fewest < 0 || counts[row] < counts[fewest])) fewest = row;
            }
            if (fewest < 0) break;
            counts[fewest]++;
            added[fewest]++;
            moved++;
        }
        if (!simulate && moved > 0) {
            for (int row = 0; row < ROWS; row++) {
                if (added[row] == 0) continue;
                ItemStack held = items.getItem(FIRST_INPUT + row);
                if (held.isEmpty()) items.setItem(FIRST_INPUT + row, stack.copyWithCount(added[row]));
                else held.grow(added[row]);
            }
            stack.shrink(moved);
            items.setChanged();
        }
        return moved;
    }

    /** The slots, for the loaders' item handlers. */
    public SimpleContainer items() {
        return items;
    }

    /** Puts {@code stack} in as pipes do ({@link #insert(Container, ItemStack, boolean)}). */
    public int insert(ItemStack stack, boolean simulate) {
        return insert(items, stack, simulate);
    }

    // ── Lid ────────────────────────────────────────────────────────────────────

    /** A player opened the window: open the lid if they're the first. */
    void startViewing(Player player) {
        if (level == null || level.isClientSide()) return;
        if (viewers.add(player.getUUID()) && viewers.size() == 1) setOpen(true);
    }

    /** A player closed the window: shut the lid if they were the last. */
    void stopViewing(Player player) {
        if (level == null || level.isClientSide()) return;
        if (viewers.remove(player.getUUID()) && viewers.isEmpty()) setOpen(false);
    }

    private void setOpen(boolean open) {
        this.open = open;
        // As a chest does: open when the lid starts to rise, close as it starts to fall.
        level.playSound(null, worldPosition, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE,
                SoundSource.BLOCKS, 0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /** The lid's angle in degrees, eased like a chest's, between ticks. */
    float lidAngle(float partialTick) {
        float t = 1f - Mth.lerp(partialTick, prevOpenness, openness);
        return (1f - t * t * t) * OPEN_ANGLE;
    }

    // ── Menu ───────────────────────────────────────────────────────────────────

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new IncubatorMenu(id, inv, this);
    }

    /** Drops what's in the slots when the block goes. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) Containers.dropContents(level, pos, items);
        super.preRemoveSideEffects(pos, state);
    }

    // ── Saving and syncing ─────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        List<ItemStack> stacks = new ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) stacks.add(items.getItem(i));
        output.store(TAG_ITEMS, ItemStack.OPTIONAL_CODEC.listOf(), stacks);
        output.putIntArray(TAG_PROGRESS, progress.clone());
        output.putIntArray(TAG_PAID, paid.clone());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read(TAG_ITEMS, ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < Math.min(list.size(), SLOTS); i++) items.setItem(i, list.get(i));
        });
        input.getIntArray(TAG_PROGRESS).ifPresent(saved ->
                System.arraycopy(saved, 0, progress, 0, Math.min(saved.length, ROWS)));
        input.getIntArray(TAG_PAID).ifPresent(saved ->
                System.arraycopy(saved, 0, paid, 0, Math.min(saved.length, ROWS)));
        // Only update packets carry these; on the server they're worked out from the slots.
        open = input.getBooleanOr(TAG_OPEN, false);
        shown = input.getIntOr(TAG_SHOWN, computeShown());
    }

    /**
     * What clients need: whether the lid is open, and what shows inside. They don't need the slots
     * or the tank; the window sends those.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_OPEN, open);
        tag.putInt(TAG_SHOWN, shown);
        return tag;
    }
}
