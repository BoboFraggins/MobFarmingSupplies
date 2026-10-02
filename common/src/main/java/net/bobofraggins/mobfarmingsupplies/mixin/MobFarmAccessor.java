package net.bobofraggins.mobfarmingsupplies.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobFarmAccessor {

    @Accessor("goalSelector")
    GoalSelector getGoalSelectorDirect();

    @Mutable
    @Accessor("moveControl")
    void setMoveControl(MoveControl moveControl);

    @Invoker("populateDefaultEquipmentSlots")
    void invokePopulateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty);

    @Invoker("populateDefaultEquipmentEnchantments")
    void invokePopulateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty);
}
