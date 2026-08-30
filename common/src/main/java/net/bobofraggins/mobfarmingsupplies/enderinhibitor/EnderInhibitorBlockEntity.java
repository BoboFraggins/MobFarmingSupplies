package net.bobofraggins.mobfarmingsupplies.enderinhibitor;

import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Block entity for the Ender Inhibitor.
 *
 * <p>Carries the suppression-area offset (X/Y/Z) so players can shift the zone away
 * from the block itself, and registers/unregisters its effective center with
 * {@link EnderInhibitorEvents} so teleport suppression can use an O(k) distance check
 * rather than a block scan. Also gives the block a {@link EnderInhibitorBlockEntityRenderer}
 * attachment point for both the rotating Ender Pearl and the optional area wireframe.
 */
public class EnderInhibitorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int OFFSET_MAX = 8;

    private int offsetX = 0;
    private int offsetY = 0;
    private int offsetZ = 0;

    /** Client-side only. Controls the in-world suppression-area wireframe. */
    public boolean showArea = false;

    public EnderInhibitorBlockEntity(BlockPos pos, BlockState state) {
        super(MGRRegistryHelper.getBEType("ender_inhibitor"), pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            EnderInhibitorEvents.addInhibitor(level, worldPosition, effectiveCenter());
        }
    }

    // Called by NeoForge IBlockEntityExtension when the chunk containing this BE unloads.
    // No @Override because onChunkUnloaded() is a NeoForge extension, not vanilla.
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide()) {
            EnderInhibitorEvents.removeInhibitor(level, worldPosition);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            EnderInhibitorEvents.removeInhibitor(level, worldPosition);
        }
        super.preRemoveSideEffects(pos, state);
    }

    // ── Offset ───────────────────────────────────────────────────────────────────

    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public int getOffsetZ() { return offsetZ; }

    private BlockPos effectiveCenter() {
        return worldPosition.offset(offsetX, offsetY, offsetZ);
    }

    /** Adjusts the suppression-area offset on the given axis (0=X, 1=Y, 2=Z) by delta (±1). */
    public void adjustOffset(int axis, int delta) {
        switch (axis) {
            case 0 -> offsetX = Math.max(-OFFSET_MAX, Math.min(OFFSET_MAX, offsetX + delta));
            case 1 -> offsetY = Math.max(-OFFSET_MAX, Math.min(OFFSET_MAX, offsetY + delta));
            case 2 -> offsetZ = Math.max(-OFFSET_MAX, Math.min(OFFSET_MAX, offsetZ + delta));
        }
        if (level != null && !level.isClientSide()) {
            EnderInhibitorEvents.addInhibitor(level, worldPosition, effectiveCenter());
        }
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ── MenuProvider ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.ender_inhibitor");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new EnderInhibitorMenu(syncId, playerInventory, this);
    }

    // ── NBT ──────────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("OffsetX", offsetX);
        output.putInt("OffsetY", offsetY);
        output.putInt("OffsetZ", offsetZ);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        offsetX = input.getIntOr("OffsetX", 0);
        offsetY = input.getIntOr("OffsetY", 0);
        offsetZ = input.getIntOr("OffsetZ", 0);
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
