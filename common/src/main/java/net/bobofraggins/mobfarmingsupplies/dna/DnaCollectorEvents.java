package net.bobofraggins.mobfarmingsupplies.dna;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.bobofraggins.mobfarmingsupplies.register.MGRTags;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * Wires the DNA Collector's sampling logic into {@link InteractionEvent#INTERACT_ENTITY},
 * which fires before the target entity's own {@code interact()}/{@code mobInteract()}.
 *
 * <p>{@link Item#interactLivingEntity} alone is not enough: vanilla's
 * {@code Player#interactOn} calls {@code Entity#interact} <em>first</em>, and
 * ridable/tameable mobs (horses, donkeys, mules, llamas, camels, pigs, striders, ...)
 * consume that call for taming/feeding/mounting/inventory before the held item is ever
 * consulted — so {@code interactLivingEntity} never runs for them.
 */
public final class DnaCollectorEvents {

    private DnaCollectorEvents() {}

    public static void registerCommonEvents() {
        InteractionEvent.INTERACT_ENTITY.register(DnaCollectorEvents::onInteractEntity);
    }

    @SuppressWarnings("deprecation") // builtInRegistryHolder(): no direct EntityType.is(TagKey) in 26.1.2
    private static EventResult onInteractEntity(Player player, Entity entity, InteractionHand hand) {
        if (!(entity instanceof LivingEntity target) || target instanceof Player) {
            return EventResult.pass();
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof DnaCollectorItem)) {
            return EventResult.pass();
        }

        if (target.getType().builtInRegistryHolder().is(MGRTags.EntityTypes.NO_DNA_SAMPLING)) {
            return EventResult.pass();
        }

        // Visual feedback is sufficient on the client; all logic runs server-side.
        if (player.level().isClientSide()) {
            return EventResult.interruptTrue();
        }

        // ── Serialize entity ──────────────────────────────────────────────────────
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, player.level().registryAccess());
        target.save(output);
        CompoundTag entityNbt = output.buildResult();

        // ── Compute display name ──────────────────────────────────────────────────
        String mobName = target.hasCustomName()
                ? target.getCustomName().getString()
                : target.getType().getDescription().getString();

        // ── Build sample item ─────────────────────────────────────────────────────
        ItemStack sampleStack = new ItemStack(Registration.DNA_SAMPLE.get());
        sampleStack.set(Registration.DNA_SAMPLE_CONTENTS.get(),
                new DnaSampleContents(entityNbt, mobName));

        // Give to player (auto-stacks into existing slots; overflow is dropped).
        player.getInventory().placeItemBackInInventory(sampleStack);

        // Consume one collector from the stack.
        stack.consume(1, player);

        return EventResult.interruptTrue();
    }
}
