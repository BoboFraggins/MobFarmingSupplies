package net.bobofraggins.mobfarmingsupplies.advancement;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.List;
import java.util.function.Supplier;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * The mod's advancement criterion triggers. The advancements themselves are data files under
 * {@code data/mobfarmingsupplies/advancement}.
 *
 * <p>Events that happen to a block rather than to a player (a clone spawning, a head dropping, a
 * diamond being flushed) credit every player within a few blocks: blocks don't record who placed
 * them, and a harvester's fake player can't earn advancements.
 */
public final class MFSTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.TRIGGER_TYPE);

    // Logistics
    public static final RegistrySupplier<EventTrigger> FILTER_INSTALLED = event("filter_installed");
    public static final RegistrySupplier<EventTrigger> BRIDGE_CROSSED_DIMENSIONS = event("bridge_crossed_dimensions");
    // Mob Farming
    /** Variant {@code store} or {@code withdraw}. */
    public static final RegistrySupplier<EventTrigger> SYRINGE_USED = event("syringe_used");
    public static final RegistrySupplier<EventTrigger> CLONE_SPAWNED = event("clone_spawned");
    public static final RegistrySupplier<EventTrigger> MOB_BEHEADED = event("mob_beheaded");
    public static final RegistrySupplier<HeadsCollectedTrigger> HEADS_COLLECTED =
            TRIGGERS.register("heads_collected", HeadsCollectedTrigger::new);
    // Fun Stuff
    /** Variant: the button's block id. */
    public static final RegistrySupplier<EventTrigger> BUTTON_PRESSED = event("button_pressed");
    public static final RegistrySupplier<EventTrigger> HAT_TRICK = event("hat_trick");
    public static final RegistrySupplier<EventTrigger> PRESENT_WRAPPED = event("present_wrapped");
    /** Variant: the flushed item's id. */
    public static final RegistrySupplier<EventTrigger> TOILET_FLUSHED = event("toilet_flushed");

    /** How far from a block event a player can be and still be credited. */
    public static final double NEARBY = 16;

    private MFSTriggers() {}

    private static RegistrySupplier<EventTrigger> event(String name) {
        return TRIGGERS.register(name, EventTrigger::new);
    }

    /** Real players (never fake ones) within {@link #NEARBY} blocks of {@code pos}. */
    public static List<ServerPlayer> playersNear(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return List.of();
        double max = NEARBY * NEARBY;
        return serverLevel.getPlayers(p -> pos.distToCenterSqr(p.position()) <= max);
    }

    /** Fires {@code trigger} for every player near {@code pos}. */
    public static void triggerNear(Supplier<EventTrigger> trigger, Level level, BlockPos pos, String variant) {
        for (ServerPlayer player : playersNear(level, pos)) trigger.get().trigger(player, variant);
    }
}
