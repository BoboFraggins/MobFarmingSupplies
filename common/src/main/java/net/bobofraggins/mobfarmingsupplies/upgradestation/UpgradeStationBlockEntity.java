package net.bobofraggins.mobfarmingsupplies.upgradestation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * The Upgrade Station's slots and lid. The item and input stay in the block; the result is worked
 * out from them whenever they change and is never saved, so taking it is what crafts.
 *
 * <p>The lid opens while anyone has the window open and shuts when the last of them closes it.
 */
public class UpgradeStationBlockEntity extends BlockEntity implements MenuProvider {

    public static final int ITEM_SLOT = 0;
    public static final int INPUT_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOTS = 3;

    /** How far the lid swings open, in degrees: a chest's. */
    private static final float OPEN_ANGLE = 90f;
    private static final float SWING_PER_TICK = 0.1f;
    private static final String TAG_OPEN = "Open";
    private static final String TAG_ITEMS = "Items";

    final SimpleContainer items = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            UpgradeStationBlockEntity.this.setChanged();
            updateOutput();
        }
    };

    @Nullable private RecipeManager.CachedCheck<UpgradingInput, UpgradingRecipe> recipes;

    /** Server: who has the window open. The lid is open while anyone does. */
    private final Set<UUID> viewers = new HashSet<>();
    /** Synced to clients, not saved: nobody has the window open when a world loads. */
    private boolean open;
    /** Client only: how open the lid is, 0 to 1, this tick and last. */
    private float openness, prevOpenness;

    public UpgradeStationBlockEntity(BlockPos pos, BlockState state) {
        super(MFSRegistryHelper.getBEType("upgrade_station"), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, UpgradeStationBlockEntity be) {
        be.prevOpenness = be.openness;
        be.openness = Mth.clamp(be.openness + (be.open ? SWING_PER_TICK : -SWING_PER_TICK), 0f, 1f);
    }

    // ── Crafting ───────────────────────────────────────────────────────────────

    /** Server: puts what the item and input make in the result slot, without counting as a change. */
    private void updateOutput() {
        if (!(level instanceof ServerLevel server)) return;
        ItemStack item = items.getItem(ITEM_SLOT), input = items.getItem(INPUT_SLOT);
        UpgradingInput in = new UpgradingInput(item, input);
        ItemStack result = item.isEmpty() || input.isEmpty() ? ItemStack.EMPTY
                : recipes().getRecipeFor(in, server).map(found -> found.value().assemble(in)).orElse(ItemStack.EMPTY);
        items.getItems().set(OUTPUT_SLOT, result);
    }

    private RecipeManager.CachedCheck<UpgradingInput, UpgradingRecipe> recipes() {
        if (recipes == null) {
            // In full: in a block entity, "Registration" is a type it inherits, not the mod's.
            recipes = RecipeManager.createCheck(
                    net.bobofraggins.mobfarmingsupplies.register.Registration.UPGRADING_TYPE.get());
        }
        return recipes;
    }

    // ── Lid ────────────────────────────────────────────────────────────────────

    /** A player opened the window: show the result, and open the lid if they're the first. */
    void startViewing(Player player) {
        updateOutput();
        if (viewers.add(player.getUUID()) && viewers.size() == 1) setOpen(true);
    }

    /** A player closed the window: shut the lid if they were the last. */
    void stopViewing(Player player) {
        if (viewers.remove(player.getUUID()) && viewers.isEmpty()) setOpen(false);
    }

    private void setOpen(boolean open) {
        if (level == null) return;
        this.open = open;
        // As a chest does: open when the lid starts to rise, close as it starts to fall.
        level.playSound(null, worldPosition, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE,
                SoundSource.BLOCKS, 0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /** The lid's angle in degrees, eased like a chest's, between ticks. */
    float lidAngle(float partialTick) {
        float t = 1f - Mth.lerp(partialTick, prevOpenness, openness);
        return (1f - t * t * t) * OPEN_ANGLE;
    }

    // ── Menu ───────────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new UpgradeStationMenu(id, inv, this);
    }

    /** Breaking the block drops the item and input; the result was never really there. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            items.removeItemNoUpdate(OUTPUT_SLOT);
            Containers.dropContents(level, pos, items);
        }
        super.preRemoveSideEffects(pos, state);
    }

    // ── Saving and syncing ─────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_ITEMS, ItemStack.OPTIONAL_CODEC.listOf(),
                List.of(items.getItem(ITEM_SLOT), items.getItem(INPUT_SLOT)));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read(TAG_ITEMS, ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < Math.min(list.size(), OUTPUT_SLOT); i++) items.setItem(i, list.get(i));
        });
        // Only update packets carry this.
        open = input.getBooleanOr(TAG_OPEN, false);
    }

    /** What clients need: whether the lid is open. The window sends the slots. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_OPEN, open);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
