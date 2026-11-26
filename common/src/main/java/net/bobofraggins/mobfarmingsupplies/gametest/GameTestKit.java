package net.bobofraggins.mobfarmingsupplies.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import java.util.function.Consumer;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Shared pieces for the mod's game tests. Every test runs on {@link #PLATFORM}: a 15x8x15 space
 * with a smooth stone floor (made by {@code tools/make_gametest_platform.py}). The test framework puts
 * a structure one block above the test's origin, so in a test's own coordinates the floor is y=1 and
 * blocks go at y=2.
 *
 * <p>The tests live in common and are handed to each loader as a list of {@link TestFunction}s
 * ({@link MFSGameTests#all()}). They are development only: the release jars leave this package out.
 */
public final class GameTestKit {

    public static final String MODID = MobFarmingSuppliesCommon.MODID;
    public static final String PLATFORM = MODID + ":gametest/platform";

    /** Middle of the platform, one above the floor. */
    public static final BlockPos CENTRE = new BlockPos(7, 2, 7);

    private GameTestKit() {}

    public static TestFunction test(String batch, String name, int maxTicks, Consumer<GameTestHelper> body) {
        return new TestFunction(batch, MODID + "." + batch + "." + name, PLATFORM, maxTicks, 0L, true, body);
    }

    /**
     * A stand-in player two blocks north of {@link #CENTRE}, facing it (south). It isn't in the
     * server's player list (vanilla's mock player is, and Forge then tries to send it the login
     * handshake over a connection without a channel); its connection is an in-memory channel that
     * swallows whatever is sent, so opening screens works.
     */
    public static ServerPlayer player(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level,
                new GameProfile(UUID.randomUUID(), "mfs-test-player"));
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);
        channel.attr(Connection.ATTRIBUTE_PROTOCOL).set(ConnectionProtocol.PLAY);
        new ServerGamePacketListenerImpl(level.getServer(), connection, player);
        Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(CENTRE.north(2)));
        player.moveTo(at.x, at.y, at.z, 0f, 30f);
        return player;
    }

    /** Ends a {@link #player}: closes whatever it has open. */
    public static void remove(GameTestHelper helper, ServerPlayer player) {
        player.closeContainer();
    }

    public static boolean isOurs(ResourceLocation id) {
        return id.getNamespace().equals(MODID);
    }

    /** Collects every problem in a test before failing, so one run shows them all. */
    public static final class Problems {
        private final List<String> list = new ArrayList<>();

        public void add(String problem) {
            list.add(problem);
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        /** Fails {@code helper} with every problem, or succeeds. */
        public void finish(GameTestHelper helper) {
            if (list.isEmpty()) {
                helper.succeed();
            } else {
                helper.fail(list.size() + " problem(s):\n  - " + String.join("\n  - ", list));
            }
        }
    }
}
