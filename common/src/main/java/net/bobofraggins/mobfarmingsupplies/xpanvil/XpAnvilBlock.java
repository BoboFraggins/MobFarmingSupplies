package net.bobofraggins.mobfarmingsupplies.xpanvil;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.PylonExperience;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.ExperienceFill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The XP Powered Anvil: repairs what's put in it with experience from its tank. Right-click with
 * experience to fill it ({@link ExperienceFill}); with anything else, to open it. It lies across
 * the way its placer was looking, as a vanilla anvil does.
 *
 * <p>It falls like a vanilla anvil, hurting what it lands on, and takes its tank and slots with it.
 * It isn't in {@code #minecraft:anvil}: a falling anvil in that tag sometimes wears down to a
 * chipped or damaged anvil, and this one has none, so vanilla would destroy it instead.
 */
public class XpAnvilBlock extends BaseEntityBlock implements Fallable {

    /** Points of experience a millibucket of it is worth, as the syringe counts it, for spilling orbs. */
    private static final long MB_PER_XP = PylonExperience.MB_PER_XP;

    public static final MapCodec<XpAnvilBlock> CODEC = simpleCodec(XpAnvilBlock::new);

    /** The way its placer was looking. The model's long side runs east-west when this is north. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Base, waist, neck and head, with the long side east-west.
    private static final VoxelShape SHAPE_X = Shapes.or(
            Block.box(2, 0, 2, 14, 4, 14),
            Block.box(3, 4, 4, 13, 5, 12),
            Block.box(4, 5, 6, 12, 10, 10),
            Block.box(0, 10, 3, 16, 16, 13));
    // The same turned a quarter, with the long side north-south.
    private static final VoxelShape SHAPE_Z = Shapes.or(
            Block.box(2, 0, 2, 14, 4, 14),
            Block.box(4, 4, 3, 12, 5, 13),
            Block.box(6, 5, 4, 10, 10, 12),
            Block.box(3, 10, 0, 13, 16, 16));

    public XpAnvilBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<XpAnvilBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // ── Falling, as a vanilla anvil does ─────────────────────────────────────

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 2);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        level.scheduleTick(pos, this, 2);
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!FallingBlock.isFree(level.getBlockState(pos.below())) || pos.getY() < level.getMinBuildHeight()) return;
        // Take the tank and slots along, and don't spill them as the block leaves.
        CompoundTag carried = null;
        if (level.getBlockEntity(pos) instanceof XpAnvilBlockEntity be) {
            carried = be.saveWithoutMetadata(level.registryAccess());
            be.startFalling();
        }
        FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
        falling.blockData = carried;
        falling.setHurtsEntities(2.0f, 40);
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaced, FallingBlockEntity falling) {
        if (!falling.isSilent()) level.levelEvent(LevelEvent.SOUND_ANVIL_LAND, pos, 0);
    }

    /**
     * It couldn't land (on a torch, say) and broke: what it held spills out, the tools as items
     * and the experience as orbs, since the anvil item vanilla drops can't carry them.
     */
    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity falling) {
        if (!falling.isSilent()) level.levelEvent(LevelEvent.SOUND_ANVIL_BROKEN, pos, 0);
        if (!(level instanceof ServerLevel server) || falling.blockData == null) return;
        falling.blockData.getList("Items", Tag.TAG_COMPOUND).forEach(tag ->
                ItemStack.OPTIONAL_CODEC.parse(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag)
                        .result().filter(stack -> !stack.isEmpty())
                        .ifPresent(stack -> Containers.dropItemStack(server, pos.getX(), pos.getY(), pos.getZ(), stack)));
        long mb = falling.blockData.getLong("Amount");
        if (mb >= MB_PER_XP) ExperienceOrb.award(server, Vec3.atCenterOf(pos), (int) Math.min(mb / MB_PER_XP, Integer.MAX_VALUE));
    }

    @Override
    public DamageSource getFallDamageSource(Entity entity) {
        return entity.damageSources().anvil(entity);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new XpAnvilBlockEntity(pos, state);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != Registration.XP_ANVIL_BE_TYPE.get()) return null;
        BlockEntityTicker<XpAnvilBlockEntity> ticker = XpAnvilBlockEntity::serverTick;
        return (BlockEntityTicker<T>) ticker;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof XpAnvilBlockEntity be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return ExperienceFill.useItemOn(be, stack, level, pos, player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof XpAnvilBlockEntity be
                && player instanceof ServerPlayer sp) {
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof XpAnvilBlockEntity be) {
            be.preRemoveSideEffects(pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
