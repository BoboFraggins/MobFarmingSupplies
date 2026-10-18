package net.bobofraggins.mobfarmingsupplies.headpike;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.events.common.LifecycleEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

/**
 * Every Head Pike in the world and the mobs it wards off, and the spawn check that uses them: a
 * mob whose head is on a pike can't appear on its own within {@link #RADIUS} blocks of it, in a
 * cube"On its own" is {@link #WARDED_REASONS}: natural spawns (phantoms and village cats
 * included), new chunks' animals, patrols, zombie reinforcements, golems villagers summon, and
 * events like the wandering trader and zombie sieges, but not raids. Anything a player makes
 * happen is untouched: spawners, eggs, breeding, commands, golems built by hand and the Clone-o-Matic.
 *
 * <p>Kept in the world's saved data rather than found from loaded block entities, so a pike in a
 * chunk that isn't loaded still keeps its mobs away from the chunks around it that are.
 */
public final class HeadPikes extends SavedData {

    /** How far a pike reaches in each direction, in blocks. */
    public static final int RADIUS = 64;

    /** The spawns a pike refuses: the ones nobody asked for. */
    private static final Set<EntitySpawnReason> WARDED_REASONS = EnumSet.of(
            EntitySpawnReason.NATURAL, EntitySpawnReason.CHUNK_GENERATION, EntitySpawnReason.PATROL,
            EntitySpawnReason.REINFORCEMENT, EntitySpawnReason.MOB_SUMMONED, EntitySpawnReason.EVENT);

    private record Entry(ResourceKey<Level> dimension, BlockPos pos, List<EntityType<?>> mobs) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Entry::dimension),
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().fieldOf("mobs").forGetter(Entry::mobs)
        ).apply(i, Entry::new));
    }

    private static final Codec<HeadPikes> CODEC = Entry.CODEC.listOf().xmap(HeadPikes::fromEntries, HeadPikes::toEntries);

    private static final SavedDataType<HeadPikes> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "head_pikes"),
            HeadPikes::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    /**
     * The running server's pikes. Spawn checks read it from wherever they run (new chunks fill in
     * off the server thread), so it and its maps are safe to read from any thread.
     */
    @Nullable private static volatile HeadPikes active;

    /** Per dimension, per pike, the mobs it wards off. */
    private final Map<ResourceKey<Level>, Map<BlockPos, List<EntityType<?>>>> pikes = new ConcurrentHashMap<>();

    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(HeadPikes::get);
        LifecycleEvent.SERVER_STOPPED.register(server -> active = null);
    }

    /**
     * Whether a pike keeps {@code mob} from spawning for {@code reason} at ({@code x}, {@code y},
     * {@code z}). Each loader asks this from its own spawn hook: on NeoForge, Architectury's spawn
     * check can't refuse a spawn (it passes the answer on as "cancelled").
     */
    public static boolean refuses(LevelAccessor level, Entity mob, EntitySpawnReason reason, double x, double y, double z) {
        if (!WARDED_REASONS.contains(reason)) return false;
        // Raid waves are events too, and a raid waits for every raider it sends: leave them be.
        if (reason == EntitySpawnReason.EVENT && mob instanceof Raider) return false;
        return warded(level, mob.getType(), x, y, z);
    }

    /** The server's pikes, loading them if need be. Server thread only. */
    private static HeadPikes get(MinecraftServer server) {
        HeadPikes data = server.getDataStorage().computeIfAbsent(TYPE);
        active = data;
        return data;
    }

    /** Records which mobs the pike at {@code pos} wards off (none: it's gone or bare). */
    static void update(ServerLevel level, BlockPos pos, List<EntityType<?>> mobs) {
        HeadPikes data = get(level.getServer());
        Map<BlockPos, List<EntityType<?>>> inLevel = data.pikes.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
        List<EntityType<?>> old = mobs.isEmpty() ? inLevel.remove(pos) : inLevel.put(pos.immutable(), List.copyOf(mobs));
        if (!mobs.equals(old == null ? List.of() : old)) data.setDirty();
    }

    /** Whether a pike near ({@code x}, {@code y}, {@code z}) wards off {@code type}. */
    private static boolean warded(LevelAccessor accessor, EntityType<?> type, double x, double y, double z) {
        HeadPikes data = active;
        if (data == null || !(accessor instanceof ServerLevelAccessor serverAccessor)) return false;
        ServerLevel level = serverAccessor.getLevel();
        Map<BlockPos, List<EntityType<?>>> inLevel = data.pikes.get(level.dimension());
        if (inLevel == null || inLevel.isEmpty()) return false;
        BlockPos at = BlockPos.containing(x, y, z);
        boolean serverThread = level.getServer().isSameThread();
        for (Map.Entry<BlockPos, List<EntityType<?>>> pike : inLevel.entrySet()) {
            BlockPos pos = pike.getKey();
            if (!pike.getValue().contains(type) || Math.abs(pos.getX() - at.getX()) > RADIUS
                    || Math.abs(pos.getY() - at.getY()) > RADIUS || Math.abs(pos.getZ() - at.getZ()) > RADIUS) continue;
            // A pike removed without us hearing of it (by another mod, say) shouldn't ward forever.
            if (serverThread && level.isLoaded(pos) && !(level.getBlockEntity(pos) instanceof HeadPikeBlockEntity)) {
                inLevel.remove(pos);
                data.setDirty();
                continue;
            }
            return true;
        }
        return false;
    }

    /** The mob {@code skull} is the head of, if any mob spawns as one. */
    @Nullable
    public static EntityType<?> mobOf(ItemStack skull) {
        EntityType<?> mob = skull.get(Registration.MOB_HEAD_TYPE.get());
        if (mob != null) return mob;
        if (skull.is(Items.SKELETON_SKULL)) return EntityTypes.SKELETON;
        if (skull.is(Items.WITHER_SKELETON_SKULL)) return EntityTypes.WITHER_SKELETON;
        if (skull.is(Items.ZOMBIE_HEAD)) return EntityTypes.ZOMBIE;
        if (skull.is(Items.CREEPER_HEAD)) return EntityTypes.CREEPER;
        if (skull.is(Items.PIGLIN_HEAD)) return EntityTypes.PIGLIN;
        return null; // a player or dragon head, or another mod's: just decoration
    }

    private static HeadPikes fromEntries(List<Entry> entries) {
        HeadPikes data = new HeadPikes();
        for (Entry e : entries) data.pikes.computeIfAbsent(e.dimension(), k -> new ConcurrentHashMap<>()).put(e.pos(), e.mobs());
        return data;
    }

    private List<Entry> toEntries() {
        List<Entry> entries = new ArrayList<>();
        pikes.forEach((dimension, inLevel) -> inLevel.forEach((pos, mobs) -> entries.add(new Entry(dimension, pos, mobs))));
        return entries;
    }
}
