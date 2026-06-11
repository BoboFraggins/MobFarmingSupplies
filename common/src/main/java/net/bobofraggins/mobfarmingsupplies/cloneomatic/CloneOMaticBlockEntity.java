package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import net.bobofraggins.mobfarmingsupplies.MGRConfig;
import net.bobofraggins.mobfarmingsupplies.dna.IDnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Block entity for the Clone-O-Matic.
 *
 * <p>Holds nine DNA sample slots.  When the block is powered by redstone, a spawn
 * attempt fires every {@link MGRConfig#getCloneOMaticSpawnInterval()} ticks.
 * Each slot accepts a {@link net.bobofraggins.mobfarmingsupplies.dna.DnaSampleItem};
 * on each proc a random populated slot is chosen and its stored entity NBT is used to
 * reconstruct the mob via {@link net.minecraft.world.entity.EntityType#loadEntityRecursive}.
 *
 * <p>Spawn constraints (fixed, not yet configurable):
 * <ul>
 *   <li>Horizontal spawn radius: {@value #SPAWN_RADIUS_XZ} blocks</li>
 *   <li>Vertical spawn surface: machine level and one block above</li>
 *   <li>Max entities in spawn volume: {@value #MAX_ENTITY_COUNT}</li>
 *   <li>Entities spawned per proc: 1–{@value #MAX_SPAWN_PER_INTERVAL}</li>
 * </ul>
 */
public class CloneOMaticBlockEntity extends BlockEntity implements MenuProvider {

    // ── Constants ────────────────────────────────────────────────────────────────

    public static final int DNA_SLOTS = 9;

    /**
     * Horizontal (X/Z) spawn radius in blocks, matching a 9x9 interior
     * (machine in the centre, 4 blocks of floor in each direction before the walls).
     */
    public static final int SPAWN_RADIUS_XZ = 4;

    /** Maximum entities within the spawn volume before spawning is skipped. */
    public static final int MAX_ENTITY_COUNT = 16;

    /** Maximum entities spawned in a single proc. */
    public static final int MAX_SPAWN_PER_INTERVAL = 4;

    // ── State ─────────────────────────────────────────────────────────────────────

    private List<ItemStack> cachedPool = null;

    final SimpleContainer dnaSlots = new SimpleContainer(DNA_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            cachedPool = null;
        }
    };

    private int spawnTickCounter = 0;

    // ── Constructor ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public CloneOMaticBlockEntity(BlockPos pos, BlockState state) {
        super((net.minecraft.world.level.block.entity.BlockEntityType<CloneOMaticBlockEntity>)
                MGRRegistryHelper.getBEType("clone_o_matic"), pos, state);
    }

    // ── Server tick ──────────────────────────────────────────────────────────────

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, CloneOMaticBlockEntity be) {
        be.tick(level, pos, state);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(CloneOMaticBlock.POWERED)) {
            return;
        }

        int interval = MGRConfig.getCloneOMaticSpawnInterval();
        if (++spawnTickCounter >= interval) {
            spawnTickCounter = 0;
            trySpawn(level, pos);
        }
    }

    private void trySpawn(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (cachedPool == null) {
            List<ItemStack> fresh = new ArrayList<>();
            for (int i = 0; i < DNA_SLOTS; i++) {
                ItemStack s = dnaSlots.getItem(i);
                if (!s.isEmpty() && s.getItem() instanceof IDnaSampleItem) fresh.add(s);
            }
            cachedPool = fresh;
        }
        List<ItemStack> pool = cachedPool;
        if (pool.isEmpty()) return;

        AABB searchBox = new AABB(
                pos.getX() + 0.5 - SPAWN_RADIUS_XZ, pos.getY() - 1, pos.getZ() + 0.5 - SPAWN_RADIUS_XZ,
                pos.getX() + 0.5 + SPAWN_RADIUS_XZ, pos.getY() + 2, pos.getZ() + 0.5 + SPAWN_RADIUS_XZ);
        int current = level.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class, searchBox,
                e -> !(e instanceof Player)).size();
        if (current >= MAX_ENTITY_COUNT) return;

        int toSpawn = 1 + serverLevel.getRandom().nextInt(MAX_SPAWN_PER_INTERVAL);
        for (int i = 0; i < toSpawn; i++) {
            if (current + i >= MAX_ENTITY_COUNT) break;

            ItemStack chosen = pool.get(serverLevel.getRandom().nextInt(pool.size()));
            Entity entity = ((IDnaSampleItem) chosen.getItem())
                    .createSpawnEntity(chosen, serverLevel, serverLevel.getRandom());
            if (entity == null) continue;

            double ox = (serverLevel.getRandom().nextDouble() * 2.0 - 1.0) * SPAWN_RADIUS_XZ;
            double oz = (serverLevel.getRandom().nextDouble() * 2.0 - 1.0) * SPAWN_RADIUS_XZ;
            BlockPos spawnPos = findSpawnSurface(serverLevel, pos, ox, oz);
            if (spawnPos == null) continue;

            double sx = spawnPos.getX() + 0.5;
            double sy = spawnPos.getY();
            double sz = spawnPos.getZ() + 0.5;

            // Wider mobs (spiders, ravagers, horses, ...) can fit their feet on a 1x1
            // surface yet still overlap a neighbouring wall block; reject those so they
            // aren't shoved outside the farm by collision resolution on the next tick.
            // Only block geometry is checked here (not entities) - a busy farm full of
            // mobs awaiting harvest must not block new spawns just because something
            // else is standing nearby.
            if (!noBlockCollision(serverLevel, entity.getType().getSpawnAABB(sx, sy, sz))) continue;

            entity.setPos(sx, sy, sz);
            serverLevel.addFreshEntity(entity);
        }
    }

    @Nullable
    private static BlockPos findSpawnSurface(ServerLevel level, BlockPos machine, double ox, double oz) {
        int x = (int) Math.floor(machine.getX() + 0.5 + ox);
        int z = (int) Math.floor(machine.getZ() + 0.5 + oz);
        for (int y = machine.getY(); y >= machine.getY() - 1; y--) {
            BlockPos ground = new BlockPos(x, y, z);
            BlockPos feet   = ground.above();
            BlockPos head   = feet.above();

            BlockState groundState = level.getBlockState(ground);
            if (!groundState.isValidSpawn(level, ground, null)) continue;
            if (!level.getFluidState(ground).isEmpty()) continue;

            if (!isClear(level, feet)) continue;
            if (!isClear(level, head)) continue;

            return feet;
        }
        return null;
    }

    private static boolean isClear(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)
                && level.getFluidState(pos).isEmpty();
    }

    /** Like {@link ServerLevel#noCollision(AABB)}, but ignores entities. */
    private static boolean noBlockCollision(ServerLevel level, AABB box) {
        for (VoxelShape shape : level.getBlockCollisions(null, box)) {
            if (!shape.isEmpty()) return false;
        }
        return true;
    }

    // ── Rising-edge trigger ──────────────────────────────────────────────────────

    /** Called by {@link CloneOMaticBlock} on the LOW→HIGH redstone transition. */
    public void onRisingEdge() {
        spawnTickCounter = 0;
        if (level != null) trySpawn(level, worldPosition);
    }

    // ── Accessors ────────────────────────────────────────────────────────────────

    public SimpleContainer getDnaSlots() {
        return dnaSlots;
    }

    // ── MenuProvider ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobfarmingsupplies.clone_o_matic");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player player) {
        return new CloneOMaticMenu(syncId, playerInv, this);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────────

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, dnaSlots);
        }
        super.preRemoveSideEffects(pos, state);
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
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>(DNA_SLOTS);
        for (int i = 0; i < DNA_SLOTS; i++) stacks.add(dnaSlots.getItem(i));
        output.store("DnaSlots", ItemStack.OPTIONAL_CODEC.listOf(), stacks);
        output.putInt("SpawnTick", spawnTickCounter);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("DnaSlots", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(loaded -> {
            for (int i = 0; i < Math.min(loaded.size(), DNA_SLOTS); i++) {
                dnaSlots.setItem(i, loaded.get(i));
            }
        });
        spawnTickCounter = input.getIntOr("SpawnTick", 0);
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
