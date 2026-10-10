package net.bobofraggins.mobfarmingsupplies.glamping.present;

import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Present block — right-click any single-position block with a Present item to wrap it,
 * hiding it underneath a present model; shift-right-click the present to unwrap it back in
 * place. Wrapped block + block-entity state is carried on the item form via the vanilla
 * {@code minecraft:block_entity_data} component (see {@link #getDrops} /
 * {@link #getCloneItemStack}), the same idiom {@code PicnicBasketBlock} uses — placement
 * restores it automatically via {@link BlockItem}'s own generic block-entity-data handling,
 * no custom placement code needed.
 */
public class PresentBlock extends BaseEntityBlock {

    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /**
     * An enderman's Present, contents not yet rolled (see {@link PresentEndermanHandler}). Only
     * ever carried by an enderman, or for the one tick after it sets the Present down. Has a real
     * blockstate model, since an enderman draws its carried block from the blockstate model.
     */
    public static final BooleanProperty SURPRISE = BooleanProperty.create("surprise");

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public PresentBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SURPRISE, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SURPRISE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PresentBlockEntity(pos, state);
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    // -------------------------------------------------------------------------
    // Unwrapping — shift-right-click to restore wrapped block in place
    // -------------------------------------------------------------------------

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isCrouching()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        return unwrap(level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Server side: restores the wrapped block at {@code pos} and gives the player an empty
     * Present. Returns false (and changes nothing) if there's no wrapped Present there.
     *
     * <p>Also called from {@link PresentWrapEvents}: vanilla skips {@link #useWithoutItem}
     * entirely when a sneaking player holds anything in either hand, so an offhand torch or
     * shield would otherwise make unwrapping silently do nothing.
     */
    static boolean unwrap(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof PresentBlockEntity present)) return false;
        if (!present.hasWrappedBlock()) return false;

        BlockState wrapped = present.getWrappedState();
        CompoundTag entityData = present.getWrappedEntityData();

        // UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS: the Present's own block entity isn't a
        // Container so this is a no-op today, but skip vanilla's automatic side effects here
        // too since we're manually transplanting the wrapped block-entity data ourselves right
        // below — matches the wrap side in PresentWrapEvents for the same reason.
        level.setBlock(pos, wrapped,
                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS);

        if (entityData != null) {
            BlockEntity newBe = level.getBlockEntity(pos);
            if (newBe != null) {
                newBe.loadCustomOnly(
                        TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), entityData));
                newBe.setChanged();
            }
        }

        ItemStack presentItem = new ItemStack(Registration.PRESENT_ITEM.get());
        // Unwrapping requires an empty main hand — put it there directly.
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, presentItem);
        } else if (!player.addItem(presentItem)) {
            Block.popResource(level, pos, presentItem);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Drops — carry wrapped data on the item when broken
    // -------------------------------------------------------------------------

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        PresentBlockEntity present = be instanceof PresentBlockEntity p && p.hasWrappedBlock() ? p : null;
        if (present == null && state.getValue(SURPRISE)) {
            // An enderman's Present (dropped on death, so no block entity): roll its contents now.
            present = new PresentBlockEntity(BlockPos.ZERO, state);
            present.setWrappedBlock(PresentEndermanHandler.rollContents(params.getLevel().getRandom()), null);
        }
        if (present != null) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem) {
                    TagValueOutput beOut = TagValueOutput.createWithContext(
                            ProblemReporter.DISCARDING, params.getLevel().registryAccess());
                    present.saveCustomOnly(beOut);
                    BlockItem.setBlockEntityData(drop, present.getType(), beOut);
                }
            }
        }
        return drops;
    }

    // -------------------------------------------------------------------------
    // Enderman set it down — roll its contents
    // -------------------------------------------------------------------------

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // Next tick: the block entity may not exist yet while the block is being placed.
        if (state.getValue(SURPRISE) && !level.isClientSide()) level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(SURPRISE)) return;
        if (level.getBlockEntity(pos) instanceof PresentBlockEntity present && !present.hasWrappedBlock()) {
            present.setWrappedBlock(PresentEndermanHandler.rollContents(random), null);
        }
        level.setBlock(pos, state.setValue(SURPRISE, false), Block.UPDATE_ALL);
    }

    // -------------------------------------------------------------------------
    // Middle-click — give item with wrapped data
    // -------------------------------------------------------------------------

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean creative) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, creative);
        if (level.getBlockEntity(pos) instanceof PresentBlockEntity present && present.hasWrappedBlock()) {
            TagValueOutput beOut =
                    TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            present.saveCustomOnly(beOut);
            BlockItem.setBlockEntityData(stack, present.getType(), beOut);
        }
        return stack;
    }

    // -------------------------------------------------------------------------
    // Always drop item even when destroyed by explosion
    // -------------------------------------------------------------------------

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return true;
    }
}
