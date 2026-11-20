package net.bobofraggins.mobfarmingsupplies.essencehopper;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Using a glass bottle on any block with a fluid inventory that holds an essence fills it with
 * that essence; using a bottle of essence on one pours it in. Hooked on
 * {@link InteractionEvent#RIGHT_CLICK_BLOCK}, which runs before the block's own reaction on both
 * loaders, so it works on other mods' tanks as well as ours. Anything else passes through.
 */
public final class EssenceBottleEvents {

    private EssenceBottleEvents() {}

    public static void registerCommonEvents() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register(EssenceBottleEvents::onRightClickBlock);
    }

    private static EventResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        ItemStack stack = player.getItemInHand(hand);
        boolean empty = stack.is(Items.GLASS_BOTTLE);
        if (!empty && !(stack.getItem() instanceof EssenceBottleItem)) return EventResult.pass();
        // The client can't see what's in the block; it passes the click on and the server decides.
        Level level = player.level();
        if (level.isClientSide()) return EventResult.pass();

        ItemStack result;
        if (empty) {
            Essence essence = EssenceBottlePlatform.extractBottle(level, pos, face);
            if (essence == null) return EventResult.pass();
            result = new ItemStack(Registration.ESSENCE_BOTTLES.get(essence).get());
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
        } else {
            Essence essence = ((EssenceBottleItem) stack.getItem()).essence();
            if (!EssenceBottlePlatform.insertBottle(level, pos, face, essence)) return EventResult.pass();
            result = new ItemStack(Items.GLASS_BOTTLE);
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
        }
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
        return EventResult.interruptTrue();
    }
}
