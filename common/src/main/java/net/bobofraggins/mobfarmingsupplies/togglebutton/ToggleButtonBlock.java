package net.bobofraggins.mobfarmingsupplies.togglebutton;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A redstone toggle button, like a vanilla {@link net.minecraft.world.level.block.LeverBlock lever}:
 * right-clicking flips {@link #POWERED} and the block emits a constant signal strength of 15
 * while powered, with no separate timeout.
 *
 * <p>Placeable on floors, walls, and ceilings — matches vanilla
 * {@link net.minecraft.world.level.block.ButtonBlock}'s use of
 * {@link FaceAttachedHorizontalDirectionalBlock} for the {@code FACE}/{@code FACING}
 * state, placement logic, and support checks. {@code button.json}'s model is authored
 * for the {@code face=floor, facing=north} orientation (matching {@link #FLOOR_SHAPE});
 * the blockstate JSON rotates it (x/y transforms) for every other orientation, exactly
 * mirroring vanilla's own button blockstate JSONs.
 *
 * <p>Turning the button on plays {@link #activationSound}; turning it off plays the
 * vanilla lever-off click.
 */
public class ToggleButtonBlock extends FaceAttachedHorizontalDirectionalBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    // Matches the bounding box of the shared button.json model at face=floor,facing=north
    // (unrotated) — the other orientations are the same box reoriented to hug whichever
    // face it's mounted on.
    private static final VoxelShape FLOOR_SHAPE = Block.box(3, 0, 3, 13, 5, 13);
    private static final VoxelShape CEILING_SHAPE = Block.box(3, 11, 3, 13, 16, 13);
    private static final VoxelShape WALL_NORTH_SHAPE = Block.box(3, 3, 11, 13, 13, 16);
    private static final VoxelShape WALL_SOUTH_SHAPE = Block.box(3, 3, 0, 13, 13, 5);
    private static final VoxelShape WALL_EAST_SHAPE = Block.box(0, 3, 3, 5, 13, 13);
    private static final VoxelShape WALL_WEST_SHAPE = Block.box(11, 3, 3, 16, 13, 13);

    private final RegistrySupplier<SoundEvent> activationSound;
    private final MapCodec<ToggleButtonBlock> codec;

    public ToggleButtonBlock(BlockBehaviour.Properties props, RegistrySupplier<SoundEvent> activationSound) {
        super(props);
        this.activationSound = activationSound;
        this.codec = simpleCodec(p -> new ToggleButtonBlock(p, activationSound));
        registerDefaultState(stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends ToggleButtonBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // FaceAttachedHorizontalDirectionalBlock/HorizontalDirectionalBlock don't
        // self-register FACING/FACE via an inherited createBlockStateDefinition — every
        // concrete subclass (including vanilla's own ButtonBlock) must add them itself.
        builder.add(FACING, POWERED, FACE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> FLOOR_SHAPE;
            case CEILING -> CEILING_SHAPE;
            case WALL -> switch (state.getValue(FACING)) {
                case NORTH -> WALL_NORTH_SHAPE;
                case SOUTH -> WALL_SOUTH_SHAPE;
                case EAST -> WALL_EAST_SHAPE;
                case WEST -> WALL_WEST_SHAPE;
                default -> FLOOR_SHAPE; // FACING is horizontal-only; unreachable
            };
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    // canSurvive / getStateForPlacement / updateShape are inherited from
    // FaceAttachedHorizontalDirectionalBlock, which already does the generic
    // floor/wall/ceiling support-check and placement-face-detection work.

    // ── Redstone output ───────────────────────────────────────────────────────────

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        // Strong power only flows into the block this button is actually mounted on
        // (matches vanilla ButtonBlock/LeverBlock), not always "down" — that was only
        // correct back when this block was floor-only.
        return state.getValue(POWERED) && direction == getConnectedDirection(state).getOpposite() ? 15 : 0;
    }

    // ── Toggle ────────────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        boolean powered = !state.getValue(POWERED);
        level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.relative(getConnectedDirection(state).getOpposite()), this);

        if (powered) {
            level.playSound(null, pos, activationSound.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        } else {
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, 0.5f);
        }
        level.gameEvent(player, powered ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
        return InteractionResult.SUCCESS;
    }
}
