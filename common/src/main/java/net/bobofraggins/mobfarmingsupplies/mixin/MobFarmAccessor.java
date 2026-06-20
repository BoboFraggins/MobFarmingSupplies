package net.bobofraggins.mobfarmingsupplies.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobFarmAccessor {

    @Accessor("goalSelector")
    GoalSelector getGoalSelectorDirect();

    @Mutable
    @Accessor("moveControl")
    void setMoveControl(MoveControl moveControl);
}
