package net.bobofraggins.mobfarmingsupplies.mixin;

import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.mobharvester.FireAspectCooking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Lets the Mob Harvester's Fire Aspect Upgrade cook a mob's loot as it drops ({@link FireAspectCooking}). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityLootMixin {

    @ModifyArg(
            method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems("
                    + "Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"),
            index = 2)
    private Consumer<ItemStack> mobfarmingsupplies$cookDrops(Consumer<ItemStack> drops) {
        return ((LivingEntity) (Object) this).level() instanceof ServerLevel level
                ? FireAspectCooking.wrap(level, drops) : drops;
    }
}
