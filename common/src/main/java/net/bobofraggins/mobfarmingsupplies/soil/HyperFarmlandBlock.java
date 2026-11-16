package net.bobofraggins.mobfarmingsupplies.soil;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * Hyper Farmland: farmland that makes what's planted in it grow {@link #SPEED} times as fast. It
 * gives the plant on top (and the rest of its stalk, for sugar cane and bamboo) the extra random
 * ticks, a few at a time, so the plant grows as it would with a faster random tick speed.
 *
 * <p>It never dries out or gets trampled: it keeps vanilla's {@link FarmBlock#MOISTURE} at its
 * maximum, so crops on it grow as on watered farmland. On 1.21.1 it is a {@link FarmBlock}, which
 * is what NeoForge checks for crops, stems and saplings; on Fabric mixins accept it where vanilla
 * asks for {@code Blocks.FARMLAND} ({@code soil.fabric.mixin}), and sugar cane and trees have
 * common mixins. Bamboo and big dripleaf take it through their block tags.
 */
public class HyperFarmlandBlock extends FarmBlock {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final MapCodec<FarmBlock> CODEC = (MapCodec) simpleCodec(HyperFarmlandBlock::new);

    /** How many times as fast plants grow. */
    public static final int SPEED = 16;
    /** Ticks between handing out the extra random ticks. */
    private static final int INTERVAL = 20;
    /** Tallest stalk it speeds up. */
    private static final int MAX_STALK = 16;
    /** The light crops and saplings need to grow. */
    private static final int GROW_LIGHT = 9;
    /** Potion swirls, tinted yellow (1.21.1 can't tint the instant-effect star). */
    private static final ColorParticleOption SPARKLE =
            ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFFFE135);

    public HyperFarmlandBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MOISTURE, MAX_MOISTURE));
    }

    @Override
    public MapCodec<FarmBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    // ── Farmland behaviour switched off ──────────────────────────────────────

    /** Stays Hyper Farmland under a solid block (farmland turns to dirt). */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    /** Farmland schedules a tick here to turn to dirt; this block's own ticks are already running. */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state;
    }

    /** Never dries out. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}

    /** Never trampled: only the fall damage a plain block deals. */
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this)) level.scheduleTick(pos, this, INTERVAL);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, INTERVAL);
        // A block gets randomTickSpeed / 4096 random ticks a tick; the plant already has one share.
        double extra = (SPEED - 1) * level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING) * INTERVAL / 4096.0;
        int ticks = (int) extra + (random.nextDouble() < extra - (int) extra ? 1 : 0);
        for (int i = 0; i < ticks; i++) tickPlant(level, pos.above(), random);
    }

    /** One random tick for the plant at {@code base} and each block of its stalk. */
    private static void tickPlant(ServerLevel level, BlockPos base, RandomSource random) {
        BlockState plant = level.getBlockState(base);
        if (!plant.isRandomlyTicking()) return;
        Block block = plant.getBlock();
        BlockPos.MutableBlockPos at = base.mutable();
        for (int i = 0; i < MAX_STALK; i++) {
            BlockState here = level.getBlockState(at);
            if (!here.is(block)) break;
            if (here.isRandomlyTicking()) here.randomTick(level, at.immutable(), random);
            at.move(0, 1, 0);
        }
    }

    /**
     * Whether it's light enough above for crops and saplings to grow: vanilla's own test, light
     * level 9, where open sky counts as full light at any time of day.
     */
    public static boolean lightEnoughToGrow(Level level, BlockPos pos) {
        return level.getRawBrightness(pos.above(), 0) >= GROW_LIGHT;
    }

    /** Yellow sparkles rising off the top, while it's light enough for crops to grow. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0 && lightEnoughToGrow(level, pos)) {
            level.addParticle(SPARKLE,
                    pos.getX() + random.nextDouble(), pos.getY() + 1.0, pos.getZ() + random.nextDouble(),
                    0, 0.05, 0);
        }
    }
}
