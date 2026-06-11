package net.bobofraggins.mobfarmingsupplies.tank;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
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
 * Stores a single fluid type, locked on first insertion. Capacity is fixed at
 * {@value #CAPACITY} mB (16 buckets). No tiers, no upgrades.
 *
 * <p>All fluid I/O exposed to automation goes through {@link TankFluidHandler}
 * (NeoForge) or the equivalent Fabric storage (Phase 7b).
 * Direct {@link #insert} and {@link #extract} helpers are provided for
 * bucket/bottle interaction via {@link TankBlockEntityPlatform}.
 */
public class TankBlockEntity extends BlockEntity implements MenuProvider {

    public static final long CAPACITY = 64_000L;

    /** Fluid type key — always stored with amount=1. {@link FluidStack#empty()} means unlocked. */
    public FluidStack storedFluid = FluidStack.empty();

    /** Actual stored amount in mB. */
    public long amount = 0L;

    /** Two-slot container: slot 0 = fluid input, slot 1 = filled/emptied output. */
    public final SimpleContainer transferContainer = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            TankBlockEntity.this.setChanged();
        }
    };

    private int transferTick = 0;

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(MGRRegistryHelper.getBEType("tank"), pos, state);
    }

    // ── Server tick ────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, TankBlockEntity be) {
        if (++be.transferTick % 4 == 0) be.tickFluidTransfer();
    }

    private void tickFluidTransfer() {
        ItemStack input = transferContainer.getItem(0);
        if (input.isEmpty()) return;
        if (!transferContainer.getItem(1).isEmpty()) return;

        ItemStack output = TankBlockEntityPlatform.tryTransferFluidWithItem(this, input);
        if (output != null) {
            transferContainer.setItem(0, ItemStack.EMPTY);
            transferContainer.setItem(1, output);
        }
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    public boolean isLocked() {
        return !storedFluid.isEmpty();
    }

    public FluidStack getStoredFluid() {
        return storedFluid;
    }

    public long getAmount() {
        return amount;
    }

    public long getCapacity() {
        return CAPACITY;
    }

    // ── Mutation ───────────────────────────────────────────────────────────────

    /**
     * Inserts up to {@code requested} mB of {@code fluid}.
     *
     * @param simulate if true, returns the insertable amount without changing state
     * @return mB actually (or hypothetically) inserted
     */
    public long insert(FluidStack fluid, long requested, boolean simulate) {
        if (fluid.isEmpty() || requested <= 0) return 0;
        if (!storedFluid.isEmpty()
                && (!storedFluid.isFluidEqual(fluid) || !storedFluid.isComponentEqual(fluid))) return 0;

        long space = CAPACITY - amount;
        long toInsert = Math.min(requested, space);
        if (toInsert <= 0) return 0;

        if (!simulate) {
            if (storedFluid.isEmpty()) storedFluid = fluid.copyWithAmount(1);
            amount += toInsert;
            notifyFluidChanged();
        }
        return toInsert;
    }

    /**
     * Extracts up to {@code requested} mB.
     *
     * @param simulate if true, returns the extractable amount without changing state
     * @return the extracted (or hypothetically extracted) FluidStack, or empty
     */
    public FluidStack extract(long requested, boolean simulate) {
        if (storedFluid.isEmpty() || amount == 0 || requested <= 0) return FluidStack.empty();

        long toExtract = Math.min(requested, Math.min(Integer.MAX_VALUE, amount));
        if (toExtract <= 0) return FluidStack.empty();

        FluidStack result = storedFluid.copyWithAmount(toExtract);
        if (!simulate) {
            amount -= toExtract;
            if (amount <= 0) {
                amount = 0;
                storedFluid = FluidStack.empty();
            }
            notifyFluidChanged();
        }
        return result;
    }

    // ── MenuProvider ───────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.tank");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new TankMenu(id, inv, worldPosition, transferContainer);
    }

    // ── Notifications ──────────────────────────────────────────────────────────

    /** Called after fluid mutations — saves and syncs to clients. */
    void notifyFluidChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            TankBlockEntityPlatform.onCapabilitiesChanged(level, worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ── NBT ────────────────────────────────────────────────────────────────────

    private static final String TAG_FLUID    = "StoredFluid";
    private static final String TAG_AMOUNT   = "Amount";
    private static final String TAG_TRANSFER = "Transfer";

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!storedFluid.isEmpty()) {
            output.store(TAG_FLUID, FluidStack.CODEC, storedFluid);
        }
        output.putLong(TAG_AMOUNT, amount);
        List<ItemStack> stacks = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) stacks.add(transferContainer.getItem(i));
        output.store(TAG_TRANSFER, ItemStack.OPTIONAL_CODEC.listOf(), stacks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedFluid = input.read(TAG_FLUID, FluidStack.CODEC)
                .filter(f -> !f.isEmpty())
                .map(f -> f.copyWithAmount(1))
                .orElse(FluidStack.empty());
        amount = input.getLongOr(TAG_AMOUNT, 0L);
        input.read(TAG_TRANSFER, ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < Math.min(list.size(), 2); i++) {
                transferContainer.setItem(i, list.get(i));
            }
        });
    }

    // ── Data component sync (item ↔ world) ─────────────────────────────────────

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(MGRRegistryHelper.getDataComponentType("tank_contents"),
                new TankContents(storedFluid, amount));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        TankContents contents = input.get(MGRRegistryHelper.<TankContents>getDataComponentType("tank_contents"));
        if (contents != null) {
            storedFluid = contents.storedFluid().isEmpty()
                    ? FluidStack.empty()
                    : contents.storedFluid().copyWithAmount(1);
            amount = contents.amount();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard(TAG_FLUID);
        output.discard(TAG_AMOUNT);
    }

    // ── Client sync ────────────────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
