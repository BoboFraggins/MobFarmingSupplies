package net.bobofraggins.mobfarmingsupplies.experiencepylon;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.ExperienceItemTransfer;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Experience Pylon: a store for experience fluid. The block model is the base
 * ({@code block/experience_pylon_base}); {@link ExperiencePylonRenderer} draws the pylon above it,
 * slowly bobbing. Buckets, bottles and pipes fill and empty it as they would a tank; so do a
 * tank in bucket mode and an Experience Syringe (each empties fully, or fills fully when empty).
 */
public class ExperiencePylonBlock extends BaseEntityBlock {



    // The base plate and rim, then the pylon (which reaches 27 pixels up, plus 1 when bobbing).
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(1, 1, 1, 15, 3, 15),
            Block.box(3, 3, 2, 13, 28, 14));

    public ExperiencePylonBlock(Properties props) {
        super(props);
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExperiencePylonBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Runs the UI's container slots, as a tank's do. */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != Registration.EXPERIENCE_PYLON_BE_TYPE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<TankBlockEntity>) TankBlockEntity::serverTick;
    }

    /** Opens the UI: with an empty hand, or anything the pylon doesn't trade experience with. */
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ExperiencePylonBlockEntity be
                && player instanceof ServerPlayer sp) {
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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

        // Bottles o' enchanting in, empty bottles out (as a tank does them).
        if (stack.is(Items.GLASS_BOTTLE) || stack.is(Items.EXPERIENCE_BOTTLE)) {
            if (!(level.getBlockEntity(pos) instanceof ExperiencePylonBlockEntity be))
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            ItemStack result = TankBottleTransfer.tryTransfer(be, stack);
            if (result == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
            level.playSound(
                    null, pos,
                    stack.is(Items.GLASS_BOTTLE) ? SoundEvents.BOTTLE_FILL : SoundEvents.BOTTLE_EMPTY,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // A tank in bucket mode or a syringe: one that holds experience empties into the pylon,
        // an empty one fills up from it. A tank in block mode is placed as usual.
        if (ExperienceItemTransfer.handles(stack)) {
            if (!(level.getBlockEntity(pos) instanceof ExperiencePylonBlockEntity be))
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            boolean storing = ExperienceItemTransfer.holdsExperience(stack);
            ItemStack result = ExperienceItemTransfer.transfer(be, stack, true);
            if (result == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
            level.playSound(null, pos, storing ? SoundEvents.BUCKET_EMPTY : SoundEvents.BUCKET_FILL,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // Buckets and other fluid containers; the pylon's fluid handler refuses anything but experience.
        boolean filling = stack.is(Items.BUCKET);
        boolean success = TankBlockPlatform.handleFluidItemInteraction(player, hand, level, pos, filling);
        return success ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
