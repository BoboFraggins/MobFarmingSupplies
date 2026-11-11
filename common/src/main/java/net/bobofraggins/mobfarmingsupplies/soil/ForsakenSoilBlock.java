package net.bobofraggins.mobfarmingsupplies.soil;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Forsaken Soil: every second or so (on average) it tries to raise one of its biome's hostile mobs
 * on top of itself, until {@link #MOB_CAP} hostile mobs are within {@link #RANGE} blocks. The mob
 * must pass its own natural spawn rules there, so light (a torch, or daylight outdoors) turns the
 * soil off, as it would stop mobs spawning on grass.
 */
public class ForsakenSoilBlock extends Block {

    public static final MapCodec<ForsakenSoilBlock> CODEC = simpleCodec(ForsakenSoilBlock::new);

    /** Hostile mobs within {@link #RANGE} blocks that stop it spawning more. */
    public static final int MOB_CAP = 16;
    /** How far, in blocks, it counts mobs for {@link #MOB_CAP}. */
    public static final int RANGE = 8;
    /** Ticks between attempts: 10 to 30, so one a second on average. */
    private static final int MIN_DELAY = 10, MAX_DELAY = 30;

    public ForsakenSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ForsakenSoilBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this)) schedule(level, pos, level.getRandom());
    }

    private void schedule(Level level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, MIN_DELAY + random.nextInt(MAX_DELAY - MIN_DELAY + 1));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        schedule(level, pos, random);
        BlockPos spawnPos = pos.above();
        if (level.getDifficulty() == Difficulty.PEACEFUL || !canBeDarkEnough(level, spawnPos)) return;
        int nearby = level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(RANGE),
                mob -> mob.isAlive() && mob.getType().getCategory() == MobCategory.MONSTER).size();
        if (nearby >= MOB_CAP) return;

        MobSpawnSettings.SpawnerData data = level.getBiome(spawnPos).value().getMobSettings()
                .getMobs(MobCategory.MONSTER).getRandom(random).orElse(null);
        if (data == null) return;
        EntityType<?> type = data.type();
        // The mob's own natural spawn rules: darkness for monsters, swamps or slime chunks for slimes.
        if (!SpawnPlacements.isSpawnPositionOk(type, level, spawnPos)
                || !SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, spawnPos, random)) {
            return;
        }
        // Positions it on the block and rolls its gear and variant, as a spawner does.
        var entity = type.create(level, null, spawnPos, EntitySpawnReason.SPAWNER, false, false);
        if (entity == null) return;
        entity.setYRot(random.nextFloat() * 360f);
        if (!level.noCollision(entity) || level.containsAnyLiquid(entity.getBoundingBox())) {
            entity.discard();
            return;
        }
        level.addFreshEntityWithPassengers(entity);
        if (entity instanceof Mob mob) mob.spawnAnim();
    }

    /**
     * Whether it can ever be dark enough at {@code at} for a hostile mob to spawn: the fixed parts
     * of vanilla's {@link net.minecraft.world.entity.monster.Monster#isDarkEnoughToSpawn} (block
     * light at most the dimension's limit, overall brightness within its light test). Works on
     * either side, so the smoke shows exactly when spawning is possible.
     */
    public static boolean canBeDarkEnough(Level level, BlockPos at) {
        DimensionType dimension = level.dimensionType();
        return level.getBrightness(LightLayer.BLOCK, at) <= dimension.monsterSpawnBlockLightLimit()
                && level.getMaxLocalRawBrightness(at) <= dimension.monsterSpawnLightTest().maxInclusive();
    }

    /** Wisps of smoke rising off the top, like the Wither Inhibitor's, while it's dark enough to spawn. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0 && canBeDarkEnough(level, pos.above())) {
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + random.nextDouble(), pos.getY() + 1.02, pos.getZ() + random.nextDouble(),
                    0, 0.02, 0);
        }
    }
}
