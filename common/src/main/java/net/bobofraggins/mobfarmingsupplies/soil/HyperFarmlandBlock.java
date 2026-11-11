package net.bobofraggins.mobfarmingsupplies.soil;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Hyper Farmland: farmland that makes what's planted in it grow {@link #SPEED} times as fast. It
 * gives the plant on top (and the rest of its stalk, for sugar cane and bamboo) the extra random
 * ticks, a few at a time, so the plant grows as it would with a faster random tick speed.
 *
 * <p>It never dries out or gets trampled: it keeps vanilla's {@link FarmlandBlock#MOISTURE} at its
 * maximum, so crops on it grow as on watered farmland. What can be planted on it comes from the
 * {@code supports_*} and {@code grows_crops} block tags.
 */
public class HyperFarmlandBlock extends Block {

    public static final MapCodec<HyperFarmlandBlock> CODEC = simpleCodec(HyperFarmlandBlock::new);

    /** How many times as fast plants grow. */
    public static final int SPEED = 8;
    /** Ticks between handing out the extra random ticks. */
    private static final int INTERVAL = 20;
    /** Tallest stalk it speeds up. */
    private static final int MAX_STALK = 16;
    /** The light crops and saplings need to grow. */
    private static final int GROW_LIGHT = 9;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);
    /** Vanilla's instant-effect star, tinted yellow. */
    private static final SpellParticleOption SPARKLE =
            SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT, 0xFFFFE135, 1.0f);

    public HyperFarmlandBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE));
    }

    @Override
    protected MapCodec<HyperFarmlandBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FarmlandBlock.MOISTURE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
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
        double extra = (SPEED - 1) * level.getGameRules().get(GameRules.RANDOM_TICK_SPEED) * INTERVAL / 4096.0;
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
