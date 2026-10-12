package net.bobofraggins.mobfarmingsupplies.client.devtools;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Development tool: places Gravitational Anomalies high in the sky of a test world and takes
 * screenshots of them from several angles, then closes the game.
 *
 * <p>Only active in a development environment with the {@code MFS_CAPTURE_SCENE} environment
 * variable set to an output folder. Launch straight into a world, e.g.
 * {@code ./gradlew :neoforge:runClient --args="--quickPlaySingleplayer icons"}.
 */
public final class SceneCapture {

    private static final Logger LOGGER = LoggerFactory.getLogger("MobFarmingSupplies/SceneCapture");

    /** Ticks to let the world settle, and to let each new view render, before a screenshot. */
    private static final int SETTLE_TICKS = 200;
    private static final int VIEW_TICKS = 30;
    /** How far the camera is from the anomaly, in blocks. */
    private static final double DISTANCE = 3.0;
    private static final int HEIGHT = 200;

    /** A view: which anomaly, from what bearing and elevation (degrees), and at what time of day. */
    private record Shot(String name, int anomalyX, double bearing, double elevation, long time) {}

    private static Path outputDir;
    private static Deque<Shot> shots;
    private static Shot current;
    private static int ticks;
    private static final AtomicInteger pending = new AtomicInteger();
    private static boolean finished;

    private SceneCapture() {}

    public static void register() {
        String out = System.getenv("MFS_CAPTURE_SCENE");
        if (out == null || out.isBlank() || !Platform.isDevelopmentEnvironment()) return;
        outputDir = Path.of(out).toAbsolutePath().normalize();
        ClientTickEvent.CLIENT_POST.register(SceneCapture::tick);
        LOGGER.info("Scene screenshots will be written to {}", outputDir);
    }

    private static List<Shot> plan() {
        List<Shot> list = new ArrayList<>();
        // The idle anomaly at x 0, the active one far off at x 64.
        double[] elevations = {80, 50, 25, 10, 3, 0, -20};
        for (double elevation : elevations) {
            list.add(new Shot("idle_" + label(elevation), 0, 20, elevation, 6000));
        }
        list.add(new Shot("idle_night_25", 0, 20, 25, 18000));
        for (double elevation : new double[] {70, 30, 8, -15}) {
            list.add(new Shot("active_" + label(elevation), 64, 20, elevation, 6000));
        }
        list.add(new Shot("active_night_20", 64, 20, 20, 18000));
        return list;
    }

    private static String label(double elevation) {
        return elevation < 0 ? "below" + (int) -elevation : "above" + (int) elevation;
    }

    private static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null || finished) return;
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null) return;
        ticks++;
        if (shots == null) {
            if (ticks == 1) {
                command(server, "gamemode spectator @a");
                command(server, "gamerule doDaylightCycle false");
                command(server, "weather clear");
                command(server, "forceload add -16 -16 80 16");
                command(server, "setblock 0 " + HEIGHT + " 0 mobfarmingsupplies:gravitational_anomaly");
                command(server, "setblock 64 " + HEIGHT + " 0 mobfarmingsupplies:gravitational_anomaly[active=true]");
                mc.options.hideGui = true;
            }
            if (ticks < SETTLE_TICKS) return;
            shots = new ArrayDeque<>(plan());
            ticks = 0;
        }
        if (current == null) {
            current = shots.poll();
            if (current == null) {
                if (pending.get() == 0) {
                    finished = true;
                    LOGGER.info("Scene screenshots written to {}", outputDir);
                    mc.stop();
                }
                return;
            }
            aim(server, current);
            ticks = 0;
            return;
        }
        if (ticks < VIEW_TICKS) return;
        Shot shot = current;
        current = null;
        pending.incrementAndGet();
        Screenshot.takeScreenshot(mc.getMainRenderTarget(), image -> {
            try (image) {
                Files.createDirectories(outputDir);
                image.writeToFile(outputDir.resolve(shot.name() + ".png"));
            } catch (Exception e) {
                LOGGER.error("Couldn't save {}", shot.name(), e);
            } finally {
                pending.decrementAndGet();
            }
        });
    }

    /** Puts the camera {@link #DISTANCE} from the anomaly, looking at its centre. */
    private static void aim(MinecraftServer server, Shot shot) {
        double cx = shot.anomalyX() + 0.5, cy = HEIGHT + 0.5, cz = 0.5;
        double b = Math.toRadians(shot.bearing()), e = Math.toRadians(shot.elevation());
        double ex = cx + DISTANCE * Math.cos(e) * Math.sin(b);
        double ey = cy + DISTANCE * Math.sin(e);
        double ez = cz + DISTANCE * Math.cos(e) * Math.cos(b);
        double dx = cx - ex, dy = cy - ey, dz = cz - ez;
        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        double pitch = Math.toDegrees(-Math.atan2(dy, Math.hypot(dx, dz)));
        command(server, "time set " + shot.time());
        // A spectator's eyes are 1.62 blocks above its feet.
        command(server, String.format(java.util.Locale.ROOT, "tp @a %.4f %.4f %.4f %.3f %.3f",
                ex, ey - 1.62, ez, yaw, pitch));
    }

    private static void command(MinecraftServer server, String command) {
        server.execute(() -> server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput(), command));
    }
}
