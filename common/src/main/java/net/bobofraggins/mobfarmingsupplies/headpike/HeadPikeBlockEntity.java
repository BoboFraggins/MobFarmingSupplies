package net.bobofraggins.mobfarmingsupplies.headpike;

import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The lower half of a Head Pike: three slots of one skull each. While a skull is on the pike, the
 * mob it's the head of can't spawn naturally nearby ({@link HeadPikes}).
 */
public class HeadPikeBlockEntity extends BaseContainerBlockEntity {

    public static final int SLOTS = 3;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    public HeadPikeBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("head_pike"), pos, state);
    }

    /** The skulls on the pike, top first, gaps left out. */
    public List<ItemStack> skulls() {
        List<ItemStack> skulls = new ArrayList<>(SLOTS);
        for (ItemStack stack : items) if (!stack.isEmpty()) skulls.add(stack);
        return skulls;
    }

    /** The mobs the pike keeps away. */
    public List<EntityType<?>> warded() {
        List<EntityType<?>> types = new ArrayList<>(SLOTS);
        for (ItemStack stack : items) {
            EntityType<?> type = HeadPikes.mobOf(stack);
            if (type != null && !types.contains(type)) types.add(type);
        }
        return types;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel serverLevel) {
            HeadPikes.update(serverLevel, worldPosition, warded());
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // Skulls can also arrive without the menu (a structure, a command, an older save): note them
    // whenever the pike joins a level or is loaded into one.
    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel serverLevel) HeadPikes.update(serverLevel, worldPosition, warded());
    }

    /** Called by the block when it's removed (see its {@code onRemove}, which also drops the skulls). */
    void preRemoveSideEffects(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (level instanceof ServerLevel serverLevel) HeadPikes.update(serverLevel, pos, List.of());
    }

    // ── Container ────────────────────────────────────────────────────────────

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(net.bobofraggins.mobfarmingsupplies.register.MFSTags.SKULLS);
    }

    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    // 1.20.1's BaseContainerBlockEntity leaves the container methods to the subclass.

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
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mobfarmingsupplies.head_pike");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new HeadPikeMenu(containerId, inventory, this);
    }

    // ── Saving and syncing ───────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        if (level instanceof ServerLevel serverLevel) HeadPikes.update(serverLevel, worldPosition, warded());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
