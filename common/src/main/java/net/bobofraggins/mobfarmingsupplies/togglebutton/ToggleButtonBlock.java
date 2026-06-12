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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
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
 * <p>Sits on top of its supporting block ({@link #getShape} matches the {@code button.json}
 * model). Turning the button on plays {@link #activationSound}; turning it off plays the
 * vanilla lever-off click.
 */
public class ToggleButtonBlock extends Block {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    /** Matches the bounding box of the shared {@code button.json} model. */
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 5, 13);

    private final RegistrySupplier<SoundEvent> activationSound;
    private final MapCodec<ToggleButtonBlock> codec;

    public ToggleButtonBlock(BlockBehaviour.Properties props, RegistrySupplier<SoundEvent> activationSound) {
        super(props);
        this.activationSound = activationSound;
        this.codec = simpleCodec(p -> new ToggleButtonBlock(p, activationSound));
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

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
        return state.getValue(POWERED) && direction == Direction.DOWN ? 15 : 0;
    }

    // ── Toggle ────────────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        boolean powered = !state.getValue(POWERED);
        level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.below(), this);

        if (powered) {
            level.playSound(null, pos, activationSound.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        } else {
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, 0.5f);
        }
        level.gameEvent(player, powered ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
        return InteractionResult.SUCCESS;
    }
}
