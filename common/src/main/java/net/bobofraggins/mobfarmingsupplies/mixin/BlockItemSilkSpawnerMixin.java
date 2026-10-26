package net.bobofraggins.mobfarmingsupplies.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets a spawner the Block Breaker took with Silk Touch keep its mob when anyone places it. Vanilla
 * only applies a spawner item's block entity data for operators; this makes an exception for items
 * carrying {@link Registration#SILK_TOUCHED_SPAWNER}, and only those.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemSilkSpawnerMixin {

    @ModifyExpressionValue(
            method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntityType;onlyOpCanSetNbt()Z"))
    private static boolean mobfarmingsupplies$silkTouchedSpawner(boolean onlyOps, @Local(argsOnly = true) ItemStack stack) {
        return onlyOps && !stack.has(Registration.SILK_TOUCHED_SPAWNER.get());
    }
}
