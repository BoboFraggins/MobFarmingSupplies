package net.bobofraggins.mobfarmingsupplies.client.devtools;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Development tool: renders block icons as transparent PNGs, exactly as JEI and the creative tab
 * draw them, but at a resolution suitable for documentation. See {@code docs/imgs/README.md}.
 *
 * <p>Only active in a development environment with the {@code MFS_RENDER_ICONS} environment
 * variable set to an output folder. Once a world is open it renders each block, one per tick,
 * then closes the game. (Items can't be created before a world loads: their components aren't
 * bound until then.) {@code MFS_RENDER_ICONS_ITEMS} renders just the listed items instead of
 * every block item in the mod: a comma-separated list of items written as for {@code /give},
 * components included, each optionally prefixed with {@code name=} to choose its file name.
 *
 * <p>Each item is drawn into its own transparent offscreen target with the projection, transform
 * and lighting {@code GuiGraphics} uses for item icons, so the angle and lighting match. The result
 * is cropped to the item, scaled with an area filter so its larger side is {@link #FILL} of
 * {@link #SIZE}, and centred.
 */
public final class BlockIconRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger("MobFarmingSupplies/BlockIconRenderer");

    /** Output image size, in pixels. */
    public static final int SIZE = 256;
    /** How much of the image the item's larger side fills. */
    public static final double FILL = 0.8;
    /** Size the item is rendered at before being scaled down. */
    private static final int RENDER_SIZE = 1024;

    private static Path outputDir;
    /** An item to render and the file name (without extension) to save it as. */
    private record Icon(String name, ItemStack stack) {}

    private static Deque<Icon> queue;
    private static int written;
    private static int worldTicks;
    private static boolean finished;

    private BlockIconRenderer() {}

    public static void register() {
        String out = System.getenv("MFS_RENDER_ICONS");
        if (out == null || out.isBlank() || !Platform.isDevelopmentEnvironment()) return;
        outputDir = Path.of(out).toAbsolutePath().normalize();
        ClientTickEvent.CLIENT_POST.register(BlockIconRenderer::tick);
        LOGGER.info("Block icons will be written to {}", outputDir);
    }

    private static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        if (queue == null) {
            if (++worldTicks < 40) return; // let the world settle first
            queue = new ArrayDeque<>(itemsToRender(mc));
        }
        Icon next = queue.poll();
        if (next != null) {
            try {
                render(mc, next);
            } catch (Exception e) {
                LOGGER.error("Couldn't render {}", next.name(), e);
            }
        } else if (!finished) {
            finished = true;
            LOGGER.info("Wrote {} block icons to {}", written, outputDir);
            mc.stop();
        }
    }

    private static List<Icon> itemsToRender(Minecraft mc) {
        List<Icon> icons = new ArrayList<>();
        String only = System.getenv("MFS_RENDER_ICONS_ITEMS");
        if (only != null && !only.isBlank()) {
            ItemParser parser = new ItemParser(mc.level.registryAccess());
            for (String entry : splitTopLevel(only)) {
                String name = null, spec = entry;
                int eq = entry.indexOf('='), bracket = entry.indexOf('[');
                if (eq > 0 && (bracket < 0 || eq < bracket) && entry.substring(0, eq).matches("[a-z0-9_]+")) {
                    name = entry.substring(0, eq);
                    spec = entry.substring(eq + 1);
                }
                try {
                    ItemParser.ItemResult parsed = parser.parse(new StringReader(spec));
                    ItemStack stack = new ItemStack(parsed.item(), 1, parsed.components());
                    icons.add(new Icon(name != null ? name : BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(), stack));
                } catch (CommandSyntaxException e) {
                    LOGGER.warn("Couldn't read item {}: {}", spec, e.getMessage());
                }
            }
            return icons;
        }
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            // Mob Heads need a mob type to draw anything; list specific ones with their component instead.
            if (key.getNamespace().equals(MobFarmingSuppliesCommon.MODID) && item instanceof BlockItem
                    && !key.getPath().equals("mob_head")) {
                icons.add(new Icon(key.getPath(), new ItemStack(item)));
            }
        }
        return icons;
    }

    /** Splits on commas that aren't inside component brackets or quotes. */
    private static List<String> splitTopLevel(String list) {
        List<String> parts = new ArrayList<>();
        int depth = 0, start = 0;
        boolean quoted = false;
        for (int i = 0; i < list.length(); i++) {
            char c = list.charAt(i);
            if (c == '"' && (i == 0 || list.charAt(i - 1) != '\\')) quoted = !quoted;
            else if (!quoted && (c == '[' || c == '{')) depth++;
            else if (!quoted && (c == ']' || c == '}')) depth--;
            else if (!quoted && depth == 0 && c == ',') {
                parts.add(list.substring(start, i).trim());
                start = i + 1;
            }
        }
        parts.add(list.substring(start).trim());
        parts.removeIf(String::isEmpty);
        return parts;
    }

    private static void render(Minecraft mc, Icon icon) throws Exception {
        ItemStack stack = icon.stack();
        TextureTarget target = new TextureTarget(RENDER_SIZE, RENDER_SIZE, true, Minecraft.ON_OSX);
        target.setClearColor(0f, 0f, 0f, 0f);
        target.clear(Minecraft.ON_OSX);
        target.bindWrite(true);

        // As GuiGraphics draws an item, at RENDER_SIZE pixels instead of 16.
        Matrix4f projection = new Matrix4f().setOrtho(0f, RENDER_SIZE, RENDER_SIZE, 0f, 1000f, 21000f);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        modelView.translate(0f, 0f, -11000f);
        RenderSystem.applyModelViewMatrix();
        try {
            PoseStack pose = new PoseStack();
            pose.translate(RENDER_SIZE / 2f, RENDER_SIZE / 2f, 150f);
            pose.scale(RENDER_SIZE, -RENDER_SIZE, RENDER_SIZE);
            BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, null, 0);
            if (model.usesBlockLight()) Lighting.setupFor3DItems();
            else Lighting.setupForFlatItems();
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            mc.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose, buffers,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
            buffers.endBatch();
        } finally {
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            mc.getMainRenderTarget().bindWrite(true);
        }

        // takeScreenshot gives rows top to bottom; save reads them bottom to top, as the GPU has them.
        try (NativeImage image = Screenshot.takeScreenshot(target)) {
            int n = RENDER_SIZE;
            ByteBuffer data = ByteBuffer.allocate(n * n * 4);
            for (int y = 0; y < n; y++) {
                for (int x = 0; x < n; x++) {
                    data.putInt((x + (n - 1 - y) * n) * 4, image.getPixelRGBA(x, y));
                }
            }
            save(icon.name(), data, 4);
        } finally {
            target.destroyBuffers();
        }
    }

    /** Crops the rendered item, scales it to fit {@link #FILL} of the image, and writes the PNG. */
    private static void save(String name, ByteBuffer data, int pixelSize) throws Exception {
        int n = RENDER_SIZE;
        int[] argb = new int[n * n];
        int minX = n, minY = n, maxX = -1, maxY = -1;
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                int abgr = data.getInt((x + y * n) * pixelSize);
                int a = abgr >>> 24, b = (abgr >> 16) & 0xFF, g = (abgr >> 8) & 0xFF, r = abgr & 0xFF;
                int row = n - y - 1; // GPU rows run bottom to top
                argb[row * n + x] = a << 24 | r << 16 | g << 8 | b;
                if (a != 0) {
                    minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                    minY = Math.min(minY, row); maxY = Math.max(maxY, row);
                }
            }
        }
        if (maxX < 0) {
            LOGGER.warn("{} rendered nothing; skipped", name);
            return;
        }
        int w = maxX - minX + 1, h = maxY - minY + 1;
        double scale = SIZE * FILL / Math.max(w, h);
        int outW = Math.max(1, (int) Math.round(w * scale)), outH = Math.max(1, (int) Math.round(h * scale));
        int offX = (SIZE - outW) / 2, offY = (SIZE - outH) / 2;

        try (NativeImage image = new NativeImage(SIZE, SIZE, true)) {
            for (int oy = 0; oy < outH; oy++) {
                for (int ox = 0; ox < outW; ox++) {
                    // Area filter: average every source pixel under this output pixel, weighting
                    // colour by alpha so transparent pixels don't darken the edges.
                    int x0 = minX + (int) (ox / scale), x1 = Math.max(x0 + 1, minX + (int) ((ox + 1) / scale));
                    int y0 = minY + (int) (oy / scale), y1 = Math.max(y0 + 1, minY + (int) ((oy + 1) / scale));
                    long sa = 0, sr = 0, sg = 0, sb = 0, count = 0;
                    for (int y = y0; y < Math.min(y1, n); y++) {
                        for (int x = x0; x < Math.min(x1, n); x++) {
                            int p = argb[y * n + x];
                            int a = p >>> 24;
                            sa += a;
                            sr += (long) ((p >> 16) & 0xFF) * a;
                            sg += (long) ((p >> 8) & 0xFF) * a;
                            sb += (long) (p & 0xFF) * a;
                            count++;
                        }
                    }
                    if (count == 0 || sa == 0) continue;
                    int a = (int) (sa / count);
                    int r = (int) (sr / sa), g = (int) (sg / sa), b = (int) (sb / sa);
                    image.setPixelRGBA(offX + ox, offY + oy, a << 24 | b << 16 | g << 8 | r);
                }
            }
            Files.createDirectories(outputDir);
            image.writeToFile(outputDir.resolve(name + ".png"));
            written++;
            LOGGER.info("Wrote {}.png", name);
        }
    }
}
