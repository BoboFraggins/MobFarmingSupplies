package net.bobofraggins.mobfarmingsupplies.logisticsorter;

import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.shared.sides.SideLayout;
import net.bobofraggins.mobfarmingsupplies.shared.sides.SideOriented;
import net.minecraft.core.FrontAndTop;
import org.jetbrains.annotations.Nullable;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Logistic Sorter: moves items from its INPUT sides to a MATCH side if they pass its Item
 * Filters (combined with AND or OR), otherwise to a NO_MATCH side. It holds no item buffer —
 * an item only moves when a destination accepts it — so it can't jam or lose items.
 *
 * <p>Side modes, the AND/OR setting, and the nine filters are saved with the block and carried
 * on the item when broken (see {@link LogisticSorterBlock#getDrops}).
 */
public class LogisticSorterBlockEntity extends BlockEntity implements SideOriented, MenuProvider {

    /** How it was placed, for its side grid (null = placed before orientations existed; see {@link SideLayout}). */
    @Nullable private FrontAndTop sideOrientation;

    @Override
    @Nullable
    public FrontAndTop getSideOrientation() { return sideOrientation; }

    @Override
    public void setSideOrientation(FrontAndTop orientation) {
        sideOrientation = orientation;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }


    public static final int FILTER_SLOTS  = 9;

    private final SideMode[] sides = new SideMode[6]; // indexed by Direction#get3DDataValue
    private boolean andMode = true;
    private final SimpleContainer filters = new SimpleContainer(FILTER_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            LogisticSorterBlockEntity.this.onConfigChanged();
        }
    };

    private int tickCounter = 0;
    private boolean stateDirty = true;
    private final int[] rotation = new int[2]; // round-robin offsets for MATCH / NO_MATCH outputs
    private boolean routing = false;           // re-entrancy guard for pushed-in items

    public LogisticSorterBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("logistic_sorter"), pos, state);
        java.util.Arrays.fill(sides, SideMode.NONE);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, LogisticSorterBlockEntity be) {
        if (be.stateDirty) be.syncBlockState();
        if (++be.tickCounter % Math.max(1, MFSConfig.getHopperTransferInterval()) == 0 && be.isActive()) {
            LogisticSorterPlatform.pullPhase(be, level, pos);
        }
    }

    // ── Configuration ─────────────────────────────────────────────────────────────

    public SideMode getSide(Direction dir) { return sides[dir.get3DDataValue()]; }

    public boolean isAndMode() { return andMode; }

    public SimpleContainer getFilters() { return filters; }

    /** Packs the six side modes, 2 bits each, indexed by {@link Direction#get3DDataValue}. */
    public int packedSides() {
        int packed = 0;
        for (int i = 0; i < 6; i++) packed |= sides[i].ordinal() << (2 * i);
        return packed;
    }

    public static SideMode unpackSide(int packed, Direction dir) {
        return SideMode.byOrdinal((packed >> (2 * dir.get3DDataValue())) & 3);
    }

    public void setConfig(int packedSides, boolean andMode) {
        for (Direction d : Direction.values()) sides[d.get3DDataValue()] = unpackSide(packedSides, d);
        this.andMode = andMode;
        onConfigChanged();
    }

    /** Configured and ready: has a filter, an INPUT side, and an output side. */
    public boolean isActive() {
        boolean in = false, out = false;
        for (SideMode m : sides) {
            in |= m == SideMode.INPUT;
            out |= m.isOutput();
        }
        return in && out && SorterFilters.hasAnyFilter(filters);
    }

    private void onConfigChanged() {
        setChanged();
        stateDirty = true;
        syncBlockState();
        if (level != null && !level.isClientSide()) {
            LogisticSorterPlatform.invalidateCapabilities(level, worldPosition);
        }
    }

    /** Mirrors side modes and activity into the block state (server only). */
    private void syncBlockState() {
        if (level == null || level.isClientSide()) return;
        stateDirty = false;
        BlockState current = getBlockState();
        if (!(current.getBlock() instanceof LogisticSorterBlock)) return;
        BlockState updated = current.setValue(LogisticSorterBlock.ACTIVE, isActive());
        for (Direction d : Direction.values()) {
            updated = updated.setValue(LogisticSorterBlock.SIDES.get(d), getSide(d));
        }
        if (!updated.equals(current)) {
            level.setBlock(worldPosition, updated, 2);
        } else {
            level.sendBlockUpdated(worldPosition, current, current, 2); // AND/OR or filter-only change
        }
    }

    // ── Routing ───────────────────────────────────────────────────────────────────

    public boolean matches(ItemStack stack) {
        return SorterFilters.matches(filters, andMode, stack, level != null ? level.registryAccess() : null);
    }

    /**
     * The output sides an item should go to, in round-robin order. Items are split evenly across
     * them ({@link EvenSplit}); the rotation decides which outputs get the remainder of an uneven
     * split, so over time every output gets its turn at the extras.
     */
    public List<Direction> outputsFor(ItemStack stack) {
        return outputs(matches(stack));
    }

    public List<Direction> outputs(boolean matching) {
        SideMode want = matching ? SideMode.MATCH : SideMode.NO_MATCH;
        List<Direction> found = new ArrayList<>();
        for (Direction d : Direction.values()) if (getSide(d) == want) found.add(d);
        if (found.size() > 1) {
            int r = Math.floorMod(rotation[matching ? 0 : 1]++, found.size());
            java.util.Collections.rotate(found, -r);
        }
        return found;
    }

    /** Guards against infinite loops when sorters feed each other. Returns false if already routing. */
    public boolean beginRouting() {
        if (routing) return false;
        routing = true;
        return true;
    }

    public void endRouting() { routing = false; }

    // ── MenuProvider ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.logistic_sorter");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new LogisticSorterMenu(syncId, inv, this);
    }

    // ── NBT ──────────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        SideLayout.save(output, sideOrientation);
        output.putInt("Sides", packedSides());
        output.putBoolean("AndMode", andMode);
        List<ItemStack> stacks = new ArrayList<>(FILTER_SLOTS);
        for (int i = 0; i < FILTER_SLOTS; i++) stacks.add(filters.getItem(i));
        output.store("Filters", ItemStack.OPTIONAL_CODEC.listOf(), stacks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        sideOrientation = SideLayout.load(input);
        int packed = input.getIntOr("Sides", 0);
        for (Direction d : Direction.values()) sides[d.get3DDataValue()] = unpackSide(packed, d);
        andMode = input.getBooleanOr("AndMode", true);
        input.read("Filters", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(loaded -> {
            for (int i = 0; i < FILTER_SLOTS; i++) {
                filters.getItems().set(i, i < loaded.size() ? loaded.get(i) : ItemStack.EMPTY);
            }
        });
        stateDirty = true; // block state is re-synced on the next server tick
    }

    // ── Client sync ───────────────────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
