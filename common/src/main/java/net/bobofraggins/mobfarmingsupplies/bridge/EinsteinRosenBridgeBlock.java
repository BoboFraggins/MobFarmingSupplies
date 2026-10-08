package net.bobofraggins.mobfarmingsupplies.bridge;

import net.bobofraggins.mobfarmingsupplies.shared.menu.ExtendedMenus;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlock;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Einstein-Rosen Bridge — an Omnidirectional Hopper linked to every other bridge on its channel
 * (see {@link EinsteinRosenBridgeBlockEntity}). Uses the hopper's side properties, so the block
 * state mirrors each side's {@link HopperSide} the same way.
 */
public class EinsteinRosenBridgeBlock extends BaseEntityBlock {

    private static final Map<Direction, EnumProperty<HopperSide>> SIDES = OmniHopperBlock.SIDES;

    public EinsteinRosenBridgeBlock(BlockBehaviour.Properties props) {
        super(props);
        BlockState state = this.stateDefinition.any();
        for (EnumProperty<HopperSide> p : SIDES.values()) state = state.setValue(p, HopperSide.NONE);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        SIDES.values().forEach(builder::add);
    }

    // ── Drops — keep the channel and side configuration on the item ───────────

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof EinsteinRosenBridgeBlockEntity bridge) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem) {
                    TagValueOutput beOut = TagValueOutput.createWithContext(
                            ProblemReporter.DISCARDING, params.getLevel().registryAccess());
                    bridge.saveCustomOnly(beOut);
                    BlockItem.setBlockEntityData(drop, bridge.getType(), beOut);
                    if (bridge.getChannel() != 0) drop.set(Registration.BRIDGE_CHANNEL.get(), bridge.getChannel());
                }
            }
        }
        return drops;
    }

    // ── Pick block (and Jade's name) — show the channel ─────────────────────────

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        if (level.getBlockEntity(pos) instanceof EinsteinRosenBridgeBlockEntity bridge && bridge.getChannel() != 0) {
            stack.set(Registration.BRIDGE_CHANNEL.get(), bridge.getChannel());
        }
        return stack;
    }

    // ── Portal particles ────────────────────────────────────────────────────────

    /**
     * Nether portal particles all around the block. Vanilla's portal particle travels from its start
     * point plus its velocity back to its start point, so starting each one on a face with an
     * outward velocity draws it in toward the block, from every side.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 4; i++) {
            Direction face = Direction.values()[random.nextInt(6)];
            double x = pos.getX() + 0.5 + face.getStepX() * 0.5 + (face.getStepX() == 0 ? random.nextDouble() - 0.5 : 0);
            double y = pos.getY() + 0.5 + face.getStepY() * 0.5 + (face.getStepY() == 0 ? random.nextDouble() - 0.5 : 0);
            double z = pos.getZ() + 0.5 + face.getStepZ() * 0.5 + (face.getStepZ() == 0 ? random.nextDouble() - 0.5 : 0);
            double out = random.nextFloat() * 0.8;
            level.addParticle(ParticleTypes.PORTAL, x, y, z,
                    face.getStepX() * out + (random.nextFloat() - 0.5) * 0.2,
                    face.getStepY() * out + (random.nextFloat() - 0.5) * 0.2,
                    face.getStepZ() * out + (random.nextFloat() - 0.5) * 0.2);
        }
    }

    // ── Block entity ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EinsteinRosenBridgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> beType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(beType, Registration.BRIDGE_BE_TYPE.get(), EinsteinRosenBridgeBlockEntity::serverTick);
    }

    // ── Interaction ─────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()
                && level.getBlockEntity(pos) instanceof EinsteinRosenBridgeBlockEntity be
                && player instanceof ServerPlayer sp) {
            ExtendedMenus.openAt(sp, be, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
