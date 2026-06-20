package net.bobofraggins.mobfarmingsupplies.cloneomatic;

import net.bobofraggins.mobfarmingsupplies.mixin.GoalSelectorAccessor;
import net.bobofraggins.mobfarmingsupplies.mixin.MobFarmAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.Set;

final class AquaticFarmMobHelper {

    private AquaticFarmMobHelper() {}

    /**
     * Strips the swimming AI from a squid or glow squid spawned by the Clone-O-Matic
     * so fans and vector plates can move it without the mob's own physics fighting back.
     *
     * Replaces SquidMoveControl with the passive default (applies no thrust) and clears
     * all registered goals so the mob is purely physics-driven.
     */
    static void suppressSwimmingAI(Mob mob) {
        MobFarmAccessor acc = (MobFarmAccessor) mob;

        acc.setMoveControl(new MoveControl(mob));

        Set<WrappedGoal> goals = ((GoalSelectorAccessor) acc.getGoalSelectorDirect()).getAvailableGoals();
        goals.forEach(wg -> { if (wg.isRunning()) wg.stop(); });
        goals.clear();
    }
}
