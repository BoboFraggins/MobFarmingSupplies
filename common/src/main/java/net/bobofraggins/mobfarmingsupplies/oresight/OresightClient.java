package net.bobofraggins.mobfarmingsupplies.oresight;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.event.events.client.ClientTickEvent;
import java.util.HashSet;
import java.util.Set;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Client side of Oresight: finds blocks of the player's Oresight block near them (or of its whole
 * ore tag; see {@link OresightTargets}) and traces them in blue lines that show through walls.
 *
 * <p>Every {@link #SCAN_INTERVAL} ticks it looks at the blocks within {@link #RANGE} of the
 * player, skipping chunk sections whose palette doesn't contain the block, and turns what it finds
 * into outline edges. Touching blocks are traced as one shape: an edge is drawn only where the
 * surface bends, not between two blocks lying flat against each other. Each loader calls
 * {@link #render} after the world's translucent features are drawn.
 */
public final class OresightClient {

    /** How far from the player, in blocks, along each axis. */
    public static final int RANGE = 32;
    private static final int SCAN_INTERVAL = 10;
    /** At most this many blocks are traced, so a huge deposit can't stall the game. */
    private static final int MAX_BLOCKS = 4096;
    private static final int R = 60, G = 140, B = 255, A = 255;
    private static final float LINE_WIDTH = 2.5f;

    /** Lines drawn over everything: vanilla's line pipeline with the depth test switched off. */
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "pipeline/oresight_lines"))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();

    private static RenderType renderType;

    @Nullable private static Block target;
    /** Edges to draw: x, y, z of the edge's low end and its axis (0 = X, 1 = Y, 2 = Z), four ints each. */
    private static int[] edges = new int[0];
    private static int ticks;

    private OresightClient() {}

    public static void register() {
        ClientTickEvent.CLIENT_POST.register(OresightClient::tick);
    }

    /** Set from the server ({@code OresightPacket}). */
    public static void setTarget(@Nullable Block block) {
        target = block;
        edges = new int[0];
        ticks = SCAN_INTERVAL - 1; // scan on the next tick
    }

    private static boolean active(Minecraft mc) {
        return target != null && mc.player != null && mc.level != null && mc.player.hasEffect(Oresight.effect());
    }

    private static void tick(Minecraft mc) {
        if (!active(mc)) {
            edges = new int[0];
            return;
        }
        if (++ticks % SCAN_INTERVAL != 0) return;
        edges = edgesOf(scan(mc.level, mc.player.blockPosition(), OresightTargets.matcher(target)));
    }

    /** Positions of blocks {@code wanted} within {@link #RANGE} of {@code centre}, as {@link BlockPos#asLong}. */
    private static Set<Long> scan(ClientLevel level, BlockPos centre, java.util.function.Predicate<BlockState> wanted) {
        Set<Long> found = new HashSet<>();
        int minX = centre.getX() - RANGE, maxX = centre.getX() + RANGE;
        int minY = Math.max(centre.getY() - RANGE, level.getMinY()), maxY = Math.min(centre.getY() + RANGE, level.getMaxY());
        int minZ = centre.getZ() - RANGE, maxZ = centre.getZ() + RANGE;
        for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                ChunkAccess chunk = level.getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                for (int sy = minY >> 4; sy <= maxY >> 4; sy++) {
                    int index = level.getSectionIndexFromSectionY(sy);
                    if (index < 0 || index >= chunk.getSections().length) continue;
                    LevelChunkSection section = chunk.getSections()[index];
                    if (section.hasOnlyAir() || !section.maybeHas(wanted)) continue;
                    for (int ly = 0; ly < 16; ly++) {
                        int y = (sy << 4) + ly;
                        if (y < minY || y > maxY) continue;
                        for (int lz = 0; lz < 16; lz++) {
                            int z = (cz << 4) + lz;
                            if (z < minZ || z > maxZ) continue;
                            for (int lx = 0; lx < 16; lx++) {
                                int x = (cx << 4) + lx;
                                if (x < minX || x > maxX) continue;
                                if (wanted.test(section.getBlockState(lx, ly, lz))) {
                                    found.add(BlockPos.asLong(x, y, z));
                                    if (found.size() >= MAX_BLOCKS) return found;
                                }
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    /**
     * The outline of the blocks: each block edge whose four surrounding cells don't form a flat
     * surface or a solid (one or three of them filled, or two diagonally), once each.
     */
    private static int[] edgesOf(Set<Long> blocks) {
        record Edge(int x, int y, int z, int axis) {}
        Set<Edge> drawn = new HashSet<>();
        for (long packed : blocks) {
            int x = BlockPos.getX(packed), y = BlockPos.getY(packed), z = BlockPos.getZ(packed);
            for (int axis = 0; axis < 3; axis++) {
                for (int a = 0; a < 2; a++) {
                    for (int b = 0; b < 2; b++) {
                        // The edge runs along `axis`; a and b pick which corner across the other two.
                        int[] start = {x, y, z};
                        int[] stepA = new int[3], stepB = new int[3];
                        int axisA = (axis + 1) % 3, axisB = (axis + 2) % 3;
                        start[axisA] += a;
                        start[axisB] += b;
                        stepA[axisA] = a == 0 ? -1 : 1;
                        stepB[axisB] = b == 0 ? -1 : 1;
                        boolean c1 = blocks.contains(BlockPos.asLong(x + stepA[0], y + stepA[1], z + stepA[2]));
                        boolean c2 = blocks.contains(BlockPos.asLong(x + stepB[0], y + stepB[1], z + stepB[2]));
                        boolean c3 = blocks.contains(BlockPos.asLong(
                                x + stepA[0] + stepB[0], y + stepA[1] + stepB[1], z + stepA[2] + stepB[2]));
                        int filled = 1 + (c1 ? 1 : 0) + (c2 ? 1 : 0) + (c3 ? 1 : 0);
                        boolean show = filled == 1 || filled == 3 || (filled == 2 && c3);
                        if (show) drawn.add(new Edge(start[0], start[1], start[2], axis));
                    }
                }
            }
        }
        int[] out = new int[drawn.size() * 4];
        int i = 0;
        for (Edge edge : drawn) {
            out[i++] = edge.x();
            out[i++] = edge.y();
            out[i++] = edge.z();
            out[i++] = edge.axis();
        }
        return out;
    }

    private static RenderType renderType() {
        if (renderType == null) {
            renderType = RenderType.create("mobfarmingsupplies_oresight_lines", RenderSetup.builder(PIPELINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.MAIN_TARGET)
                    .createRenderSetup());
        }
        return renderType;
    }

    /** Draws the traced edges, relative to the camera, through everything. */
    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
        Minecraft mc = Minecraft.getInstance();
        int[] lines = edges;
        if (lines.length == 0 || !active(mc)) return;
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        RenderType type = renderType();
        VertexConsumer vc = buffers.getBuffer(type);
        Matrix4f pose = poseStack.last().pose();
        for (int i = 0; i < lines.length; i += 4) {
            float x0 = (float) (lines[i] - camera.x);
            float y0 = (float) (lines[i + 1] - camera.y);
            float z0 = (float) (lines[i + 2] - camera.z);
            int axis = lines[i + 3];
            float dx = axis == 0 ? 1 : 0, dy = axis == 1 ? 1 : 0, dz = axis == 2 ? 1 : 0;
            vc.addVertex(pose, x0, y0, z0).setColor(R, G, B, A).setNormal(dx, dy, dz).setLineWidth(LINE_WIDTH);
            vc.addVertex(pose, x0 + dx, y0 + dy, z0 + dz).setColor(R, G, B, A).setNormal(dx, dy, dz).setLineWidth(LINE_WIDTH);
        }
        buffers.endBatch(type);
    }
}
