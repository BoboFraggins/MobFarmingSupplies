package net.bobofraggins.mobfarmingsupplies.disenchanter;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.ExperienceFill;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The XP Powered Disenchanter: an enchanting table, its diamonds turned to XP Juice, that pulls
 * enchantments off items onto books with experience from its tank. Right-click with experience
 * to fill it ({@link ExperienceFill}); with anything else, to open it. Its book floats and turns
 * as an enchanting table's does ({@link DisenchanterRenderer}).
 */
public class DisenchanterBlock extends BaseEntityBlock {

    public static final MapCodec<DisenchanterBlock> CODEC = simpleCodec(DisenchanterBlock::new);

    /** An enchanting table's shape: 12 pixels tall. */
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public DisenchanterBlock(Properties props) {
        super(props);
    }

    @Override
    public MapCodec<DisenchanterBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DisenchanterBlockEntity(pos, state);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (type != Registration.DISENCHANTER_BE_TYPE.get()) return null;
        BlockEntityTicker<DisenchanterBlockEntity> ticker = level.isClientSide()
                ? DisenchanterBlockEntity::clientTick : DisenchanterBlockEntity::serverTick;
        return (BlockEntityTicker<T>) ticker;
    }

    /**
     * While it works, XP Juice green runes fly in to the book as an enchanting table's do from its
     * bookshelves, from the same places and as often, though the disenchanter needs no bookshelves.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof DisenchanterBlockEntity be) || !be.isWorking()) return;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) != 0) continue;
            level.addParticle(Registration.XP_RUNE.get(),
                    pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5,
                    offset.getX() + random.nextFloat() - 0.5,
                    offset.getY() - random.nextFloat() - 1.0,
                    offset.getZ() + random.nextFloat() - 0.5);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof DisenchanterBlockEntity be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return ExperienceFill.useItemOn(be, stack, level, pos, player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DisenchanterBlockEntity be
                && player instanceof ServerPlayer sp) {
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof DisenchanterBlockEntity be) {
            be.preRemoveSideEffects(pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
