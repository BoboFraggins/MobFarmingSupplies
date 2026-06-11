package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.MapCodec;
import dev.architectury.fluid.FluidStack;
import dev.architectury.registry.menu.MenuRegistry;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TankBlock extends BaseEntityBlock {

    public static final MapCodec<TankBlock> CODEC = simpleCodec(TankBlock::new);

    private static final int BOTTLE_MB = 250;
    private static final TagKey<Fluid> EXPERIENCE_TAG =
            TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "experience"));

    public TankBlock(Properties props) {
        super(props);
    }

    @Override
    public MapCodec<TankBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != Registration.TANK_BE_TYPE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<TankBlockEntity>) TankBlockEntity::serverTick;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TankBlockEntity be
                && player instanceof ServerPlayer sp) {
            MenuRegistry.openExtendedMenu(sp, be, buf -> buf.writeBlockPos(pos));
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

        // Empty glass bottle → water bottle
        if (stack.is(Items.GLASS_BOTTLE)) {
            if (!(level.getBlockEntity(pos) instanceof TankBlockEntity be))
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            FluidStack simulated = be.extract(BOTTLE_MB, true);
            if (simulated.getAmount() >= BOTTLE_MB && simulated.getRawFluid() == Fluids.WATER) {
                be.extract(BOTTLE_MB, false);
                player.setItemInHand(hand, ItemUtils.createFilledResult(
                        stack, player, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        // Water bottle → fill tank with water
        if (stack.is(Items.POTION)) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null && contents.is(Potions.WATER)) {
                if (!(level.getBlockEntity(pos) instanceof TankBlockEntity be))
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                FluidStack water = FluidStack.create(Fluids.WATER, BOTTLE_MB);
                long inserted = be.insert(water, BOTTLE_MB, true);
                if (inserted >= BOTTLE_MB) {
                    be.insert(water, BOTTLE_MB, false);
                    player.setItemInHand(hand, ItemUtils.createFilledResult(
                            stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        // XP bottle → fill tank with experience fluid
        if (stack.is(Items.EXPERIENCE_BOTTLE)) {
            if (!(level.getBlockEntity(pos) instanceof TankBlockEntity be))
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            FluidStack locked = be.getStoredFluid();
            if (!locked.isEmpty() && locked.getFluid().builtInRegistryHolder().is(EXPERIENCE_TAG)) {
                FluidStack xp = FluidStack.create(locked.getFluid(), BOTTLE_MB);
                long inserted = be.insert(xp, BOTTLE_MB, true);
                if (inserted >= BOTTLE_MB) {
                    be.insert(xp, BOTTLE_MB, false);
                    player.setItemInHand(hand, ItemUtils.createFilledResult(
                            stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        // Bucket / modded fluid container — delegate to platform
        boolean filling;
        if (stack.is(Items.BUCKET)) {
            filling = true;
        } else if (stack.getItem() instanceof TankBlockItem) {
            TankContents tankContents = stack.getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY);
            filling = tankContents.amount() == 0;
        } else {
            filling = false;
        }

        boolean success = TankBlockPlatform.handleFluidItemInteraction(player, hand, level, pos, filling);
        return success ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
