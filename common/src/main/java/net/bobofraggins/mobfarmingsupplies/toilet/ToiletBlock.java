package net.bobofraggins.mobfarmingsupplies.toilet;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.MFSTags;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Toilet — voids any item pushed into it, supplies unlimited water (like Cooking for
 * Blockheads' kitchen sink) and can be sat on.
 *
 * <p>Right-click with an empty hand: the lid swings up (with the chest-open sound) and the player
 * sits on a {@link ToiletSeatEntity}; when they stand up it flushes and the lid closes again. A mob
 * that walks onto the bowl sits down the same way and stays until it is made to get up (a player
 * right-clicking takes its place). Right-click with an empty bucket or other fluid container: it is
 * filled with water instead. There is no UI.
 *
 * <p>The item voiding and the water are exposed as platform storages on every side
 * ({@code ToiletItemHandler} / {@code ToiletFluidHandler} on NeoForge, {@code ToiletItemStorage}
 * / {@code ToiletWaterStorage} on Fabric).
 *
 * <p>The block model is the bowl only; the lid is drawn by {@link ToiletBlockEntityRenderer} so
 * it can swing between closed and {@link #OPEN}.
 */
public class ToiletBlock extends Block implements EntityBlock {

    public static final MapCodec<ToiletBlock> CODEC = simpleCodec(ToiletBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** Lid up — someone is sitting on the toilet. */
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    /** How often an open lid checks that its seat is still occupied (self-heals after a reload). */
    private static final int RECHECK_TICKS = 20;
    /** How far away a mob on the player's lead can be and still be seated in their place. */
    private static final int LEASH_REACH = 10;

    // The model is authored facing north (bowl front toward -Z, cistern at +Z).
    private static final VoxelShape SHAPE_NORTH = shape(3, 0, 0, 13, 6, 16, 1, 5, 11, 15, 14, 16);
    private static final VoxelShape SHAPE_EAST  = shape(0, 0, 3, 16, 6, 13, 0, 5, 1, 5, 14, 15);
    private static final VoxelShape SHAPE_SOUTH = shape(3, 0, 0, 13, 6, 16, 1, 5, 0, 15, 14, 5);
    private static final VoxelShape SHAPE_WEST  = shape(0, 0, 3, 16, 6, 13, 11, 5, 1, 16, 14, 15);

    /** Bowl box then cistern box. */
    private static VoxelShape shape(double... b) {
        return Shapes.or(Block.box(b[0], b[1], b[2], b[3], b[4], b[5]),
                Block.box(b[6], b[7], b[8], b[9], b[10], b[11]));
    }

    public ToiletBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    public MapCodec<ToiletBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    // ── Placement & shape ────────────────────────────────────────────────────────

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Bowl faces the player who placed it.
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        // Keeps the bowl facing the right way when a structure is placed rotated.
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    // ── Interaction ──────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        // Empty bucket / fluid container: fill it with water, like a kitchen sink. Anything else
        // (or a container that can't take water) falls through to sitting down.
        boolean filled = TankBlockPlatform.handleFluidItemInteraction(player, hand, level, pos, true);
        return filled ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isPassenger()) return InteractionResult.PASS;

        ToiletSeatEntity taken = ToiletSeatEntity.findOccupied(level, pos);
        if (taken != null) {
            // A mob sitting here gets up for a player; another player keeps their seat.
            if (taken.getFirstPassenger() instanceof Player) return InteractionResult.PASS;
            taken.swapRiderFor(player);
            return InteractionResult.SUCCESS;
        }

        // A mob on the player's lead is seated in their place, so you can put your pet on the toilet.
        Entity rider = findLeashedSitter(level, player);
        boolean seated = sitDown(level, pos, state, rider != null ? rider : player);
        return seated ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    // ── Mobs sitting down ────────────────────────────────────────────────────────

    /**
     * Called by vanilla every tick something stands on this block (26.2 replaced the fall-on hook
     * the other branches use). A mob standing on the bowl sits down if it may and the seat is free.
     */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.isClientSide() || !mayMobSit(entity)) return;
        if (!ToiletSeatEntity.isOccupied(level, pos)) {
            sitDown(level, pos, state, entity);
        }
    }

    /** Living, not a player, not already riding something, and not excluded by tag. */
    @SuppressWarnings("deprecation") // builtInRegistryHolder(): no direct EntityType.is(TagKey) in 26.1.2
    private static boolean mayMobSit(Entity entity) {
        return entity instanceof LivingEntity
                && !(entity instanceof Player)
                && !entity.isPassenger()
                && !entity.getType().builtInRegistryHolder().is(MFSTags.EntityTypes.CANNOT_USE_TOILET);
    }

    /** A mob on {@code player}'s lead, within {@value #LEASH_REACH} blocks, that may sit; or {@code null}. */
    @Nullable
    private static Entity findLeashedSitter(Level level, Player player) {
        AABB around = player.getBoundingBox().inflate(LEASH_REACH);
        return level.getEntitiesOfClass(Mob.class, around,
                        mob -> mob.getLeashHolder() == player && mayMobSit(mob))
                .stream().findFirst().orElse(null);
    }

    /** Seats {@code rider} on the toilet at {@code pos}: lid up, chest-open sound. */
    private static boolean sitDown(Level level, BlockPos pos, BlockState state, Entity rider) {
        ToiletSeatEntity seat = ToiletSeatEntity.create(level, pos, state.getValue(FACING));
        level.addFreshEntity(seat);
        if (!rider.startRiding(seat, true, true)) {
            seat.discard();
            return false;
        }
        setOpen(level, pos, true);
        // Volume and pitch as vanilla's chest uses for its lid.
        level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS,
                0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        level.scheduleTick(pos, state.getBlock(), RECHECK_TICKS);
        return true;
    }

    /** Opens or closes the lid at {@code pos}, if there is still a toilet there. */
    static void setOpen(Level level, BlockPos pos, boolean open) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ToiletBlock && state.getValue(OPEN) != open) {
            level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
        }
    }

    // Seats aren't saved, so after a reload (or anything else that loses the seat) the lid
    // would stay up for good - close it once nobody is sitting here.
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(OPEN)) return;
        if (ToiletSeatEntity.isOccupied(level, pos)) {
            level.scheduleTick(pos, this, RECHECK_TICKS);
        } else {
            setOpen(level, pos, false);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (state.getValue(OPEN) && !level.isClientSide()) level.scheduleTick(pos, this, RECHECK_TICKS);
    }

    // ── Block entity (lid animation only) ────────────────────────────────────────

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToiletBlockEntity(pos, state);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide() || type != Registration.TOILET_BE_TYPE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<ToiletBlockEntity>) ToiletBlockEntity::clientTick;
    }
}
