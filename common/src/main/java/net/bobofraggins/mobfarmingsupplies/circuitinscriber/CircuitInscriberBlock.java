package net.bobofraggins.mobfarmingsupplies.circuitinscriber;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Circuit Inscriber. Its base is the block model; the arms, lasers and the item on the work
 * surface are drawn by {@link CircuitInscriberRenderer}. It faces the player who placed it, with
 * the open work surface at the front.
 */
public class CircuitInscriberBlock extends BaseEntityBlock {

    public static final MapCodec<CircuitInscriberBlock> CODEC = simpleCodec(CircuitInscriberBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Facing north: the base, the back wall and the two side rails. */
    private static final VoxelShape NORTH = Shapes.or(Block.box(0, 0, 0, 16, 8, 16), Block.box(1, 8, 15, 15, 16, 16),
            Block.box(0, 8, 2, 1, 12, 16), Block.box(15, 8, 2, 16, 12, 16));
    private static final VoxelShape EAST = rotate(NORTH, 1);
    private static final VoxelShape SOUTH = rotate(NORTH, 2);
    private static final VoxelShape WEST = rotate(NORTH, 3);

    public CircuitInscriberBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** {@code shape} turned clockwise (seen from above) by {@code quarters} quarter turns. */
    private static VoxelShape rotate(VoxelShape shape, int quarters) {
        VoxelShape result = shape;
        for (int i = 0; i < quarters; i++) {
            VoxelShape[] turned = {Shapes.empty()};
            result.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                    turned[0] = Shapes.or(turned[0], Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2)));
            result = turned[0];
        }
        return result;
    }

    @Override
    protected MapCodec<CircuitInscriberBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp
                && level.getBlockEntity(pos) instanceof CircuitInscriberBlockEntity be) {
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitInscriberBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (type != Registration.CIRCUIT_INSCRIBER_BE_TYPE.get()) return null;
        BlockEntityTicker<CircuitInscriberBlockEntity> ticker = level.isClientSide()
                ? CircuitInscriberBlockEntity::clientTick : CircuitInscriberBlockEntity::serverTick;
        return (BlockEntityTicker<T>) ticker;
    }
}
