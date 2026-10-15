package net.bobofraggins.mobfarmingsupplies.magnet;

import dev.architectury.event.events.common.TickEvent;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketAccessoryAvailability;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * What a switched-on Superconducting Magnet does: every tick, items and experience orbs within
 * {@link #RANGE} blocks of its owner (a cube, not a sphere) are brought to them and picked up as if
 * they'd walked over them. Items the owner has just dropped themselves are left until they could
 * be picked up again, and items another mod has marked as not to be moved are left alone.
 */
public final class MagnetPull {

    /** How far, in blocks, in each direction. */
    public static final int RANGE = 8;

    private MagnetPull() {}

    public static void register() {
        TickEvent.PLAYER_POST.register(MagnetPull::onPlayerTick);
    }

    private static void onPlayerTick(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.isSpectator() || !serverPlayer.isAlive()) {
            return;
        }
        if (!isOn(findMagnet(serverPlayer, true))) return;

        AABB area = serverPlayer.getBoundingBox().inflate(RANGE);
        for (ItemEntity item : serverPlayer.level().getEntitiesOfClass(ItemEntity.class, area,
                item -> canCollect(item, serverPlayer))) {
            item.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
            item.playerTouch(serverPlayer);
        }
        for (ExperienceOrb orb : serverPlayer.level().getEntitiesOfClass(ExperienceOrb.class, area,
                ExperienceOrb::isAlive)) {
            // Collect them all at once, rather than one every couple of ticks.
            serverPlayer.takeXpDelay = 0;
            orb.playerTouch(serverPlayer);
        }
    }

    private static boolean canCollect(ItemEntity item, Player player) {
        if (!item.isAlive() || item.getItem().isEmpty()) return false;
        // Something its owner has just dropped stays put until they could pick it up again.
        if (item.hasPickUpDelay() && item.getOwner() == player) return false;
        return !MagnetRemoteMovement.isBlocked(item);
    }

    public static boolean isOn(ItemStack stack) {
        return stack.is(Registration.SUPERCONDUCTING_MAGNET.get())
                && stack.has(Registration.MAGNET_ON.get());
    }

    /**
     * The player's magnet: the one worn as a charm, or else the first in their inventory. With
     * {@code onOnly}, only a switched-on one counts.
     */
    public static ItemStack findMagnet(Player player, boolean onOnly) {
        ItemStack worn = wornMagnet(player);
        if (!worn.isEmpty() && (!onOnly || isOn(worn))) return worn;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Registration.SUPERCONDUCTING_MAGNET.get()) && (!onOnly || isOn(stack))) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack wornMagnet(Player player) {
        if (!PicnicBasketAccessoryAvailability.isLoaded()) return ItemStack.EMPTY;
        return MagnetAccessoryPlatform.findWornMagnet(player);
    }

    /**
     * Switches the player's magnet (see {@link #findMagnet}) on or off, from the toggle key. Says
     * so if they haven't got one.
     */
    public static void toggleFound(ServerPlayer player) {
        ItemStack worn = wornMagnet(player);
        if (!worn.isEmpty()) {
            MagnetAccessoryPlatform.replaceWornMagnet(player, toggle(player, worn.copy()));
            return;
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(Registration.SUPERCONDUCTING_MAGNET.get())) {
                inventory.setItem(i, toggle(player, inventory.getItem(i).copy()));
                return;
            }
        }
        player.displayClientMessage(Component.translatable("message.mobfarmingsupplies.superconducting_magnet.not_found"), true);
    }

    /** {@code stack} switched the other way, with a chime and a message for {@code player}. */
    public static ItemStack toggle(ServerPlayer player, ItemStack stack) {
        boolean on = !isOn(stack);
        if (on) stack.set(Registration.MAGNET_ON.get(), Unit.INSTANCE);
        else stack.remove(Registration.MAGNET_ON.get());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.4f, on ? 1.6f : 0.9f);
        player.displayClientMessage(Component.translatable(on
                ? "message.mobfarmingsupplies.superconducting_magnet.on"
                : "message.mobfarmingsupplies.superconducting_magnet.off"), true);
        return stack;
    }
}
