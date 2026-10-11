package net.bobofraggins.mobfarmingsupplies.stirlingengine;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Stirling Engine of one tier: makes energy from heat below it (see {@link StirlingEngineBlockEntity}).
 * The whole engine is drawn by {@link StirlingEngineRenderer} so its flywheel and piston can move.
 */
public class StirlingEngineBlock extends BaseEntityBlock {

    public static final MapCodec<StirlingEngineBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            StirlingEngineTier.CODEC.fieldOf("tier").forGetter(StirlingEngineBlock::tier),
            propertiesCodec()).apply(i, StirlingEngineBlock::new));

    /** The engine's base and frame: the full block, twelve pixels high. */
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    private final StirlingEngineTier tier;

    public StirlingEngineBlock(StirlingEngineTier tier, Properties props) {
        super(props);
        this.tier = tier;
    }

    public StirlingEngineTier tier() {
        return tier;
    }

    @Override
    public MapCodec<StirlingEngineBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StirlingEngineBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (type != Registration.STIRLING_ENGINE_BE_TYPE.get()) return null;
        BlockEntityTicker<StirlingEngineBlockEntity> ticker = level.isClientSide()
                ? StirlingEngineBlockEntity::clientTick : StirlingEngineBlockEntity::serverTick;
        return (BlockEntityTicker<T>) ticker;
    }
}
