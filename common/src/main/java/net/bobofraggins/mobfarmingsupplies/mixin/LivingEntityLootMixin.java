package net.bobofraggins.mobfarmingsupplies.mixin;

import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.mobharvester.FireAspectCooking;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Lets the Mob Harvester's Fire Aspect Upgrade cook a mob's loot as it drops ({@link FireAspectCooking}). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityLootMixin {

    @ModifyVariable(
            method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;ZLnet/minecraft/resources/ResourceKey;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"), argsOnly = true)
    private Consumer<ItemStack> mobfarmingsupplies$cookDrops(Consumer<ItemStack> drops, ServerLevel level,
                                                             DamageSource source, boolean playerKill,
                                                             ResourceKey<LootTable> table) {
        return FireAspectCooking.wrap(level, drops);
    }
}
