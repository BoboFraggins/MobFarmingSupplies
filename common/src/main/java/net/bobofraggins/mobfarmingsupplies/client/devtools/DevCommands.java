package net.bobofraggins.mobfarmingsupplies.client.devtools;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Development tool: once a singleplayer world has loaded, runs the commands in the file named by
 * the {@code MFS_DEV_COMMANDS} environment variable as the player, one per tick ("WAIT n" waits n
 * ticks), then closes the game. For testing things that need a real player. Two lines act on the
 * client instead: "CAMERA first|back|front" sets the camera, "SCREENSHOT name" saves
 * {@code screenshots/name.png}, "RECIPES item_id" opens JEI's recipes for an item, "USES item_id"
 * its uses, "USEON x y z" uses the held item on the top of that block, as a click does, and
 * "CLIENTDATA x y z" logs the client's copy of the block entity there.
 */
public final class DevCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger("MobFarmingSupplies/DevCommands");
    private static final int SETTLE_TICKS = 100;

    /** Opens JEI's recipes for an item; set by the JEI plugin when JEI is installed. */
    @Nullable public static Consumer<ItemStack> showRecipes;
    @Nullable public static Consumer<ItemStack> showUses;

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
        if (line.startsWith("RECIPES ")) {
            ResourceLocation id = ResourceLocation.tryParse(line.substring(8).strip());
            if (showRecipes != null && id != null) {
                showRecipes.accept(new ItemStack(BuiltInRegistries.ITEM.get(id)));
            }
            return;
        }
        if (line.startsWith("USES ")) {
            ResourceLocation id = ResourceLocation.tryParse(line.substring(5).strip());
            if (showUses != null && id != null) {
                showUses.accept(new ItemStack(BuiltInRegistries.ITEM.get(id)));
            }
            return;
        }
        if (line.startsWith("SCREENSHOT ")) {
            Screenshot.grab(mc.gameDirectory, line.substring(11).strip() + ".png", mc.getMainRenderTarget(),
                    message -> LOGGER.info("{}", message.getString()));
            return;
        }
        if (line.startsWith("CLIENTDATA ")) {
            // Logs the client's copy of the block entity at x y z, to compare with "data get block".
            String[] p = line.substring(11).strip().split("\\s+");
            BlockPos at = new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
            var be = mc.level.getBlockEntity(at);
            LOGGER.info("CLIENTDATA {} -> {}", at, be == null ? "none" : be.saveWithoutMetadata(mc.level.registryAccess()));
            return;
        }
        if (line.startsWith("USEON ")) {
            // Uses the main-hand item on the top face of the block at x y z, as a player's click does.
            String[] p = line.substring(6).strip().split("\\s+");
            BlockPos target = new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
            server.execute(() -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target).add(0, 0.5, 0), Direction.UP, target, false);
                InteractionResult result = player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                LOGGER.info("USEON {} -> {}", target, result);
            });
            return;
        }
        String command = line;
        LOGGER.info("> {}", command);
        server.execute(() -> {
            var player = server.getPlayerList().getPlayers().getFirst();
            // The server's own source, acting as the player: the player may not be allowed commands.
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack()
                    .withEntity(player).withPosition(player.position()).withLevel(player.serverLevel()), command);
        });
    }
}
