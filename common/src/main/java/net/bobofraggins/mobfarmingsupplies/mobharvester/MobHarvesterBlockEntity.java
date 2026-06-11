package net.bobofraggins.mobfarmingsupplies.mobharvester;

import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Block entity for the Mob Harvester.
 *
 * <p>Holds three typed upgrade slots (Sharpness, Looting, Beheading).
 * When powered, attacks all non-player {@link LivingEntity}s in its kill zone
 * every {@link #ATTACK_INTERVAL_TICKS} ticks using a platform-specific fake player
 * (NeoForge) or direct hurt (Fabric).
 *
 * <p>Kill zone: {@value #KILL_WIDTH}×{@value #KILL_HEIGHT}×{@value #KILL_WIDTH}
 * blocks centred on the harvester's top face.
 */
@SuppressWarnings("unchecked")
public class MobHarvesterBlockEntity extends BlockEntity implements MenuProvider {

    // ── Constants ─────────────────────────────────────────────────────────────────

    /** Number of upgrade slots (one per {@link HarvesterUpgradeItem.UpgradeType}). */
    public static final int UPGRADE_SLOTS = HarvesterUpgradeItem.UpgradeType.values().length; // 3

    /** Ticks between attack attempts when continuously powered. */
    public static final int ATTACK_INTERVAL_TICKS = 10;

    /** Kill-zone horizontal half-extent (blocks from block centre). */
    public static final double KILL_WIDTH = 1.5;

    /**
     * Kill-zone vertical height from the block's base.
     *
     * <p>The harvester block itself has a two-block-tall hitbox ({@link MobHarvesterBlock#getShape}),
     * so a mob standing on top of it has its feet at {@code +2}. Extending to {@code +3}
     * ensures those mobs' bounding boxes still overlap the kill box rather than just
     * grazing its upper edge.
     */
    public static final double KILL_HEIGHT = 3.0;

    // ── State ─────────────────────────────────────────────────────────────────────

    private static net.minecraft.world.item.Item harvesterSwordItem = null;
    private static net.minecraft.core.component.DataComponentType<Integer> beheadingLevelType = null;

    private ItemStack cachedSword = null;
    private boolean swordDirty = true;

    final SimpleContainer upgrades = new SimpleContainer(UPGRADE_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            swordDirty = true;
        }
    };

    private int attackTickCounter = 0;
    private AABB killBox;

    // ── Constructor ───────────────────────────────────────────────────────────────

    public MobHarvesterBlockEntity(BlockPos pos, BlockState state) {
        super((net.minecraft.world.level.block.entity.BlockEntityType<MobHarvesterBlockEntity>)
                MGRRegistryHelper.getBEType("mob_harvester"), pos, state);
        killBox = new AABB(
                pos.getX() + 0.5 - KILL_WIDTH, pos.getY(),       pos.getZ() + 0.5 - KILL_WIDTH,
                pos.getX() + 0.5 + KILL_WIDTH, pos.getY() + KILL_HEIGHT, pos.getZ() + 0.5 + KILL_WIDTH);
    }

    // ── Server tick ───────────────────────────────────────────────────────────────

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, MobHarvesterBlockEntity be) {
        be.tick(level, pos, state);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(MobHarvesterBlock.POWERED)) return;
        if (++attackTickCounter < ATTACK_INTERVAL_TICKS) return;
        attackTickCounter = 0;
        attack(level, pos);
    }

    private void attack(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, killBox);
        if (targets.isEmpty()) return;

        if (swordDirty) {
            if (harvesterSwordItem == null) harvesterSwordItem = MGRRegistryHelper.getItem("harvester_sword");
            if (beheadingLevelType == null) beheadingLevelType = MGRRegistryHelper.getDataComponentType("beheading_level");
            ItemStack sword = new ItemStack(harvesterSwordItem);
            int sharpness = getUpgradeCount(HarvesterUpgradeItem.UpgradeType.SHARPNESS);
            int looting   = getUpgradeCount(HarvesterUpgradeItem.UpgradeType.LOOTING);
            int beheading = getUpgradeCount(HarvesterUpgradeItem.UpgradeType.BEHEADING);
            var enchReg = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            if (sharpness > 0)
                sword.enchant(enchReg.getOrThrow(Enchantments.SHARPNESS), sharpness * 10);
            if (looting > 0)
                sword.enchant(enchReg.getOrThrow(Enchantments.LOOTING), looting);
            if (beheading > 0)
                sword.set(beheadingLevelType, beheading);
            cachedSword = sword;
            swordDirty = false;
        }

        MobHarvesterAttackPlatform.attackTargets(serverLevel, pos, cachedSword, targets);
    }

    private int getUpgradeCount(HarvesterUpgradeItem.UpgradeType type) {
        ItemStack stack = upgrades.getItem(type.ordinal());
        if (stack.isEmpty()) return 0;
        if (!(stack.getItem() instanceof HarvesterUpgradeItem upgrade)) return 0;
        if (upgrade.upgradeType != type) return 0;
        return Math.min(stack.getCount(), type.maxStack);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────────

    public SimpleContainer getUpgrades() {
        return upgrades;
    }

    // ── MenuProvider ──────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.mob_harvester");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player player) {
        return new MobHarvesterMenu(syncId, playerInv, this);
    }

    // ── Notifications ─────────────────────────────────────────────────────────────

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>(UPGRADE_SLOTS);
        for (int i = 0; i < UPGRADE_SLOTS; i++) stacks.add(upgrades.getItem(i));
        output.store("Upgrades", ItemStack.OPTIONAL_CODEC.listOf(), stacks);
        output.putInt("AttackTick", attackTickCounter);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Upgrades", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(loaded -> {
            for (int i = 0; i < Math.min(loaded.size(), UPGRADE_SLOTS); i++) {
                upgrades.setItem(i, loaded.get(i));
            }
        });
        attackTickCounter = input.getIntOr("AttackTick", 0);
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
