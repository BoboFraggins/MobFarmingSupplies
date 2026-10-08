package net.bobofraggins.mobfarmingsupplies.mobhead;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A Mob Head standing on a floor, in any of 16 rotations — vanilla skull placement and shape. Its
 * look comes from {@link MobHeadBlockEntity}'s mob type (drawn by the block entity renderer).
 */
public class MobHeadBlock extends BaseEntityBlock {

    public static final MapCodec<MobHeadBlock> CODEC = simpleCodec(MobHeadBlock::new);
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);

    public MobHeadBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(ROTATION, RotationSegment.convertToSegment(ctx.getRotation() + 180));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MobHeadBlockEntity(pos, state);
    }

    /** Pick block keeps the mob type. */
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return cloneOf(level, pos);
    }

    static ItemStack cloneOf(LevelReader level, BlockPos pos) {
        ItemStack stack = new ItemStack(Registration.MOB_HEAD_ITEM.get());
        if (level.getBlockEntity(pos) instanceof MobHeadBlockEntity head && head.getMobType() != null) {
            stack.set(Registration.MOB_HEAD_TYPE.get(), head.getMobType());
        }
        return stack;
    }
}
