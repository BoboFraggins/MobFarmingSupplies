package net.bobofraggins.mobfarmingsupplies.omnihopper;

import net.bobofraggins.mobfarmingsupplies.shared.sides.SideLayout;
import net.bobofraggins.mobfarmingsupplies.shared.sides.SideOriented;
import net.minecraft.core.FrontAndTop;
import org.jetbrains.annotations.Nullable;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SorterFilters;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Omnidirectional Hopper: moves items, fluids, energy and (on NeoForge, with Mekanism) chemicals
 * from its INPUT sides to its OUTPUT sides, split evenly across the outputs. Like the Logistic
 * Sorter it holds nothing itself — a resource only moves when a destination accepts it — so it
 * can't jam or lose anything.
 *
 * <p>Up to nine Item Filters (combined with AND or OR) limit which items move; with none
 * installed every item moves. Filters don't apply to fluids, energy or chemicals.
 *
 * <p>Items move every {@link MFSConfig#getHopperTransferInterval()} ticks; fluids, energy and
 * chemicals move every tick. Rates are per INPUT side and configurable.
 */
public class OmniHopperBlockEntity extends BlockEntity implements SideOriented, MenuProvider, HopperNode, HopperConfigurable {

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


    public static final int FILTER_SLOTS = 9;

    private final HopperSide[] sides = new HopperSide[6]; // indexed by Direction#get3DDataValue
    private boolean andMode = true;
    private final SimpleContainer filters = new SimpleContainer(FILTER_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            OmniHopperBlockEntity.this.onConfigChanged();
        }
    };

    private int tickCounter = 0;
    private boolean stateDirty = true;
    private int rotation = 0;          // round-robin offset for which output gets an uneven split's extras
    private boolean routing = false;   // re-entrancy guard for pushed-in resources

    public OmniHopperBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("omnidirectional_hopper"), pos, state);
        Arrays.fill(sides, HopperSide.NONE);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, OmniHopperBlockEntity be) {
        if (be.stateDirty) be.syncBlockState();
        if (!be.isActive()) return;
        boolean itemTick = ++be.tickCounter % Math.max(1, MFSConfig.getHopperTransferInterval()) == 0;
        OmniHopperPlatform.transfer(be, itemTick);
    }

    // ── Configuration ─────────────────────────────────────────────────────────────

    @Override
    public HopperSide getSide(Direction dir) { return sides[dir.get3DDataValue()]; }

    @Override
    public boolean isAndMode() { return andMode; }

    public SimpleContainer getFilters() { return filters; }

    @Override
    public int packedSides() {
        int packed = 0;
        for (int i = 0; i < 6; i++) packed |= sides[i].ordinal() << (2 * i);
        return packed;
    }

    public static HopperSide unpackSide(int packed, Direction dir) {
        return HopperSide.byOrdinal((packed >> (2 * dir.get3DDataValue())) & 3);
    }

    @Override
    public void setConfig(int packedSides, boolean andMode) {
        for (Direction d : Direction.values()) sides[d.get3DDataValue()] = unpackSide(packedSides, d);
        this.andMode = andMode;
        onConfigChanged();
    }

    @Override
    public boolean isActive() {
        boolean in = false, out = false;
        for (HopperSide m : sides) {
            in |= m == HopperSide.INPUT;
            out |= m == HopperSide.OUTPUT;
        }
        return in && out;
    }

    private void onConfigChanged() {
        setChanged();
        stateDirty = true;
        syncBlockState();
        if (level != null && !level.isClientSide()) {
            OmniHopperPlatform.invalidateCapabilities(level, worldPosition);
        }
    }

    /** Mirrors side modes into the block state (server only). */
    private void syncBlockState() {
        if (level == null || level.isClientSide()) return;
        stateDirty = false;
        BlockState current = getBlockState();
        if (!(current.getBlock() instanceof OmniHopperBlock)) return;
        BlockState updated = current;
        for (Direction d : Direction.values()) {
            updated = updated.setValue(OmniHopperBlock.SIDES.get(d), getSide(d));
        }
        if (!updated.equals(current)) {
            level.setBlock(worldPosition, updated, 2);
        } else {
            level.sendBlockUpdated(worldPosition, current, current, 2); // AND/OR or filter-only change
        }
    }

    // ── Routing ───────────────────────────────────────────────────────────────────

    /** Whether an item may move: with no filters installed, every item may. */
    @Override
    public boolean allowsItem(ItemStack stack) {
        if (!SorterFilters.hasAnyFilter(filters)) return true;
        return SorterFilters.matches(filters, andMode, stack, level != null ? level.registryAccess() : null);
    }

    /**
     * The OUTPUT sides in round-robin order. Resources are split evenly across them
     * ({@link net.bobofraggins.mobfarmingsupplies.logisticsorter.EvenSplit}); the rotation decides
     * which outputs get the remainder of an uneven split, so over time every output gets its turn.
     */
    public List<Direction> outputs() {
        List<Direction> found = new ArrayList<>();
        for (Direction d : Direction.values()) if (getSide(d) == HopperSide.OUTPUT) found.add(d);
        if (found.size() > 1) Collections.rotate(found, -Math.floorMod(rotation++, found.size()));
        return found;
    }

    @Override
    public List<HopperOutput> outputTargets() {
        List<HopperOutput> targets = new ArrayList<>();
        for (Direction d : outputs()) targets.add(new HopperOutput(level, worldPosition, d));
        return targets;
    }

    @Override
    public boolean beginRouting() {
        if (routing) return false;
        routing = true;
        return true;
    }

    @Override
    public void endRouting() { routing = false; }

    // ── MenuProvider ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.omnidirectional_hopper");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new OmniHopperMenu(syncId, inv, this);
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
