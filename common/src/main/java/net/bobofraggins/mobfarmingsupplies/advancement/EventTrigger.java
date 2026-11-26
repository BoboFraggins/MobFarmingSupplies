package net.bobofraggins.mobfarmingsupplies.advancement;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/**
 * A criterion trigger for one of the mod's own events (a bridge delivering across dimensions, a
 * Present being wrapped, ...). The event can carry a {@code variant} string, such as the button
 * pressed or the item flushed; a criterion with a {@code variant} only matches that one, and one
 * without matches them all:
 * <pre>{"trigger": "mobfarmingsupplies:syringe_used", "conditions": {"variant": "store"}}</pre>
 */
public class EventTrigger extends SimpleCriterionTrigger<EventTrigger.Instance> {

    private final ResourceLocation id;

    public EventTrigger(ResourceLocation id) {
        this.id = id;
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final Optional<String> variant;

        public Instance(ResourceLocation id, ContextAwarePredicate player, Optional<String> variant) {
            super(id, player);
            this.variant = variant;
        }

        public Optional<String> variant() {
            return variant;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            variant.ifPresent(v -> json.addProperty("variant", v));
            return json;
        }
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new Instance(id, player, json.has("variant")
                ? Optional.of(GsonHelper.getAsString(json, "variant")) : Optional.empty());
    }

    public void trigger(ServerPlayer player) {
        trigger(player, "");
    }

    public void trigger(ServerPlayer player, String variant) {
        trigger(player, instance -> instance.variant().map(variant::equals).orElse(true));
    }
}
