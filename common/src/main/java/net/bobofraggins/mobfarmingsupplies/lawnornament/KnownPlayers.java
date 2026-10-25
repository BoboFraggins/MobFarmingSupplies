package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.authlib.GameProfile;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Every player who has connected to this world: those with saved player data when the server
 * starts, the singleplayer host, and anyone who joins while it runs. Village yards pick one of
 * them for a Giant Player Lawn Ornament. Read during world generation, which can be off the
 * server thread, so the map is concurrent.
 */
public final class KnownPlayers {

    /** Each player's id, with their name when the server knows it ("" when it doesn't). */
    private static final Map<UUID, String> PLAYERS = new ConcurrentHashMap<>();

    private KnownPlayers() {}

    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(KnownPlayers::load);
        LifecycleEvent.SERVER_STOPPED.register(server -> PLAYERS.clear());
        PlayerEvent.PLAYER_JOIN.register(player -> add(player.getGameProfile()));
    }

    private static void load(MinecraftServer server) {
        PLAYERS.clear();
        if (server.getSingleplayerProfile() != null) add(server.getSingleplayerProfile());
        Path dir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(".dat"))
                    .forEach(name -> {
                        try {
                            UUID id = UUID.fromString(name.substring(0, name.length() - 4));
                            String known = server.services().nameToIdCache().get(id)
                                    .map(nameAndId -> nameAndId.name()).orElse("");
                            PLAYERS.putIfAbsent(id, known);
                        } catch (IllegalArgumentException notAPlayer) {
                            // Some other file in the folder.
                        }
                    });
        } catch (IOException ignored) {
            // No saved players to add.
        }
    }

    private static void add(GameProfile profile) {
        PLAYERS.put(profile.id(), profile.name());
    }

    /**
     * One of the known players, at random, as a profile the client looks up for its name and skin,
     * or empty when nobody has connected yet.
     */
    public static Optional<ResolvableProfile> random(RandomSource random) {
        List<Map.Entry<UUID, String>> players = new ArrayList<>(PLAYERS.entrySet());
        if (players.isEmpty()) return Optional.empty();
        Map.Entry<UUID, String> player = players.get(random.nextInt(players.size()));
        return Optional.of(player.getValue().isEmpty()
                ? ResolvableProfile.createUnresolved(player.getKey())
                : ResolvableProfile.createUnresolved(player.getValue()));
    }
}
