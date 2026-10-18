package net.bobofraggins.mobfarmingsupplies.mobhead;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.mobharvester.HarvesterSword;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Head drops: the Mob Harvester's Beheading Upgrade, and the Guillotine enchantment on a sword,
 * spear or axe. Something killed by the harvester (its fake player wielding a
 * {@link HarvesterSword} with a beheading level), or by a player's melee hit with a Guillotine
 * weapon, drops its head with a chance of {@code level} in 10. Players included: they drop their own head. See
 * {@link MobHeads#headFor}. Common code (Architectury's death event), so it works on both loaders.
 */
public final class BeheadingDrops {

    public static final ResourceKey<Enchantment> GUILLOTINE = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "guillotine"));

    private BeheadingDrops() {}

    public static void register() {
        EntityEvent.LIVING_DEATH.register(BeheadingDrops::onDeath);
    }

    private static EventResult onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level)) return EventResult.pass();
        if (!(source.getEntity() instanceof Player killer)) return EventResult.pass();
        ItemStack weapon = killer.getMainHandItem();
        boolean harvester = weapon.getItem() instanceof HarvesterSword;
        int beheadingLevel = harvester
                ? weapon.getOrDefault(Registration.BEHEADING_LEVEL.get(), 0)
                : guillotineLevel(level, weapon, source, killer);
        if (beheadingLevel <= 0 || level.getRandom().nextInt(10) >= beheadingLevel) return EventResult.pass();
        // Mobs follow the mob-loot rule; players always lose their head.
        if (!(entity instanceof Player) && !level.getGameRules().get(GameRules.MOB_DROPS)) return EventResult.pass();

        ItemStack head = MobHeads.headFor(entity);
        if (!head.isEmpty()) entity.spawnAtLocation(level, head);
        return EventResult.pass();
    }

    /** The Guillotine level of {@code weapon}, if {@code killer} struck the killing blow with it (not, say, an arrow). */
    private static int guillotineLevel(ServerLevel level, ItemStack weapon, DamageSource source, Player killer) {
        if (source.getDirectEntity() != killer) return 0;
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(GUILLOTINE)
                .map(guillotine -> EnchantmentHelper.getItemEnchantmentLevel(guillotine, weapon))
                .orElse(0);
    }
}
