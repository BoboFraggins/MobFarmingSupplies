package net.bobofraggins.mobfarmingsupplies.gravitationalanomaly;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Gravitational Anomaly: a small black hole. Its shadow, the black sphere 8 pixels across, is
 * the only solid part; the accretion disk around it and, while it's {@link #ACTIVE}, the
 * relativistic jets from its poles are drawn by {@link GravitationalAnomalyRenderer} and can be
 * walked through. Its block model is empty. The disk lights its surroundings fully.
 *
 * <p>It can't be mined, blown up, pushed or destroyed by withers and the dragon, but its hardness
 * stays positive so a Present, and other mods' block movers that skip unbreakable blocks, can still
 * pick it up. Only creative players can break it.
 */
public class GravitationalAnomalyBlock extends BaseEntityBlock {

    public static final MapCodec<GravitationalAnomalyBlock> CODEC = simpleCodec(GravitationalAnomalyBlock::new);
    /** Whether it's firing its jets. Off unless something turns it on. */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public GravitationalAnomalyBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<GravitationalAnomalyBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    /** No mining progress for anyone but creative players, who break blocks without it. */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return 0f;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravitationalAnomalyBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (!level.isClientSide() || type != Registration.GRAVITATIONAL_ANOMALY_BE_TYPE.get()) return null;
        BlockEntityTicker<GravitationalAnomalyBlockEntity> ticker = GravitationalAnomalyBlockEntity::clientTick;
        return (BlockEntityTicker<T>) ticker;
    }
}
