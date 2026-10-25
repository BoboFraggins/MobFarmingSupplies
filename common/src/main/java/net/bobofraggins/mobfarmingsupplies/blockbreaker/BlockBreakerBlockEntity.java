package net.bobofraggins.mobfarmingsupplies.blockbreaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SorterFilters;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperConfigurable;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlock;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.bobofraggins.mobfarmingsupplies.shared.sides.SideLayout;
import net.bobofraggins.mobfarmingsupplies.shared.sides.SideOriented;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.bobofraggins.mobfarmingsupplies.shared.nbt.ValueInput;
import net.bobofraggins.mobfarmingsupplies.shared.nbt.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Block Breaker: breaks the block in front of each INPUT side, whatever its tier and whichever tool
 * it needs (see {@link #tool}; with Silk Touch when that's on), and pushes what it drops out of its
 * OUTPUT sides, split across them in turn. It only breaks a block when every drop has somewhere to
 * go, so nothing is ever spilled or lost; a block that would drop nothing is left alone, as are
 * containers (breaking one would spill its contents), liquids and unbreakable blocks.
 *
 * <p>Up to nine Item Filters (combined with AND or OR) choose which blocks it breaks, matched
 * against the block as an item; with none installed it breaks everything.
 *
 * <p>Side modes, AND/OR and the filters work as the Omnidirectional Hopper's, so it shares that
 * hopper's side grid, AND/OR pane and config packet ({@link HopperConfigurable}).
 */
public class BlockBreakerBlockEntity extends BlockEntity implements SideOriented, MenuProvider, HopperConfigurable {

    public static final int FILTER_SLOTS = OmniHopperBlockEntity.FILTER_SLOTS;

    /** Ticks between breaks: a quarter of a second. */
    public static final int BREAK_TICKS = 5;

    private final HopperSide[] sides = new HopperSide[6]; // indexed by Direction#get3DDataValue
    private boolean andMode = true;
    private boolean silkTouch = false;
    private final SimpleContainer filters = new SimpleContainer(FILTER_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            BlockBreakerBlockEntity.this.onConfigChanged();
        }
    };

    /** How it was placed, for its side grid (null = placed before orientations existed; see {@link SideLayout}). */
    @Nullable private FrontAndTop sideOrientation;
    private boolean stateDirty = true;
    private int cooldown;
    private int rotation; // which OUTPUT side is tried first, turn about

    public BlockBreakerBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("block_breaker"), pos, state);
        Arrays.fill(sides, HopperSide.NONE);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockBreakerBlockEntity be) {
        if (be.stateDirty) be.syncBlockState();
        if (--be.cooldown > 0) return;
        be.cooldown = BREAK_TICKS;
        if (!(level instanceof ServerLevel server)) return;
        List<Direction> outputs = be.outputs();
        if (outputs.isEmpty()) return;
        for (Direction side : Direction.values()) {
            if (be.getSide(side) == HopperSide.INPUT) be.tryBreak(server, pos.relative(side), outputs);
        }
    }

    /** Breaks the block at {@code target} if it may and everything it drops fits through {@code outputs}. */
    private void tryBreak(ServerLevel level, BlockPos target, List<Direction> outputs) {
        BlockState state = level.getBlockState(target);
        if (state.isAir() || state.getDestroySpeed(level, target) < 0) return;
        if (!state.getFluidState().isEmpty() && state.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock) return;
        BlockEntity targetBe = level.getBlockEntity(target);
        // Breaking a container would spill what's inside it; leave those for a player.
        if (targetBe instanceof Container) return;
        if (!allowsBlock(state, level, target)) return;
        ItemStack tool = tool(level);
        if (state.requiresCorrectToolForDrops() && !tool.isCorrectToolForDrops(state)) return;
        List<ItemStack> drops = Block.getDrops(state, level, target, targetBe, null, tool);
        drops.removeIf(ItemStack::isEmpty);
        if (drops.isEmpty()) return;
        if (!BlockBreakerPlatform.deliverAll(level, worldPosition, outputs, drops)) return;
        level.destroyBlock(target, false);
    }

    /**
     * The tool its drops are worked out with: a netherite pickaxe (so loot tables that look for a
     * pickaxe, or for Silk Touch, see one), whose tool rules make it the right tool for every block
     * mined with a pickaxe, axe, shovel or hoe, of any tier. Tiers are tags of blocks a tool can't
     * harvest ({@code incorrect_for_netherite_tool} and modded ones above it); with no such rule
     * nothing is too hard for it. Enchanted with Silk Touch when that's on.
     */
    private ItemStack tool(Level level) {
        ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
        List<Tool.Rule> rules = new ArrayList<>();
        for (TagKey<Block> mineable : List.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_AXE,
                BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.MINEABLE_WITH_HOE)) {
            rules.add(Tool.Rule.minesAndDrops(mineable, 9f));
        }
        tool.set(DataComponents.TOOL, new Tool(rules, 1f, 1));
        if (silkTouch) {
            tool.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        }
        return tool;
    }

    /** Whether the filters let it break this block (always, with none installed). */
    private boolean allowsBlock(BlockState state, Level level, BlockPos pos) {
        if (!SorterFilters.hasAnyFilter(filters)) return true;
        ItemStack asItem = state.getBlock().getCloneItemStack(level, pos, state);
        if (asItem.isEmpty()) asItem = new ItemStack(state.getBlock().asItem());
        return !asItem.isEmpty() && SorterFilters.matches(filters, andMode, asItem, level.registryAccess());
    }

    /** The OUTPUT sides, starting from a different one each break so drops are spread over them. */
    private List<Direction> outputs() {
        List<Direction> found = new ArrayList<>();
        for (Direction d : Direction.values()) if (getSide(d) == HopperSide.OUTPUT) found.add(d);
        if (found.size() > 1) Collections.rotate(found, -Math.floorMod(rotation++, found.size()));
        return found;
    }

    // ── Configuration ─────────────────────────────────────────────────────────────

    public HopperSide getSide(Direction dir) { return sides[dir.get3DDataValue()]; }

    @Override
    public boolean isAndMode() { return andMode; }

    public boolean isSilkTouch() { return silkTouch; }

    public void setSilkTouch(boolean on) {
        silkTouch = on;
        onConfigChanged();
    }

    public SimpleContainer getFilters() { return filters; }

    @Override
    public int packedSides() {
        int packed = 0;
        for (int i = 0; i < 6; i++) packed |= sides[i].ordinal() << (2 * i);
        return packed;
    }

    @Override
    public void setConfig(int packedSides, boolean andMode) {
        for (Direction d : Direction.values()) sides[d.get3DDataValue()] = OmniHopperBlockEntity.unpackSide(packedSides, d);
        this.andMode = andMode;
        onConfigChanged();
    }

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

    private void onConfigChanged() {
        setChanged();
        stateDirty = true;
        syncBlockState();
    }

    /** Mirrors side modes into the block state, for the connections the model shows (server only). */
    private void syncBlockState() {
        if (level == null || level.isClientSide()) return;
        stateDirty = false;
        BlockState current = getBlockState();
        if (!(current.getBlock() instanceof BlockBreakerBlock)) return;
        BlockState updated = current;
        for (Direction d : Direction.values()) {
            updated = updated.setValue(OmniHopperBlock.SIDES.get(d), getSide(d));
        }
        if (!updated.equals(current)) {
            level.setBlock(worldPosition, updated, 2);
        } else {
            level.sendBlockUpdated(worldPosition, current, current, 2); // AND/OR, Silk Touch or filter change
        }
    }

    // ── MenuProvider ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.block_breaker");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new BlockBreakerMenu(syncId, inv, this);
    }

    // ── NBT ──────────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ValueOutput output = new ValueOutput(tag, registries);
        SideLayout.save(output, sideOrientation);
        output.putInt("Sides", packedSides());
        output.putBoolean("AndMode", andMode);
        output.putBoolean("SilkTouch", silkTouch);
        List<ItemStack> stacks = new ArrayList<>(FILTER_SLOTS);
        for (int i = 0; i < FILTER_SLOTS; i++) stacks.add(filters.getItem(i));
        output.store("Filters", ItemStack.OPTIONAL_CODEC.listOf(), stacks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = new ValueInput(tag, registries);
        sideOrientation = SideLayout.load(input);
        int packed = input.getIntOr("Sides", 0);
        for (Direction d : Direction.values()) sides[d.get3DDataValue()] = OmniHopperBlockEntity.unpackSide(packed, d);
        andMode = input.getBooleanOr("AndMode", true);
        silkTouch = input.getBooleanOr("SilkTouch", false);
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
