package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.shared.TallBlock;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.ExperienceItemTransfer;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockPlatform;
import net.bobofraggins.mobfarmingsupplies.tank.TankBottleTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The Experience Pylon: a store for experience fluid, two blocks tall ({@link TallBlock}). The
 * lower half holds the {@link ExperiencePylonBlockEntity}; its block model is the base
 * ({@code block/experience_pylon_base}) and {@link ExperiencePylonRenderer} draws the pylon above
 * it, slowly bobbing up into the upper half. Buckets, bottles and pipes fill and empty it, at
 * either half, as they would a tank; so do a tank in bucket mode and an Experience Syringe (each
 * empties fully, or fills fully when empty).
 */
public class ExperiencePylonBlock extends BaseEntityBlock {

    public static final MapCodec<ExperiencePylonBlock> CODEC = simpleCodec(ExperiencePylonBlock::new);

    // The base plate and rim, then the pylon, which reaches 27 pixels up (plus 1 when bobbing):
    // the first 13 pixels of it in the lower half, the rest in the upper half.
    private static final VoxelShape LOWER = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(1, 1, 1, 15, 3, 15),
            Block.box(3, 3, 2, 13, 16, 14));
    private static final VoxelShape UPPER = Block.box(3, 0, 2, 13, 12, 14);

    public ExperiencePylonBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(TallBlock.HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public MapCodec<ExperiencePylonBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TallBlock.HALF);
    }

    /** The pylon at either half's position, or null. */
    @Nullable
    public static ExperiencePylonBlockEntity blockEntity(BlockGetter level, BlockPos pos, BlockState state) {
        return TallBlock.lowerBlockEntity(level, pos, state, Registration.EXPERIENCE_PYLON.get(),
                ExperiencePylonBlockEntity.class);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TallBlock.isUpper(state) ? null : new ExperiencePylonBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Two blocks tall ───────────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return TallBlock.roomAbove(context) ? defaultBlockState() : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        TallBlock.placeUpper(level, pos, state);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return TallBlock.canSurvive(state, level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockState updated = TallBlock.updateShape(state, direction, neighborState);
        return updated != state ? updated
                : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        TallBlock.playerWillDestroy(level, pos, state, player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        TallBlock.afterRemoval(state, level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TallBlock.isUpper(state) ? UPPER : LOWER;
    }

    /** Runs the UI's container slots, as a tank's do, and grows a missing upper half. */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != Registration.EXPERIENCE_PYLON_BE_TYPE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<TankBlockEntity>) (lvl, pos, st, be) -> {
            TallBlock.growUpper(lvl, pos, st);
            TankBlockEntity.serverTick(lvl, pos, st, be);
        };
    }

    // ── Use ───────────────────────────────────────────────────────────────────────

    /** Opens the UI: with an empty hand, or anything the pylon doesn't trade experience with. */
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && blockEntity(level, pos, state) instanceof ExperiencePylonBlockEntity be
                && player instanceof ServerPlayer sp) {
            BlockPos lower = be.getBlockPos();
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(lower));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ExperiencePylonBlockEntity be = blockEntity(level, pos, state);
        if (be == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        BlockPos lower = be.getBlockPos();

        // Bottles o' enchanting in, empty bottles out (as a tank does them).
        if (stack.is(Items.GLASS_BOTTLE) || stack.is(Items.EXPERIENCE_BOTTLE)) {
            ItemStack result = TankBottleTransfer.tryTransfer(be, stack);
            if (result == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
            level.playSound(
                    null, lower,
                    stack.is(Items.GLASS_BOTTLE) ? SoundEvents.BOTTLE_FILL : SoundEvents.BOTTLE_EMPTY,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // A tank in bucket mode or a syringe: one that holds experience empties into the pylon,
        // an empty one fills up from it. A tank in block mode is placed as usual.
        if (ExperienceItemTransfer.handles(stack)) {
            boolean storing = ExperienceItemTransfer.holdsExperience(stack);
            ItemStack result = ExperienceItemTransfer.transfer(be, stack, true);
            if (result == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
            level.playSound(null, lower, storing ? SoundEvents.BUCKET_EMPTY : SoundEvents.BUCKET_FILL,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // Buckets and other fluid containers; the pylon's fluid handler refuses anything but experience.
        boolean filling = stack.is(Items.BUCKET);
        boolean success = TankBlockPlatform.handleFluidItemInteraction(player, hand, level, lower, filling);
        return success ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
