package net.bobofraggins.mobfarmingsupplies.fan;

import net.bobofraggins.mobfarmingsupplies.MGRConfig;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Block entity for the Fan.
 *
 * <p>Ticks every 2 server ticks while {@code POWERED = true}.  Each tick it:
 * <ol>
 *   <li>Reads the upgrade slot counts (Width, Height, Distance).</li>
 *   <li>Computes the push AABB, scanning outward from the fan face and stopping
 *       at the first blocking block (see {@link MGRConfig#getFanStrongerBlades()}).</li>
 *   <li>Collects all {@link Entity} instances inside the AABB and applies a
 *       velocity impulse of 0.35 m/tick in the {@link FanBlock#FACING} direction.</li>
 * </ol>
 *
 * <h3>Upgrade slots</h3>
 * <ul>
 *   <li>Slot 0 — Width upgrade: each item widens the AABB ±1 block on the perpendicular axis.</li>
 *   <li>Slot 1 — Height upgrade: each item extends the AABB ±1 block vertically.</li>
 *   <li>Slot 2 — Distance upgrade: each item adds 1 block of depth in the facing direction.</li>
 * </ul>
 */
public class FanBlockEntity extends BlockEntity implements MenuProvider {

    // ── Constants ───────────────────────────────────────────────────────────────

    public static final int UPGRADE_SLOTS = 3;

    /** Ticks between push cycles. */
    private static final int TICK_PERIOD = 2;

    /** Velocity impulse applied to entities per push cycle (m/tick in facing direction). */
    private static final double PUSH_SPEED = 0.35;

    /** Maximum upgrades recognised per slot (stack limit in the menu). */
    public static final int MAX_UPGRADES = 5;

    // ── State ────────────────────────────────────────────────────────────────────

    /**
     * Three upgrade slots: 0 = Width, 1 = Height, 2 = Distance.
     * Each slot only accepts the corresponding {@link FanUpgradeItem} (enforced by the menu slot).
     */
    final SimpleContainer upgrades = new SimpleContainer(UPGRADE_SLOTS);

    private int tickCounter = 0;
    private int cachedDepth = -1;
    private int cachedWidthCount = -1;
    private int cachedHeightCount = -1;
    private AABB cachedAabb = null;

    /** Client-side only. Not saved or synced. Controls the in-world push-area wireframe. */
    public boolean showArea = false;

    // ── Constructor ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public FanBlockEntity(BlockPos pos, BlockState state) {
        super((net.minecraft.world.level.block.entity.BlockEntityType<FanBlockEntity>)
                        MGRRegistryHelper.getBEType("fan"), pos, state);
    }

    // ── Server tick ─────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, FanBlockEntity be) {
        if (!state.getValue(FanBlock.POWERED)) return;
        if (++be.tickCounter % TICK_PERIOD != 0) return;

        Direction facing = state.getValue(FanBlock.FACING);
        int widthCount    = Math.min(be.upgrades.getItem(0).getCount(), MAX_UPGRADES);
        int heightCount   = Math.min(be.upgrades.getItem(1).getCount(), MAX_UPGRADES);
        int distanceCount = Math.min(be.upgrades.getItem(2).getCount(), MAX_UPGRADES);
        int maxDepth      = 3 + distanceCount;

        boolean strongerBlades = MGRConfig.getFanStrongerBlades();
        int depth = computeDepth(level, pos, facing, maxDepth, strongerBlades);
        if (depth == 0) return;

        if (depth != be.cachedDepth || widthCount != be.cachedWidthCount || heightCount != be.cachedHeightCount) {
            be.cachedAabb = buildAABB(pos, facing, depth, widthCount, heightCount);
            be.cachedDepth = depth;
            be.cachedWidthCount = widthCount;
            be.cachedHeightCount = heightCount;
        }
        AABB aabb = be.cachedAabb;

        List<Entity> targets = level.getEntitiesOfClass(Entity.class, aabb);
        for (Entity entity : targets) {
            applyPush(entity, facing, level);
        }
    }

    // ── Push helpers ────────────────────────────────────────────────────────────

    private static int computeDepth(Level level, BlockPos fanPos, Direction facing,
                                    int maxDepth, boolean strongerBlades) {
        for (int d = 1; d <= maxDepth; d++) {
            BlockPos check = fanPos.relative(facing, d);
            var bs = level.getBlockState(check);
            boolean blocked = strongerBlades
                    ? !bs.getCollisionShape(level, check, CollisionContext.empty()).isEmpty()
                    : !bs.isAir();
            if (blocked) return d - 1;
        }
        return maxDepth;
    }

    private static AABB buildAABB(BlockPos pos, Direction facing, int depth,
                                  int widthCount, int heightCount) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        double halfH = 0.5 + heightCount;
        double halfW = 0.5 + widthCount;

        int sx = facing.getStepX();
        int sy = facing.getStepY();
        int sz = facing.getStepZ();

        double x0, x1, y0, y1, z0, z1;

        if (sy != 0) {
            double near = cy + sy * 0.5;
            double far  = cy + sy * (0.5 + depth);
            y0 = Math.min(near, far);
            y1 = Math.max(near, far);
            x0 = cx - halfW; x1 = cx + halfW;
            z0 = cz - halfW; z1 = cz + halfW;
        } else if (sx != 0) {
            double near = cx + sx * 0.5;
            double far  = cx + sx * (0.5 + depth);
            x0 = Math.min(near, far); x1 = Math.max(near, far);
            z0 = cz - halfW; z1 = cz + halfW;
            y0 = cy - halfH; y1 = cy + halfH;
        } else {
            double near = cz + sz * 0.5;
            double far  = cz + sz * (0.5 + depth);
            z0 = Math.min(near, far); z1 = Math.max(near, far);
            x0 = cx - halfW; x1 = cx + halfW;
            y0 = cy - halfH; y1 = cy + halfH;
        }

        return new AABB(x0, y0, z0, x1, y1, z1);
    }

    private static void applyPush(Entity entity, Direction facing, Level level) {
        Vec3 motion = entity.getDeltaMovement();
        int sx = facing.getStepX(), sy = facing.getStepY(), sz = facing.getStepZ();
        double current = motion.x * sx + motion.y * sy + motion.z * sz;
        double add = Math.max(0.0, PUSH_SPEED - current);
        if (add == 0.0) return;
        if (!level.noCollision(entity, entity.getBoundingBox().move(sx * add, sy * add, sz * add))) return;
        entity.push(sx * add, sy * add, sz * add);
        entity.hurtMarked = true;
        entity.fallDistance = 0f;
    }

    // ── Accessors ────────────────────────────────────────────────────────────────

    public SimpleContainer getUpgrades() { return upgrades; }

    // ── MenuProvider ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.fan");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new FanMenu(syncId, playerInventory, this);
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
        List<ItemStack> stacks = new ArrayList<>(UPGRADE_SLOTS);
        for (int i = 0; i < UPGRADE_SLOTS; i++) stacks.add(upgrades.getItem(i));
        output.store("Upgrades", ItemStack.OPTIONAL_CODEC.listOf(), stacks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Upgrades", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(loaded -> {
            for (int i = 0; i < Math.min(loaded.size(), UPGRADE_SLOTS); i++) {
                upgrades.setItem(i, loaded.get(i));
            }
        });
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
