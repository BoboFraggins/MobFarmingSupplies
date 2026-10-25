package net.bobofraggins.mobfarmingsupplies.client.devtools;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
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
 * client instead: "CAMERA first|back|front" sets the camera, "SCREENSHOT name" saves
 * {@code screenshots/name.png}, and "RECIPES item_id" opens JEI's recipes for an item.
 * "USEBLOCK x y z" clicks the top of that block with the held item, as a player does, and
 * "MENUBUTTON id" presses button {@code id} of the open menu, and "QUICKMOVE slot" shift-clicks
 * that slot of the open menu.
 */
public final class DevCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger("MobFarmingSupplies/DevCommands");
    private static final int SETTLE_TICKS = 100;

    /** Opens JEI's recipes for an item; set by the JEI plugin when JEI is installed. */
    @Nullable public static Consumer<ItemStack> showRecipes;

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
            Identifier id = Identifier.tryParse(line.substring(8).strip());
            if (showRecipes != null && id != null) {
                showRecipes.accept(new ItemStack(BuiltInRegistries.ITEM.getValue(id)));
            }
            return;
        }
        if (line.startsWith("SCREENSHOT ")) {
            Screenshot.grab(mc.gameDirectory, line.substring(11).strip() + ".png", mc.gameRenderer.mainRenderTarget(), 1,
                    message -> LOGGER.info("{}", message.getString()));
            return;
        }
        if (line.startsWith("MENUBUTTON ")) {
            // Presses a button in the open menu, as clicking it in the screen does.
            if (mc.player != null && mc.gameMode != null) {
                int id = Integer.parseInt(line.substring(11).strip());
                mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId, id);
                LOGGER.info("MENUBUTTON {} in {}", id, mc.player.containerMenu.getClass().getSimpleName());
            }
            return;
        }
        if (line.startsWith("QUICKMOVE ")) {
            // Shift-clicks a slot of the open menu, as the player does.
            if (mc.player != null && mc.gameMode != null) {
                int slot = Integer.parseInt(line.substring(10).strip());
                mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, slot, 0,
                        net.minecraft.world.inventory.ContainerInput.QUICK_MOVE, mc.player);
                LOGGER.info("QUICKMOVE {} in {}", slot, mc.player.containerMenu.getClass().getSimpleName());
            }
            return;
        }
        if (line.startsWith("XPBAR ")) {
            // Sets the player's experience bar fraction directly (to recreate float drift).
            float bar = Float.parseFloat(line.substring(6).strip());
            server.execute(() -> server.getPlayerList().getPlayers().getFirst().experienceProgress = bar);
            return;
        }
        if (line.startsWith("USEBLOCK ")) {
            // Clicks the top face of the block at x y z with the main-hand item, as a player does.
            String[] p = line.substring(9).strip().split("\\s+");
            BlockPos target = new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
            server.execute(() -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target).add(0, 0.5, 0), Direction.UP, target, false);
                InteractionResult result = player.gameMode.useItemOn(
                        player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
                LOGGER.info("USEBLOCK {} -> {}", target, result);
            });
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
