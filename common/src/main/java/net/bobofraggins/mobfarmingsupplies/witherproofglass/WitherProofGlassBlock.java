package net.bobofraggins.mobfarmingsupplies.witherproofglass;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * A transparent glass block immune to all explosions and to direct destruction by the Wither.
 *
 * <p>Adjacent wither-proof glass blocks connect visually: six boolean blockstate properties
 * track same-type neighbours in every direction, selecting a model variant whose texture
 * omits the 1-pixel obsidian border on each connected edge.
 *
 * <p>Face suppression (hiding shared interior faces) is inherited from
 * {@link HalfTransparentBlock#skipRendering}, which fires whenever the adjacent block is
 * the same type, regardless of property values.
 */
public class WitherProofGlassBlock extends HalfTransparentBlock {

    public static final MapCodec<WitherProofGlassBlock> CODEC = simpleCodec(WitherProofGlassBlock::new);

    // ── Connection properties ──────────────────────────────────────────────────
    public static final BooleanProperty UP    = BlockStateProperties.UP;
    public static final BooleanProperty DOWN  = BlockStateProperties.DOWN;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST  = BlockStateProperties.EAST;
    public static final BooleanProperty WEST  = BlockStateProperties.WEST;

    public WitherProofGlassBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(UP,    false)
                .setValue(DOWN,  false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST,  false)
                .setValue(WEST,  false));
    }

    @Override
    public MapCodec<WitherProofGlassBlock> codec() {
        return CODEC;
    }

    // ── Block state definition ─────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
    }

    // ── Placement ──────────────────────────────────────────────────────────────

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockGetter level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        return defaultBlockState()
                .setValue(UP,    connects(level.getBlockState(pos.above())))
                .setValue(DOWN,  connects(level.getBlockState(pos.below())))
                .setValue(NORTH, connects(level.getBlockState(pos.north())))
                .setValue(SOUTH, connects(level.getBlockState(pos.south())))
                .setValue(EAST,  connects(level.getBlockState(pos.east())))
                .setValue(WEST,  connects(level.getBlockState(pos.west())));
    }

    // ── Neighbour update ───────────────────────────────────────────────────────

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction dir,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        return state.setValue(propFor(dir), connects(neighbourState));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private boolean connects(BlockState neighbour) {
        return neighbour.getBlock() instanceof WitherProofGlassBlock;
    }

    private static BooleanProperty propFor(Direction dir) {
        return switch (dir) {
            case UP    -> UP;
            case DOWN  -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST  -> EAST;
            case WEST  -> WEST;
        };
    }

    // ── Light blocking ─────────────────────────────────────────────────────────

    @Override
    protected int getLightDampening(BlockState state) {
        return 15;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return false;
    }

    // ── Wither immunity ────────────────────────────────────────────────────────

    // NeoForge IBlockExtension — no @Override since this method is not in vanilla Block
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return !(entity instanceof WitherBoss) && !(entity instanceof WitherSkull);
    }

    // NeoForge IBlockExtension — no @Override since this method is not in vanilla Block
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return Float.MAX_VALUE;
    }
}
