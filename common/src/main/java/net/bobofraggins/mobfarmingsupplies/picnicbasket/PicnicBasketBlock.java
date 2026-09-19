package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Picnic Basket — a portable 54-slot storage container that acts like a double chest,
 * with an animated split-lid model and an auto-feed handler (see {@link PicnicBasketBlockEntity}).
 */
public class PicnicBasketBlock extends BaseEntityBlock {

    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(3, 0, 4, 13, 10, 12);

    public PicnicBasketBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    // ── Block entity ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PicnicBasketBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> beType) {
        return createTickerHelper(beType, Registration.PICNIC_BASKET_BE_TYPE.get(),
                level.isClientSide() ? PicnicBasketBlockEntity::clientTick : PicnicBasketBlockEntity::serverTick);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    // ── Interaction ─────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof PicnicBasketBlockEntity be
                    && player instanceof ServerPlayer sp) {
                if (player.isShiftKeyDown()) {
                    boolean newValue = !be.isAutoFeed();
                    be.setAutoFeed(newValue);
                    sp.sendOverlayMessage(Component.translatable(newValue
                            ? "message.mobfarmingsupplies.picnic_basket.autofeed_on"
                            : "message.mobfarmingsupplies.picnic_basket.autofeed_off"));
                } else {
                    be.startOpen(player);
                    MenuRegistry.openExtendedMenu(sp, be, buf -> {
                        buf.writeBoolean(true);
                        buf.writeBlockPos(pos);
                    });
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    // ── Drops ───────────────────────────────────────────────────────────────────

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof PicnicBasketBlockEntity basket) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem) {
                    TagValueOutput beOut = TagValueOutput.createWithContext(
                            ProblemReporter.DISCARDING,
                            params.getLevel().registryAccess());
                    basket.saveCustomOnly(beOut);
                    BlockItem.setBlockEntityData(drop, basket.getType(), beOut);
                }
            }
        }
        return drops;
    }
}
