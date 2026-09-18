package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Block entity for the placed Picnic Basket.
 *
 * <p>54-slot inventory (acts like a double chest), a chest-style lid animation state
 * machine (client-side only), and an auto-feed toggle consumed by {@code PicnicBasketFeedHandler}.
 */
public class PicnicBasketBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_COUNT = 54;

    public final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT);

    private boolean autoFeed = true;

    // ── Lid animation state (client-side only) ──────────────────────────────────

    /** Number of players with this block's menu open (synced from server via block event). */
    public int openCount = 0;

    /** Animation progress last tick (0 = closed, 1 = fully open). */
    public float prevLidAngle = 0f;

    /** Animation progress this tick. */
    public float lidAngle = 0f;

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CHEST_OPEN, SoundSource.BLOCKS,
                    0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS,
                    0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            level.blockEvent(pos, state.getBlock(), 1, newCount);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof PicnicBasketMenu m
                    && worldPosition.equals(m.getPos());
        }
    };

    public void startOpen(Player player) {
        if (!isRemoved() && !player.isSpectator()) {
            openersCounter.incrementOpeners(player, getLevel(), getBlockPos(), getBlockState(), 64.0);
        }
    }

    public void stopOpen(Player player) {
        if (!isRemoved() && !player.isSpectator()) {
            openersCounter.decrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            openCount = type;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    // ── Tickers ─────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, PicnicBasketBlockEntity be) {
        be.openersCounter.recheckOpeners(level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PicnicBasketBlockEntity be) {
        be.prevLidAngle = be.lidAngle;
        if (be.openCount > 0 && be.lidAngle < 1f) {
            be.lidAngle = Math.min(1f, be.lidAngle + 0.1f);
        } else if (be.openCount == 0 && be.lidAngle > 0f) {
            be.lidAngle = Math.max(0f, be.lidAngle - 0.1f);
        }
    }

    // ── Constructor ─────────────────────────────────────────────────────────────

    public PicnicBasketBlockEntity(BlockPos pos, BlockState state) {
        super(MGRRegistryHelper.getBEType("picnic_basket"), pos, state);
    }

    // ── Auto-feed flag ───────────────────────────────────────────────────────────

    public boolean isAutoFeed() {
        return autoFeed;
    }

    public void setAutoFeed(boolean value) {
        autoFeed = value;
        setChanged();
    }

    // ── MenuProvider ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.picnic_basket");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new PicnicBasketMenu(syncId, playerInventory, inventory, worldPosition);
    }

    // ── Notifications ────────────────────────────────────────────────────────────

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ── NBT ──────────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        List<ItemStack> stacks = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) stacks.add(inventory.getItem(i));
        output.store("Items", ItemStack.OPTIONAL_CODEC.listOf(), stacks);
        output.putBoolean("AutoFeed", autoFeed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Items", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(loaded -> {
            for (int i = 0; i < Math.min(loaded.size(), SLOT_COUNT); i++) {
                inventory.setItem(i, loaded.get(i));
            }
        });
        autoFeed = input.getBooleanOr("AutoFeed", true);
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
