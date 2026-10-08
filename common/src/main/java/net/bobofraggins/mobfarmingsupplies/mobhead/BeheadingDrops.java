package net.bobofraggins.mobfarmingsupplies.mobhead;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.bobofraggins.mobfarmingsupplies.mobharvester.HarvesterSword;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * The Mob Harvester's Beheading Upgrade: something killed by the harvester (its fake player
 * wielding a {@link HarvesterSword} with a beheading level) drops its head, with a chance of
 * {@code level} in 10. Players included — they drop their own head. See {@link MobHeads#headFor}.
 * Common code (Architectury's death event), so it works on both loaders.
 */
public final class BeheadingDrops {

    private BeheadingDrops() {}

    public static void register() {
        EntityEvent.LIVING_DEATH.register(BeheadingDrops::onDeath);
    }

    private static EventResult onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level)) return EventResult.pass();
        if (!(source.getEntity() instanceof Player killer)) return EventResult.pass();
        ItemStack weapon = killer.getMainHandItem();
        if (!(weapon.getItem() instanceof HarvesterSword)) return EventResult.pass();
        int beheadingLevel = weapon.getOrDefault(Registration.BEHEADING_LEVEL.get(), 0);
        if (beheadingLevel <= 0 || level.getRandom().nextInt(10) >= beheadingLevel) return EventResult.pass();
        // Mobs follow the mob-loot rule; players always lose their head.
        if (!(entity instanceof Player) && !level.getGameRules().get(GameRules.MOB_DROPS)) return EventResult.pass();

        ItemStack head = MobHeads.headFor(entity);
        if (!head.isEmpty()) entity.spawnAtLocation(level, head);
        return EventResult.pass();
    }
}
