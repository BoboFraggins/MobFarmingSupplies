package net.bobofraggins.mobfarmingsupplies.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

/**
 * A criterion trigger for one of the mod's own events (a bridge delivering across dimensions, a
 * Present being wrapped, ...). The event can carry a {@code variant} string, such as the button
 * pressed or the item flushed; a criterion with a {@code variant} only matches that one, and one
 * without matches them all:
 * <pre>{"trigger": "mobfarmingsupplies:syringe_used", "conditions": {"variant": "store"}}</pre>
 */
public class EventTrigger extends SimpleCriterionTrigger<EventTrigger.Instance> {

    public record Instance(Optional<ContextAwarePredicate> player, Optional<String> variant)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.optionalFieldOf("variant").forGetter(Instance::variant)
        ).apply(i, Instance::new));
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, "");
    }

    public void trigger(ServerPlayer player, String variant) {
        trigger(player, instance -> instance.variant().map(variant::equals).orElse(true));
    }
}
