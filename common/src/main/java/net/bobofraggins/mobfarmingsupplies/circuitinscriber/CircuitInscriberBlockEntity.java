package net.bobofraggins.mobfarmingsupplies.circuitinscriber;

import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.energy.EnergyBuffer;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * The Circuit Inscriber: two inputs, one output and an energy buffer. While a recipe matches,
 * there's room for its result and enough energy, it spends an even share of the recipe's
 * {@code energy} each tick until {@code time} ticks have passed, then makes the result. Without
 * enough energy for a tick it waits. Hoppers feed the first slot from above,
 * the second from the sides, and take the output from below.
 */
public class CircuitInscriberBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, EnergyBuffer {

    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int OUTPUT = 2;
    public static final long CAPACITY = 512_000;
    /** Ticks without progress before the arms go back up, so a short wait for energy doesn't flap them. */
    private static final int IDLE_TICKS = 10;
    /** Client: how fast the arms move, as a fraction of their travel per tick. */
    private static final float ARM_SPEED = 0.1f;

    private static final int[] TOP = {FIRST};
    private static final int[] SIDES = {SECOND};
    private static final int[] BOTTOM = {OUTPUT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private long energy;
    private int progress;
    private int time;
    private int idleTicks = IDLE_TICKS;
    /** Whether it's working, as far as the arms and lasers are concerned. Synced to the client. */
    private boolean crafting;
    @Nullable private RecipeHolder<CircuitInscribingRecipe> recipe;
    private boolean recipeChecked;
    @Nullable private Object energyHandler;

    /** Client: the item drawn lying on the work surface. */
    public ItemStack displayItem = ItemStack.EMPTY;
    /** Client: how far the arms have curled, 0 (up) to 1 (curled over the work surface). */
    public float armProgress;
    public float prevArmProgress;
    /** Client: ticks since the block loaded, for the lasers' sweep. */
    public int clientTicks;

    /**
     * Progress, time and energy, for the screen. Menus sync these as 16-bit numbers, so the
     * energy goes as its low and high halves.
     */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> time;
                case 2 -> (int) (energy & 0xFFFF);
                default -> (int) ((energy >>> 16) & 0xFFFF);
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 4;
        }
    };

    public CircuitInscriberBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("circuit_inscriber"), pos, state);
    }

    public boolean isCrafting() {
        return crafting;
    }

    // ── Ticking ───────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, CircuitInscriberBlockEntity be) {
        if (!be.recipeChecked) {
            be.recipe = be.findRecipe((ServerLevel) level);
            be.recipeChecked = true;
        }
        boolean worked = false;
        CircuitInscribingRecipe current = be.recipe == null ? null : be.recipe.value();
        if (current != null && be.hasRoomFor(current)) {
            be.time = current.time();
            long cost = current.energyForTick(be.progress);
            if (be.energy >= cost) {
                be.energy -= cost;
                worked = true;
                if (++be.progress >= current.time()) {
                    be.craft(current);
                    be.progress = 0;
                }
                be.setChanged();
            }
        } else if (be.progress != 0 || be.time != 0) {
            be.progress = 0;
            be.time = 0;
            be.setChanged();
        }

        be.idleTicks = worked ? 0 : Math.min(IDLE_TICKS, be.idleTicks + 1);
        boolean crafting = be.idleTicks < IDLE_TICKS;
        if (crafting != be.crafting) {
            be.crafting = crafting;
            be.sync();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CircuitInscriberBlockEntity be) {
        be.clientTicks++;
        be.prevArmProgress = be.armProgress;
        float target = be.crafting ? 1f : 0f;
        if (be.armProgress < target) be.armProgress = Math.min(target, be.armProgress + ARM_SPEED);
        else if (be.armProgress > target) be.armProgress = Math.max(target, be.armProgress - ARM_SPEED);
    }

    private RecipeHolder<CircuitInscribingRecipe> findRecipe(ServerLevel level) {
        if (items.get(FIRST).isEmpty()) return null;
        // Fully qualified: in a block entity, "Registration" is shadowed by an inherited interface.
        Optional<RecipeHolder<CircuitInscribingRecipe>> match = level.recipeAccess().getRecipeFor(
                net.bobofraggins.mobfarmingsupplies.register.Registration.CIRCUIT_INSCRIBING_TYPE.get(),
                new CircuitInscribingRecipe.Input(items.get(FIRST), items.get(SECOND)), level);
        return match.orElse(null);
    }

    private boolean hasRoomFor(CircuitInscribingRecipe recipe) {
        ItemStack result = recipe.assemble(new CircuitInscribingRecipe.Input(items.get(FIRST), items.get(SECOND)));
        ItemStack output = items.get(OUTPUT);
        if (output.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void craft(CircuitInscribingRecipe recipe) {
        ItemStack result = recipe.assemble(new CircuitInscribingRecipe.Input(items.get(FIRST), items.get(SECOND)));
        ItemStack output = items.get(OUTPUT);
        if (output.isEmpty()) items.set(OUTPUT, result);
        else output.grow(result.getCount());
        items.get(FIRST).shrink(1);
        if (recipe.usesSecond()) items.get(SECOND).shrink(1);
        contentsChanged();
    }

    /** Re-checks the recipe and, if the item on the work surface changed, tells the client. */
    private void contentsChanged() {
        recipeChecked = false;
        setChanged();
        if (level != null && !level.isClientSide()) sync();
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /** What lies on the work surface: the first input, or the output if that's empty. */
    private ItemStack surfaceItem() {
        return items.get(FIRST).isEmpty() ? items.get(OUTPUT) : items.get(FIRST);
    }

    // ── Container ─────────────────────────────────────────────────────────────────

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) contentsChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty()) contentsChanged();
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        contentsChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        contentsChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != OUTPUT;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? TOP : side == Direction.DOWN ? BOTTOM : SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot != OUTPUT;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == OUTPUT;
    }

    // ── Energy ────────────────────────────────────────────────────────────────────

    @Override
    public long energy() {
        return energy;
    }

    @Override
    public long capacity() {
        return CAPACITY;
    }

    @Override
    public void setEnergy(long energy) {
        if (energy != this.energy) {
            this.energy = energy;
            setChanged();
        }
    }

    @Override
    public long maxInsert() {
        return CAPACITY;
    }

    /** Neighbours can only feed a machine. */
    @Override
    public long maxExtract() {
        return 0;
    }

    @Override
    @Nullable
    public Object energyHandler() {
        return energyHandler;
    }

    @Override
    public void setEnergyHandler(Object handler) {
        energyHandler = handler;
    }

    // ── Menu ──────────────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.circuit_inscriber");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new CircuitInscriberMenu(syncId, inventory, this, data);
    }

    // ── Saving and syncing ────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putLong("Energy", energy);
        output.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        energy = input.getLongOr("Energy", 0L);
        progress = input.getIntOr("Progress", 0);
        crafting = input.getBooleanOr("Crafting", false);
        displayItem = input.read("Display", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        recipeChecked = false;
    }

    /** The client only needs whether it's working and what's on the work surface. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Crafting", crafting);
        ItemStack shown = surfaceItem();
        if (!shown.isEmpty()) {
            ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                    shown.copyWithCount(1)).ifSuccess(t -> tag.put("Display", t));
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
