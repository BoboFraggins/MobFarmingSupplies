package net.bobofraggins.mobfarmingsupplies.essencehopper;

import dev.architectury.event.events.common.TickEvent;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

/**
 * Flight, from drinking Essence of Air: creative-style flying while it lasts. Each tick a player
 * with the effect is allowed to fly; when it ends (runs out, milk, death) a player it let fly loses
 * flight again, unless their game mode gives it to them anyway. A tag on the player remembers who
 * it let fly, across saves, so flight given by anything else is left alone.
 */
public class FlightMobEffect extends MobEffect {

    private static final String GRANTED_TAG = "mobfarmingsupplies.flight";

    public FlightMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x87CEEB);
    }

    public static void register() {
        TickEvent.PLAYER_POST.register(FlightMobEffect::onPlayerTick);
    }

    private static void onPlayerTick(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Abilities abilities = serverPlayer.getAbilities();
        boolean hasEffect = serverPlayer.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Registration.FLIGHT.get()));
        if (hasEffect) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                serverPlayer.onUpdateAbilities();
                serverPlayer.addTag(GRANTED_TAG);
            }
        } else if (serverPlayer.entityTags().contains(GRANTED_TAG)) {
            serverPlayer.removeTag(GRANTED_TAG);
            if (!serverPlayer.isCreative() && !serverPlayer.isSpectator()) {
                abilities.mayfly = false;
                abilities.flying = false;
                serverPlayer.onUpdateAbilities();
            }
        }
    }
}
