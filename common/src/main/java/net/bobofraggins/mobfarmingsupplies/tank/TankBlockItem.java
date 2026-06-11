package net.bobofraggins.mobfarmingsupplies.tank;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;
import java.util.function.Consumer;

public class TankBlockItem extends BlockItem {

    static final int BUCKET_VOLUME = 1000;

    public TankBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay lines,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, tooltipAdder, flag);
        TankContents contents = stack.getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY);
        if (contents.isLocked()) {
            tooltipAdder.accept(Component.translatable(
                            "item.mobfarmingsupplies.tank.tooltip",
                            contents.amount(),
                            TankBlockEntity.CAPACITY,
                            contents.storedFluid().getName())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (contents.bucketMode()) {
            tooltipAdder.accept(Component.translatable("item.mobfarmingsupplies.tank.mode_bucket")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        TankContents c = stack.getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY);

        BlockHitResult hit = getPlayerPOVHitResult(
                level, player, c.bucketMode() ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);

        if (hit.getType() == HitResult.Type.MISS) {
            if (!level.isClientSide()) {
                boolean newBucketMode = !c.bucketMode();
                stack.set(Registration.TANK_CONTENTS.get(),
                        new TankContents(c.storedFluid(), c.amount(), newBucketMode));
                player.sendOverlayMessage(Component.translatable(newBucketMode
                        ? "item.mobfarmingsupplies.tank.mode_bucket"
                        : "item.mobfarmingsupplies.tank.mode_block"));
            }
            return InteractionResult.SUCCESS;
        }

        if (!c.bucketMode()) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        BlockState state = level.getBlockState(pos);

        // Try picking up a matching fluid source block.
        if (level.getFluidState(pos).isSource() && state.getBlock() instanceof BucketPickup bucketPickup) {
            Fluid worldFluid = level.getFluidState(pos).getType();
            dev.architectury.fluid.FluidStack archWorldFluid =
                    dev.architectury.fluid.FluidStack.create(worldFluid, BUCKET_VOLUME);
            boolean canAccept = c.storedFluid().isEmpty()
                    || (c.storedFluid().isFluidEqual(archWorldFluid)
                            && c.storedFluid().isComponentEqual(archWorldFluid));

            if (canAccept && TankBlockEntity.CAPACITY - c.amount() >= BUCKET_VOLUME) {
                if (!level.isClientSide()) {
                    Optional<SoundEvent> sound = bucketPickup.getPickupSound();
                    ItemStack picked = bucketPickup.pickupBlock(player, level, pos, state);
                    if (!picked.isEmpty()) {
                        dev.architectury.fluid.FluidStack newType = c.storedFluid().isEmpty()
                                ? archWorldFluid
                                : c.storedFluid();
                        stack.set(Registration.TANK_CONTENTS.get(),
                                new TankContents(newType, c.amount() + BUCKET_VOLUME, true));
                        sound.ifPresent(s -> player.playSound(s, 1.0F, 1.0F));
                        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Try placing one bucket of fluid from the tank into the world.
        if (!c.storedFluid().isEmpty() && c.amount() >= BUCKET_VOLUME) {
            Fluid fluid = c.storedFluid().getFluid();
            boolean isLiquidContainer = state.getBlock() instanceof LiquidBlockContainer lbc
                    && lbc.canPlaceLiquid(player, level, pos, state, fluid);
            BlockPos targetPos = isLiquidContainer ? pos : pos.relative(face);

            if (!level.isClientSide()) {
                ItemStack newStack =
                        TankBlockItemPlatform.tryPlaceFluidInWorld(stack, player, level, hand, targetPos);
                if (newStack != null) {
                    player.setItemInHand(hand, newStack);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        TankContents c = stack.getOrDefault(Registration.TANK_CONTENTS.get(), TankContents.EMPTY);

        if (c.bucketMode()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        if (!level.isClientSide()) {
            Player player = context.getPlayer();
            if (player != null) {
                boolean success = TankBlockItemPlatform.interactWithFluidBlock(
                        player, context.getHand(), level,
                        context.getClickedPos(), context.getClickedFace());
                if (success) return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }
}
