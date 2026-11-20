package net.bobofraggins.mobfarmingsupplies.oresight;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import java.util.List;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.network.OresightPacket;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * Server side of Oresight: which block each player's Oresight shows. Kept as an entity tag
 * ({@value #TAG_PREFIX}{@code <block id>}) so it lasts across saves, and sent to the player's client
 * ({@link OresightPacket}) when they drink, when they join, and when the effect ends.
 */
public final class Oresight {

    private static final String TAG_PREFIX = "mobfarmingsupplies.oresight=";

    private Oresight() {}

    public static void register() {
        TickEvent.PLAYER_POST.register(Oresight::onPlayerTick);
        PlayerEvent.PLAYER_JOIN.register(player -> send(player, target(player)));
    }

    public static Holder<MobEffect> effect() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Registration.ORESIGHT.get());
    }

    /** Points {@code player}'s Oresight at {@code block}, replacing whatever it showed. */
    public static void setTarget(ServerPlayer player, Block block) {
        clearTags(player);
        player.addTag(TAG_PREFIX + BuiltInRegistries.BLOCK.getKey(block));
        send(player, block);
    }

    /** The block {@code player}'s Oresight shows, or null. */
    @Nullable
    public static Block target(Player player) {
        for (String tag : player.entityTags()) {
            if (tag.startsWith(TAG_PREFIX)) {
                Identifier id = Identifier.tryParse(tag.substring(TAG_PREFIX.length()));
                return id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            }
        }
        return null;
    }

    /** Forgets the target once the effect has gone (run out, milk, death). */
    private static void onPlayerTick(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.hasEffect(effect())) return;
        if (clearTags(serverPlayer)) send(serverPlayer, null);
    }

    private static boolean clearTags(ServerPlayer player) {
        List<String> old = player.entityTags().stream().filter(tag -> tag.startsWith(TAG_PREFIX)).toList();
        old.forEach(player::removeTag);
        return !old.isEmpty();
    }

    private static void send(ServerPlayer player, @Nullable Block block) {
        NetworkManager.sendToPlayer(player, new OresightPacket(Optional.ofNullable(block)));
    }
}
