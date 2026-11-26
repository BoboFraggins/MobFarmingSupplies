package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.shared.compat.DataComponentInput;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ImplicitComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When a block is placed from its item, hands the item to the block entity if it keeps data on its
 * item ({@link ImplicitComponents}): 1.20.1's stand-in for 1.20.5+'s {@code applyComponentsFromItemStack}.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemImplicitComponentsMixin {

    @Inject(method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("RETURN"))
    private static void mobfarmingsupplies$applyImplicitComponents(Level level, Player player, BlockPos pos,
                                                                    ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ImplicitComponents holder) {
            holder.applyImplicitComponents(new DataComponentInput(stack));
            be.setChanged();
        }
    }
}
