package net.bobofraggins.mobfarmingsupplies.mixin;

import net.bobofraggins.mobfarmingsupplies.advancement.MFSTriggers;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When a head enters a player's inventory, re-checks "Head of the Class" — after vanilla's own
 * handling of this event has recorded the head in the hidden head-tracker advancement
 * (see {@link net.bobofraggins.mobfarmingsupplies.advancement.HeadsCollectedTrigger}).
 */
@Mixin(InventoryChangeTrigger.class)
public abstract class InventoryChangeTriggerMixin {

    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL"))
    private void mobfarmingsupplies$countHeads(ServerPlayer player, Inventory inventory, ItemStack stack, CallbackInfo ci) {
        if (stack.is(ItemTags.SKULLS)) MFSTriggers.HEADS_COLLECTED.get().trigger(player);
    }
}
