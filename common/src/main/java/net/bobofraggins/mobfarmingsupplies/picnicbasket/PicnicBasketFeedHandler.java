package net.bobofraggins.mobfarmingsupplies.picnicbasket;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto-feeds the player from a Picnic Basket carried in their main inventory or offhand
 * when their hunger is below max (Curios/Trinkets accessory slots are added to the scan in
 * later stages).
 *
 * <p>Every {@link #FEED_INTERVAL} ticks, scans for a basket with auto-feed enabled and any
 * stored food, picks one at random, consumes it (nutrition + saturation via
 * {@link net.minecraft.world.food.FoodData#eat}), and decrements its stored count by one.
 */
public final class PicnicBasketFeedHandler {

    private PicnicBasketFeedHandler() {}

    /** Ticks between feeding attempts. 40 t = 2 s. */
    private static final int FEED_INTERVAL = 40;

    public static void register() {
        TickEvent.PLAYER_POST.register(PicnicBasketFeedHandler::onPlayerTick);
    }

    private static void onPlayerTick(Player player) {
        if (player.level().isClientSide()) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!player.getFoodData().needsFood()) return;
        if (player.level().getGameTime() % FEED_INTERVAL != 0) return;

        PicnicBasketLocator locator = PicnicBasketItemUtils.findBasketLocator(player);
        if (locator == null) return;
        ItemStack basketStack = locator.get(player);
        if (basketStack.isEmpty()) return;

        feedFromBasket(locator, basketStack, player, (ServerLevel) player.level());
    }

    // ── Feed ─────────────────────────────────────────────────────────────────────

    private static void feedFromBasket(
            PicnicBasketLocator locator, ItemStack basketStack, Player player, ServerLevel level) {
        PicnicBasketBlockEntity temp = PicnicBasketItemUtils.readBasketData(player, basketStack);
        if (!temp.isAutoFeed()) return;

        List<ItemStack> candidates = new ArrayList<>();
        Map<ItemStack, Integer> indexByStack = new IdentityHashMap<>();
        for (int i = 0; i < PicnicBasketBlockEntity.SLOT_COUNT; i++) {
            ItemStack stored = temp.inventory.getItem(i);
            if (!stored.isEmpty() && stored.has(DataComponents.FOOD)) {
                candidates.add(stored);
                indexByStack.put(stored, i);
            }
        }
        if (candidates.isEmpty()) return;

        // If Spice of Life: Carrot Edition is installed, prefer foods the player hasn't eaten yet.
        List<ItemStack> preferred = SolCarrotBridge.preferUneatenFoods(player, candidates);
        ItemStack stored = preferred.get(level.getRandom().nextInt(preferred.size()));
        int chosenIdx = indexByStack.get(stored);

        FoodProperties food = stored.get(DataComponents.FOOD);
        if (food == null) return;

        player.getFoodData().eat(food);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.PLAYERS,
                0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);

        stored.shrink(1);
        if (stored.isEmpty()) temp.inventory.setItem(chosenIdx, ItemStack.EMPTY);

        PicnicBasketItemUtils.writeBasketData(player, basketStack, temp);
        locator.set(player, basketStack);
    }
}
