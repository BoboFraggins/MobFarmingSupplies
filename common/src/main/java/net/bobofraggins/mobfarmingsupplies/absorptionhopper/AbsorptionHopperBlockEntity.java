package net.bobofraggins.mobfarmingsupplies.absorptionhopper;

import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
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
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Block entity for the Absorption Hopper.
 *
 * <p>Ticks every 3 server ticks to:
 * <ol>
 *   <li>Vacuum {@link ItemEntity}s and {@link ExperienceOrb}s from a 7×7×7 pickup area (offset by
 *       {@code offsetX/Y/Z}).</li>
 *   <li>Push items (one full stack) and fluid (up to {@value #PUSH_FLUID_MB} mB) to every enabled
 *       adjacent side — delegated to {@link AbsorptionHopperBlockEntityPlatform#outputPhase}.</li>
 * </ol>
 */
public class AbsorptionHopperBlockEntity extends BlockEntity implements MenuProvider, IAbsorptionHopperBlockEntity {

    // ── Constants ───────────────────────────────────────────────────────────────

    public static final int SLOT_COUNT    = 27;
    public static final int TANK_CAPACITY = 16_000;

    private static final int    MB_PER_XP     = 20;
    public static final int     PUSH_FLUID_MB  = 1_000;
    private static final double PICKUP_RADIUS  = 3.5;
    private static final int    OFFSET_MAX     = 8;

    // ── State ────────────────────────────────────────────────────────────────────

    public final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT);

    private static Fluid xpJuiceFluid = null;

    /** Fluid type key (amount always 1 when set, empty when tank is empty). */
    public FluidStack tankFluid = FluidStack.empty();

    /** Actual mB stored (0 – {@link #TANK_CAPACITY}). */
    public int tankAmount = 0;

    /**
     * 6-bit push-sides mask.
     * Bit 0 = UP, 1 = DOWN, 2 = NORTH, 3 = SOUTH, 4 = EAST, 5 = WEST.
     */
    private int pushSides = 0;

    private int offsetX = 0;
    private int offsetY = 0;
    private int offsetZ = 0;

    private int tickCounter = 0;
    private AABB cachedPickupBox = null;
    private boolean clientSyncPending = false;

    /** Client-side only. Controls the in-world pickup-area wireframe. */
    public boolean showArea = false;

    // ── Constructor ─────────────────────────────────────────────────────────────

    public AbsorptionHopperBlockEntity(BlockPos pos, BlockState state) {
        super(MGRRegistryHelper.getBEType("absorption_hopper"), pos, state);
    }

    // ── Server tick ─────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbsorptionHopperBlockEntity be) {
        be.tick(level, pos);
    }

    private void tick(Level level, BlockPos pos) {
        if (++tickCounter % 3 != 0) return;

        pickupPhase(level, pos);
        AbsorptionHopperBlockEntityPlatform.outputPhase(this, level, pos);
        if (clientSyncPending) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            clientSyncPending = false;
        }
    }

    // ── Pickup ──────────────────────────────────────────────────────────────────

    private void pickupPhase(Level level, BlockPos pos) {
        if (cachedPickupBox == null) {
            double cx = pos.getX() + 0.5 + offsetX;
            double cy = pos.getY() + 0.5 + offsetY;
            double cz = pos.getZ() + 0.5 + offsetZ;
            cachedPickupBox = new AABB(
                    cx - PICKUP_RADIUS, cy - PICKUP_RADIUS, cz - PICKUP_RADIUS,
                    cx + PICKUP_RADIUS, cy + PICKUP_RADIUS, cz + PICKUP_RADIUS);
        }
        AABB box = cachedPickupBox;

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box,
                ie -> !ie.hasPickUpDelay() && !ie.getItem().isEmpty());
        for (ItemEntity ie : items) {
            ItemStack stack = ie.getItem();
            if (canInsertItem(stack)) {
                insertItem(stack);
                setChanged();
                if (stack.isEmpty()) ie.discard();
            }
        }

        if (tankAmount < TANK_CAPACITY) {
            List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, box);
            for (ExperienceOrb orb : orbs) {
                int mb = orb.getValue() * MB_PER_XP;
                if (tankAmount + mb <= TANK_CAPACITY) {
                    tankAmount += mb;
                    if (tankFluid.isEmpty()) {
                        if (xpJuiceFluid == null) xpJuiceFluid = MGRRegistryHelper.getFluid("xp_juice");
                        tankFluid = FluidStack.create(xpJuiceFluid, 1);
                    }
                    orb.discard();
                    setChanged();
                }
            }
        }
    }

    private boolean canInsertItem(ItemStack incoming) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack existing = inventory.getItem(i);
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(existing, incoming)
                    && existing.getCount() < existing.getMaxStackSize()) return true;
        }
        return false;
    }

    private void insertItem(ItemStack incoming) {
        for (int i = 0; i < SLOT_COUNT && !incoming.isEmpty(); i++) {
            ItemStack existing = inventory.getItem(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, incoming)) {
                int space = existing.getMaxStackSize() - existing.getCount();
                if (space > 0) {
                    int take = Math.min(incoming.getCount(), space);
                    existing.grow(take);
                    incoming.shrink(take);
                }
            }
        }
        for (int i = 0; i < SLOT_COUNT && !incoming.isEmpty(); i++) {
            if (inventory.getItem(i).isEmpty()) {
                inventory.setItem(i, incoming.copyWithCount(incoming.getCount()));
                incoming.setCount(0);
            }
        }
    }

    // ── Direction mapping ────────────────────────────────────────────────────────

    /** @deprecated Use {@link IAbsorptionHopperBlockEntity#bitToWorldDir} directly. */
    @Deprecated
    public static Direction bitToWorldDir(int bit) {
        return switch (bit) {
            case 0 -> Direction.UP;
            case 1 -> Direction.DOWN;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.EAST;
            default -> Direction.WEST;
        };
    }

    // ── Accessors ────────────────────────────────────────────────────────────────

    public SimpleContainer getInventory() { return inventory; }

    public FluidStack getTankFluid()  { return tankFluid; }
    public int getTankAmount()        { return tankAmount; }
    public int getTankCapacity()      { return TANK_CAPACITY; }

    public int getPushSides()         { return pushSides; }
    public int getOffsetX()           { return offsetX; }
    public int getOffsetY()           { return offsetY; }
    public int getOffsetZ()           { return offsetZ; }

    public void setPushSides(int mask) {
        pushSides = mask & 0x3F;
        setChanged();
        syncBlockState();
    }

    /**
     * Pushes the current {@link #pushSides} bitmask into the block's six
     * {@link AbsorptionHopperBlock} push-side properties so the multipart model
     * can show/hide each connection pipe.
     *
     * <p>Called server-side only — from {@link #setPushSides} and from {@link #onLoad}.
     */
    private void syncBlockState() {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        BlockState updated = current
                .setValue(AbsorptionHopperBlock.PUSH_UP,    (pushSides & (1 << 0)) != 0)
                .setValue(AbsorptionHopperBlock.PUSH_DOWN,  (pushSides & (1 << 1)) != 0)
                .setValue(AbsorptionHopperBlock.PUSH_NORTH, (pushSides & (1 << 2)) != 0)
                .setValue(AbsorptionHopperBlock.PUSH_SOUTH, (pushSides & (1 << 3)) != 0)
                .setValue(AbsorptionHopperBlock.PUSH_EAST,  (pushSides & (1 << 4)) != 0)
                .setValue(AbsorptionHopperBlock.PUSH_WEST,  (pushSides & (1 << 5)) != 0);
        if (!updated.equals(current)) {
            level.setBlock(worldPosition, updated, 2);
        }
    }

    @Override
    public void setLevel(net.minecraft.world.level.Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            syncBlockState();
        }
    }

    /** Adjusts the pickup-area offset on the given axis (0=X, 1=Y, 2=Z) by delta (±1). */
    public void adjustOffset(int axis, int delta) {
        switch (axis) {
            case 0 -> offsetX = Math.max(-OFFSET_MAX, Math.min(OFFSET_MAX, offsetX + delta));
            case 1 -> offsetY = Math.max(-OFFSET_MAX, Math.min(OFFSET_MAX, offsetY + delta));
            case 2 -> offsetZ = Math.max(-OFFSET_MAX, Math.min(OFFSET_MAX, offsetZ + delta));
        }
        cachedPickupBox = null;
        setChanged();
    }

    // ── MenuProvider ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.absorption_hopper");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new AbsorptionHopperMenu(syncId, playerInventory, this);
    }

    // ── Notifications ────────────────────────────────────────────────────────────

    @Override
    public void setChanged() {
        super.setChanged();
        clientSyncPending = true;
    }

    // ── NBT ──────────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) stacks.add(inventory.getItem(i));
        output.store("Items", ItemStack.OPTIONAL_CODEC.listOf(), stacks);

        if (!tankFluid.isEmpty()) {
            output.store("TankFluid", FluidStack.CODEC, tankFluid);
        }
        output.putInt("TankAmount", tankAmount);

        output.putInt("PushSides", pushSides);
        output.putInt("OffsetX", offsetX);
        output.putInt("OffsetY", offsetY);
        output.putInt("OffsetZ", offsetZ);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Items", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(loaded -> {
            for (int i = 0; i < Math.min(loaded.size(), SLOT_COUNT); i++) {
                inventory.setItem(i, loaded.get(i));
            }
        });

        tankFluid = input.read("TankFluid", FluidStack.CODEC)
                .filter(f -> !f.isEmpty())
                .map(f -> f.copyWithAmount(1))
                .orElse(FluidStack.empty());
        tankAmount = input.getIntOr("TankAmount", 0);

        pushSides = input.getIntOr("PushSides", 0);
        offsetX   = input.getIntOr("OffsetX", 0);
        offsetY   = input.getIntOr("OffsetY", 0);
        offsetZ   = input.getIntOr("OffsetZ", 0);
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
