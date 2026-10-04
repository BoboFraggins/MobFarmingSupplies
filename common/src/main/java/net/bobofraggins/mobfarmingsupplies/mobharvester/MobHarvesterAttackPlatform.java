package net.bobofraggins.mobfarmingsupplies.mobharvester;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MobHarvesterAttackPlatform {

    /** Set once creating a fake player has failed; from then on harvesters use {@link #hurtWithoutPlayer}. */
    private static boolean fakePlayerUnavailable = false;

    private MobHarvesterAttackPlatform() {}

    /**
     * Attacks all targets in the kill zone using a fake player wielding the given sword, so
     * kills are player-attributed (Looting, XP, player-kill drops).
     *
     * <p>{@code reuse} is the fake player returned by the previous call for this harvester (or
     * {@code null}); it is reused when still valid, otherwise a new one is made. Returns the fake
     * player used, which the caller keeps for next time - or {@code null} if no fake player could
     * be made, in which case the targets were hurt by {@link #hurtWithoutPlayer} instead.
     */
    @ExpectPlatform
    @Nullable
    public static ServerPlayer attackTargets(ServerLevel level, BlockPos pos, ItemStack sword,
                                             List<LivingEntity> targets, @Nullable ServerPlayer reuse) {
        throw new AssertionError("Missing platform implementation");
    }

    /** Whether an earlier attempt to create a fake player failed (see {@link #fakePlayerFailed}). */
    public static boolean isFakePlayerUnavailable() {
        return fakePlayerUnavailable;
    }

    /**
     * Records that creating a fake player threw, and logs it once. Another mod hooking player
     * creation can assume every player has a real network connection - a fake player's has no
     * channel, so that mod throws (e.g. an NPE on {@code Connection.channel().attr(...)}). Rather
     * than crash the server, harvesters fall back to {@link #hurtWithoutPlayer}; the logged stack
     * trace names the mod responsible.
     */
    public static void fakePlayerFailed(RuntimeException e) {
        if (fakePlayerUnavailable) return;
        fakePlayerUnavailable = true;
        MobFarmingSuppliesCommon.LOGGER.error("Mob Harvester: couldn't create its fake player, most likely because another "
                + "mod expects every player to have a network connection (see the stack trace for which). Harvesters "
                + "will deal plain damage instead, so their kills won't count as player kills (no Looting, player-only "
                + "drops or beheading).", e);
    }

    /**
     * Gives the fake player the sword's attribute modifiers (its attack damage). A real player picks
     * these up in its tick when the held item changes, but a fake player never ticks, so without this
     * every hit dealt bare-hand damage. Safe to repeat: modifiers are keyed by id.
     */
    public static void applySwordAttributes(ServerPlayer fakePlayer, ItemStack sword) {
        sword.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = fakePlayer.getAttribute(attribute);
            if (instance != null) instance.addOrUpdateTransientModifier(modifier);
        });
    }

    /**
     * Fallback when no fake player is available: hurts each target directly with the sword's
     * attack damage (including Sharpness), using a non-player damage source. That source is a
     * mob attack with no attacker - unlike {@code generic()} it respects armor, as a real hit does.
     */
    public static void hurtWithoutPlayer(ServerLevel level, ItemStack sword, List<LivingEntity> targets) {
        // A player's base attack damage is 1.0; the sword's modifiers add to it, as for a real hit.
        double base = sword.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
                .compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND);
        DamageSource source = new DamageSource(level.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MOB_ATTACK_NO_AGGRO));
        for (LivingEntity target : targets) {
            float damage = EnchantmentHelper.modifyDamage(level, sword, target, source, (float) base);
            target.hurtServer(level, source, damage);
        }
    }
}
