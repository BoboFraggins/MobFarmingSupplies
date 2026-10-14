package net.bobofraggins.mobfarmingsupplies.client.devtools;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Development tool: once a singleplayer world has loaded, runs the commands in the file named by
 * the {@code MFS_DEV_COMMANDS} environment variable as the player, one per tick ("WAIT n" waits n
 * ticks), then closes the game. For testing things that need a real player. Two lines act on the
 * client instead: "CAMERA first|back|front" sets the camera, and "SCREENSHOT name" saves
 * {@code screenshots/name.png}.
 */
public final class DevCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger("MobFarmingSupplies/DevCommands");
    private static final int SETTLE_TICKS = 100;

    private static Deque<String> commands;
    private static int ticks, waiting;

    private DevCommands() {}

    public static void register() {
        String file = System.getenv("MFS_DEV_COMMANDS");
        if (file == null || file.isBlank() || !Platform.isDevelopmentEnvironment()) return;
        try {
            commands = new ArrayDeque<>(Files.readAllLines(Path.of(file)));
        } catch (Exception e) {
            LOGGER.error("Couldn't read {}", file, e);
            return;
        }
        ClientTickEvent.CLIENT_POST.register(DevCommands::tick);
    }

    private static void tick(Minecraft mc) {
        MinecraftServer server = mc.getSingleplayerServer();
        if (mc.player == null || server == null || ++ticks < SETTLE_TICKS) return;
        if (waiting > 0) {
            waiting--;
            return;
        }
        String line = commands.poll();
        if (line == null) {
            LOGGER.info("Dev commands done");
            commands.add("WAIT 1000000");
            mc.stop();
            return;
        }
        line = line.strip();
        if (line.isEmpty()) return;
        if (line.startsWith("WAIT ")) {
            waiting = Integer.parseInt(line.substring(5).strip());
            return;
        }
        if (line.startsWith("CAMERA ")) {
            mc.options.setCameraType(switch (line.substring(7).strip()) {
                case "back" -> CameraType.THIRD_PERSON_BACK;
                case "front" -> CameraType.THIRD_PERSON_FRONT;
                default -> CameraType.FIRST_PERSON;
            });
            return;
        }
        if (line.startsWith("SCREENSHOT ")) {
            Screenshot.grab(mc.gameDirectory, line.substring(11).strip() + ".png", mc.getMainRenderTarget(), 1,
                    message -> LOGGER.info("{}", message.getString()));
            return;
        }
        String command = line;
        LOGGER.info("> {}", command);
        server.execute(() -> {
            var player = server.getPlayerList().getPlayers().getFirst();
            // The server's own source, acting as the player: the player may not be allowed commands.
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack()
                    .withEntity(player).withPosition(player.position()).withLevel(player.level()), command);
        });
    }
}
