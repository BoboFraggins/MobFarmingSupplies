package net.bobofraggins.mobfarmingsupplies.bridge;

import net.bobofraggins.mobfarmingsupplies.advancement.MFSTriggers;
import net.bobofraggins.mobfarmingsupplies.shared.sides.SideLayout;
import net.bobofraggins.mobfarmingsupplies.shared.sides.SideOriented;
import net.minecraft.core.FrontAndTop;
import java.util.Arrays;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SorterFilters;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperConfigurable;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperNode;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperOutput;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlock;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperPlatform;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Einstein-Rosen Bridge: an Omnidirectional Hopper whose INPUT sides feed the OUTPUT sides of every
 * loaded bridge on the same channel, in any dimension (see {@link BridgeNetworks}).
 *
 * <p>The channel is a random 32-bit number (0 = not assigned yet; one is rolled on the first tick).
 * Item Filters and AND / OR are shared by the whole channel and saved with the world
 * ({@link BridgeChannelData}); each bridge keeps its own side configuration. Like the hopper it holds
 * nothing, so nothing is lost when a bridge unloads or breaks.
 */
public class EinsteinRosenBridgeBlockEntity extends BlockEntity implements SideOriented, MenuProvider, HopperNode, HopperConfigurable {

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


    private int channel;
    @Nullable private Integer joinedChannel; // the network this bridge is registered in, server side
    private final HopperSide[] sides = new HopperSide[6]; // indexed by Direction#get3DDataValue
    private boolean clientAndMode = true;                // client copy of the channel's AND / OR

    private int tickCounter = 0;
    private boolean stateDirty = true;

    public EinsteinRosenBridgeBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("einstein_rosen_bridge"), pos, state);
        Arrays.fill(sides, HopperSide.NONE);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, EinsteinRosenBridgeBlockEntity be) {
        if (be.channel == 0) be.setChannel(randomChannel(level.getRandom()));
        be.joinNetwork();
        if (be.stateDirty) be.syncBlockState();
        if (!be.isActive()) return;
        boolean itemTick = ++be.tickCounter % Math.max(1, MFSConfig.getHopperTransferInterval()) == 0;
        OmniHopperPlatform.transfer(be, itemTick);
    }

    /** A random non-zero channel (0 means "not assigned yet"). */
    public static int randomChannel(RandomSource random) {
        int c;
        do c = random.nextInt(); while (c == 0);
        return c;
    }

    // ── Channel and network ───────────────────────────────────────────────────────

    public int getChannel() { return channel; }

    public void setChannel(int channel) {
        if (this.channel == channel) return;
        this.channel = channel;
        setChanged();
        if (level != null && !level.isClientSide()) {
            joinNetwork();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2); // channel shown by Jade
        }
    }

    /** Joins (or moves to) the network for the current channel (server only). */
    private void joinNetwork() {
        if (level == null || level.isClientSide() || isRemoved() || channel == 0) return;
        if (joinedChannel != null && joinedChannel == channel) return;
        leaveNetwork();
        BridgeNetworks.join(this, channel);
        joinedChannel = channel;
    }

    private void leaveNetwork() {
        if (joinedChannel == null) return;
        BridgeNetworks.leave(this, joinedChannel);
        joinedChannel = null;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        leaveNetwork(); // broken or unloaded
    }

    /** The channel's shared settings (server only). */
    private BridgeChannelData.Settings settings() {
        return BridgeChannelData.get(((ServerLevel) level).getServer()).settings(channel);
    }

    /** The channel's shared Item Filters (server only). */
    public Container getFilters() {
        return settings().filters;
    }

    // ── Configuration ─────────────────────────────────────────────────────────────

    @Override
    public HopperSide getSide(Direction dir) { return sides[dir.get3DDataValue()]; }

    @Override
    public int packedSides() {
        int packed = 0;
        for (int i = 0; i < 6; i++) packed |= sides[i].ordinal() << (2 * i);
        return packed;
    }

    @Override
    public boolean isAndMode() {
        return level == null || level.isClientSide() ? clientAndMode : settings().andMode();
    }

    @Override
    public void setConfig(int packedSides, boolean andMode) {
        for (Direction d : Direction.values()) sides[d.get3DDataValue()] = OmniHopperBlockEntity.unpackSide(packedSides, d);
        setChanged();
        stateDirty = true;
        syncBlockState();
        if (level == null || level.isClientSide()) return;
        OmniHopperPlatform.invalidateCapabilities(level, worldPosition);
        if (settings().andMode() != andMode) {
            settings().setAndMode(andMode);
            // Every bridge on the channel shows the shared setting in its screen.
            for (EinsteinRosenBridgeBlockEntity bridge : BridgeNetworks.members(channel)) {
                if (bridge.level != null) {
                    bridge.level.sendBlockUpdated(bridge.worldPosition, bridge.getBlockState(), bridge.getBlockState(), 2);
                }
            }
        }
    }

    /** Mirrors side modes into the block state (server only). */
    private void syncBlockState() {
        if (level == null || level.isClientSide()) return;
        stateDirty = false;
        BlockState current = getBlockState();
        if (!(current.getBlock() instanceof EinsteinRosenBridgeBlock)) return;
        BlockState updated = current;
        for (Direction d : Direction.values()) updated = updated.setValue(OmniHopperBlock.SIDES.get(d), getSide(d));
        if (!updated.equals(current)) {
            level.setBlock(worldPosition, updated, 2);
        } else {
            level.sendBlockUpdated(worldPosition, current, current, 2);
        }
    }

    // ── HopperNode ────────────────────────────────────────────────────────────────

    @Override
    public boolean isActive() { return channel != 0 && BridgeNetworks.isActive(channel); }

    /** Whether an item may move: with no filters installed on the channel, every item may. */
    @Override
    public boolean allowsItem(ItemStack stack) {
        BridgeChannelData.Settings s = settings();
        if (!SorterFilters.hasAnyFilter(s.filters)) return true;
        return SorterFilters.matches(s.filters, s.andMode(), stack, level.registryAccess());
    }

    @Override
    public List<HopperOutput> outputTargets() { return BridgeNetworks.outputs(channel); }

    @Override
    public boolean beginRouting() { return BridgeNetworks.beginRouting(channel); }

    @Override
    public void endRouting() { BridgeNetworks.endRouting(channel); }

    /** "Spooky Action at a Distance": a delivery into another dimension credits players near either end. */
    @Override
    public void delivered(HopperOutput out) {
        if (level == null || out.level() == level) return;
        MFSTriggers.triggerNear(MFSTriggers.BRIDGE_CROSSED_DIMENSIONS, level, worldPosition, "");
        MFSTriggers.triggerNear(MFSTriggers.BRIDGE_CROSSED_DIMENSIONS, out.level(), out.pos(), "");
    }

    // ── MenuProvider ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return EinsteinRosenBridgeBlockItem.nameFor(channel);
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new EinsteinRosenBridgeMenu(syncId, inv, this);
    }

    // ── Item components (the channel travels on the item) ──────────────────────────

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        Integer fromItem = components.get(net.bobofraggins.mobfarmingsupplies.register.Registration.BRIDGE_CHANNEL.get());
        if (fromItem != null && fromItem != 0) setChannel(fromItem);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (channel != 0) components.set(net.bobofraggins.mobfarmingsupplies.register.Registration.BRIDGE_CHANNEL.get(), channel);
    }

    // ── NBT ──────────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        SideLayout.save(output, sideOrientation);
        output.putInt("Channel", channel);
        output.putInt("Sides", packedSides());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        sideOrientation = SideLayout.load(input);
        int loaded = input.getIntOr("Channel", 0);
        if (loaded != 0) channel = loaded; // joins its network on the next tick
        int packed = input.getIntOr("Sides", 0);
        for (Direction d : Direction.values()) sides[d.get3DDataValue()] = OmniHopperBlockEntity.unpackSide(packed, d);
        clientAndMode = input.getBooleanOr("AndMode", true); // only present in client updates
        stateDirty = true;
    }

    // ── Client sync ───────────────────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        if (level != null && !level.isClientSide() && channel != 0) tag.putBoolean("AndMode", settings().andMode());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
