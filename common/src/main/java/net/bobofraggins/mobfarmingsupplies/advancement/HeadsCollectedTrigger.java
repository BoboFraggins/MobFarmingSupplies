package net.bobofraggins.mobfarmingsupplies.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

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

    public static final Identifier TRACKER = Identifier.fromNamespaceAndPath("mobfarmingsupplies", "mob_farming/head_tracker");

    public record Instance(Optional<Holder<LootItemCondition>> player, int min)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.INT.fieldOf("min").forGetter(Instance::min)
        ).apply(i, Instance::new));
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        int collected = collected(player);
        trigger(player, instance -> collected >= instance.min());
    }

    /** How many different heads {@code player} has had. */
    public static int collected(ServerPlayer player) {
        AdvancementHolder tracker = player.level().getServer().getAdvancements().get(TRACKER);
        if (tracker == null) return 0;
        int count = 0;
        for (String ignored : player.getAdvancements().getOrStartProgress(tracker).getCompletedCriteria()) count++;
        return count;
    }
}
