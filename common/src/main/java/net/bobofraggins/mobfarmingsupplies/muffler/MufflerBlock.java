package net.bobofraggins.mobfarmingsupplies.muffler;

import net.minecraft.world.InteractionHand;
import net.bobofraggins.mobfarmingsupplies.shared.compat.BlockInteractions;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Muffler: a small cube of light grey wool, the size of a hopper's core, that silences every
 * sound made in its area (see {@link MufflerSounds}). Its area is the Absorption Hopper's 7-block
 * cube, which can be moved up to 8 blocks each way; its settings screen is the Ender Inhibitor's.
 */
public class MufflerBlock extends Block implements EntityBlock {


    private static final VoxelShape SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public MufflerBlock(Properties props) {
        super(props);
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MufflerBlockEntity(pos, state);
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        return BlockInteractions.use(state, level, pos, player, hand, hit, null,
                this::useWithoutItem);
    }


        public InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MufflerBlockEntity be
                && player instanceof ServerPlayer sp) {
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }
}
