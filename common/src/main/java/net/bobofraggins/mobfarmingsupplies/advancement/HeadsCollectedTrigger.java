package net.bobofraggins.mobfarmingsupplies.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/**
 * Matches once a player has collected at least {@code min} different heads.
 *
 * <p>Which heads a player has had is kept by a hidden advancement,
 * {@code mobfarmingsupplies:mob_farming/head_tracker}: one {@code inventory_changed} criterion per
 * head, so the game itself saves the set. This trigger counts its completed criteria. It's fired
 * whenever a head enters a player's inventory, just after vanilla has updated the tracker
 * (see {@code InventoryChangeTriggerMixin}).
 */
public class HeadsCollectedTrigger extends SimpleCriterionTrigger<HeadsCollectedTrigger.Instance> {

    public static final ResourceLocation TRACKER = new ResourceLocation("mobfarmingsupplies", "mob_farming/head_tracker");

    private final ResourceLocation id;

    public HeadsCollectedTrigger(ResourceLocation id) {
        this.id = id;
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final int min;

        public Instance(ResourceLocation id, ContextAwarePredicate player, int min) {
            super(id, player);
            this.min = min;
        }

        public int min() {
            return min;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("min", min);
            return json;
        }
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new Instance(id, player, GsonHelper.getAsInt(json, "min"));
    }

    public void trigger(ServerPlayer player) {
        int collected = collected(player);
        trigger(player, instance -> collected >= instance.min());
    }

    /** How many different heads {@code player} has had. */
    public static int collected(ServerPlayer player) {
        Advancement tracker = player.level().getServer().getAdvancements().getAdvancement(TRACKER);
        if (tracker == null) return 0;
        int count = 0;
        for (String ignored : player.getAdvancements().getOrStartProgress(tracker).getCompletedCriteria()) count++;
        return count;
    }
}
