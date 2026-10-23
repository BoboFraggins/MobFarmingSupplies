package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A giant lawn ornament: a figure four blocks tall ({@link GiantOrnament}), its head turning to
 * watch the nearest player (see {@link GiantOrnamentRenderer}). Only the bottom block is solid;
 * the rest of the figure is drawn above it.
 */
public class GiantOrnamentBlock extends Block implements EntityBlock {

    public static final MapCodec<GiantOrnamentBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GiantOrnament.CODEC.fieldOf("ornament").forGetter(GiantOrnamentBlock::ornament),
            propertiesCodec()).apply(i, GiantOrnamentBlock::new));

    /** The way the figure faces: towards whoever placed it. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Its feet and lower legs. */
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    private final GiantOrnament ornament;

    public GiantOrnamentBlock(GiantOrnament ornament, Properties props) {
        super(props);
        this.ornament = ornament;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public GiantOrnament ornament() {
        return ornament;
    }

    @Override
    public MapCodec<GiantOrnamentBlock> codec() {
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

    /** A Giant Player Lawn Ornament placed without an owner becomes the placer's. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (ornament == GiantOrnament.PLAYER && !level.isClientSide() && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof GiantOrnamentBlockEntity be) {
            be.claim(ResolvableProfile.createResolved(player.getGameProfile()));
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GiantOrnamentBlockEntity(pos, state);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide() || type != Registration.GIANT_ORNAMENT_BE_TYPE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<GiantOrnamentBlockEntity>)
                GiantOrnamentBlockEntity::clientTick;
    }
}
