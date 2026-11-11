package net.bobofraggins.mobfarmingsupplies.runestone;

import dev.architectury.event.events.common.TickEvent;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketAccessoryAvailability;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * What worn runestones do. The effects go through vanilla's own immunity checks (see the
 * {@code Runestone*Mixin} classes), so each behaves like the matching potion effect or mob trait:
 * <ul>
 *   <li>Fire Protection makes the wearer fire-immune, like a Blaze: they never catch fire, and fire,
 *       lava, magma and fireball damage do nothing.</li>
 *   <li>Poison Protection and Wither Protection make the wearer immune to the Poison and Wither
 *       effects. Wither Protection also stops wither and wither-skull damage.</li>
 *   <li>Water Breathing lets the wearer breathe underwater, so their air never goes down.</li>
 * </ul>
 * A runestone counts when it's worn as a charm. Without Curios (NeoForge) or Trinkets Updated
 * (Fabric) there's no charm slot, so one anywhere in the inventory counts instead.
 */
public final class Runestones {

    private Runestones() {}

    public static void register() {
        TickEvent.PLAYER_POST.register(Runestones::onPlayerTick);
    }

    /** Whether {@code player} has the runestone working: worn as a charm, or carried without a charm slot. */
    public static boolean has(Player player, Runestone runestone) {
        Item item = Registration.RUNESTONES.get(runestone).get();
        if (PicnicBasketAccessoryAvailability.isLoaded()) {
            return RunestoneAccessoryPlatform.isWearing(player, item);
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) return true;
        }
        return false;
    }

    /** Damage {@code player}'s runestones make them immune to. */
    public static boolean blocksDamage(Player player, DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (source.is(DamageTypeTags.IS_FIRE) && has(player, Runestone.FIRE_PROTECTION)) return true;
        return (source.is(DamageTypes.WITHER) || source.is(DamageTypes.WITHER_SKULL))
                && has(player, Runestone.WITHER_PROTECTION);
    }

    /** Effects {@code player}'s runestones make them immune to. */
    public static boolean blocksEffect(Player player, Holder<MobEffect> effect) {
        if (effect.value() == MobEffects.POISON.value()) return has(player, Runestone.POISON_PROTECTION);
        if (effect.value() == MobEffects.WITHER.value()) return has(player, Runestone.WITHER_PROTECTION);
        return false;
    }

    /**
     * Clears what a runestone put on mid-way should already have prevented: Poison or Wither the
     * player had before, fire still burning, air already used up.
     */
    private static void onPlayerTick(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !serverPlayer.isAlive()) return;
        if (serverPlayer.hasEffect(MobEffects.POISON) && has(serverPlayer, Runestone.POISON_PROTECTION)) {
            serverPlayer.removeEffect(MobEffects.POISON);
        }
        if (serverPlayer.hasEffect(MobEffects.WITHER) && has(serverPlayer, Runestone.WITHER_PROTECTION)) {
            serverPlayer.removeEffect(MobEffects.WITHER);
        }
        if (serverPlayer.getRemainingFireTicks() > 0 && has(serverPlayer, Runestone.FIRE_PROTECTION)) {
            serverPlayer.clearFire();
        }
        if (serverPlayer.getAirSupply() < serverPlayer.getMaxAirSupply()
                && has(serverPlayer, Runestone.WATER_BREATHING)) {
            serverPlayer.setAirSupply(serverPlayer.getMaxAirSupply());
        }
    }
}
