package net.bobofraggins.mobfarmingsupplies.glamping.magichat;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * Wires the Magic Hat's mob-capture logic into {@link InteractionEvent#INTERACT_ENTITY}, which
 * fires before the target entity's own {@code interact()}/{@code mobInteract()}.
 *
 * <p>A plain {@code Item#interactLivingEntity} override is not enough — mirrors
 * {@link net.bobofraggins.mobfarmingsupplies.dna.DnaCollectorEvents}, which hit the exact same
 * problem first: vanilla's {@code Player#interactOn} calls {@code Entity#interact} <em>first</em>,
 * and ridable/tameable mobs (horses, donkeys, mules, llamas, camels, pigs, striders, ...) can
 * consume that call before the held item is ever consulted — so {@code interactLivingEntity}
 * doesn't reliably fire for them. (Originally the Magic Hat used only
 * {@code interactLivingEntity}, ported straight from a single-loader source mod that never hit
 * this; it worked by chance on NeoForge for whatever mob was tested there, but failed on Fabric.)
 */
public final class MagicHatCaptureEvents {

    private MagicHatCaptureEvents() {}

    public static void registerCommonEvents() {
        InteractionEvent.INTERACT_ENTITY.register(MagicHatCaptureEvents::onInteractEntity);
    }

    private static EventResult onInteractEntity(Player player, Entity entity, InteractionHand hand) {
        if (!(entity instanceof LivingEntity target) || target instanceof Player || !target.isAlive()) {
            return EventResult.pass();
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof MagicHatItem) || MagicHatItem.hasMob(stack)) {
            return EventResult.pass();
        }

        // Visual feedback is sufficient on the client; all logic runs server-side.
        if (player.level().isClientSide()) {
            return EventResult.interruptTrue();
        }

        TagValueOutput tagOut = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, player.level().registryAccess());
        if (!target.save(tagOut)) {
            return EventResult.pass();
        }
        CompoundTag mobTag = tagOut.buildResult();
        CompoundTag wrapper = new CompoundTag();
        wrapper.put(MagicHatItem.MOB_KEY, mobTag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(wrapper));
        player.setItemInHand(hand, stack);
        target.discard();
        player.level().playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.ILLUSIONER_CAST_SPELL,
                SoundSource.NEUTRAL,
                1.0f,
                1.0f);

        return EventResult.interruptTrue();
    }
}
