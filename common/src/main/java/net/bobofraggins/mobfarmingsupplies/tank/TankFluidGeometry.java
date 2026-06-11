package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/**
 * Shared geometry for rendering the Tank's fluid fill as a translucent cube.
 *
 * <p>Used by both the block-entity renderer and the item special-model renderer,
 * on both NeoForge and Fabric.
 */
public final class TankFluidGeometry {

    /** Floor and ceiling of the renderable interior, in block units. */
    public static final float FLOOR = 1f / 16f;
    public static final float CEIL  = 15f / 16f;
    public static final float H     = CEIL - FLOOR;

    private TankFluidGeometry() {}

    /**
     * Emits a five-faced translucent cube (no bottom) representing the fluid fill level.
     * The cube spans from {@link #FLOOR} on all sides to {@code fillTop} on the Y axis.
     */
    @SuppressWarnings("java:S107")
    public static void renderCubeFill(
            VertexConsumer vc,
            Matrix4f mat,
            int r, int g, int b, int a,
            int light, int overlay,
            float uL, float vT, float uR, float vB,
            float fillTop) {
        // North face (z = FLOOR)
        quadFluid(vc, mat, r, g, b, a, light, overlay, uL, vT, uR, vB,
                CEIL,  FLOOR, FLOOR,
                FLOOR, FLOOR, FLOOR,
                FLOOR, fillTop, FLOOR,
                CEIL,  fillTop, FLOOR,
                0, 0, -1);
        // South face (z = CEIL)
        quadFluid(vc, mat, r, g, b, a, light, overlay, uL, vT, uR, vB,
                FLOOR, FLOOR, CEIL,
                CEIL,  FLOOR, CEIL,
                CEIL,  fillTop, CEIL,
                FLOOR, fillTop, CEIL,
                0, 0, 1);
        // West face (x = FLOOR)
        quadFluid(vc, mat, r, g, b, a, light, overlay, uL, vT, uR, vB,
                FLOOR, FLOOR, FLOOR,
                FLOOR, FLOOR, CEIL,
                FLOOR, fillTop, CEIL,
                FLOOR, fillTop, FLOOR,
                -1, 0, 0);
        // East face (x = CEIL)
        quadFluid(vc, mat, r, g, b, a, light, overlay, uL, vT, uR, vB,
                CEIL, FLOOR, CEIL,
                CEIL, FLOOR, FLOOR,
                CEIL, fillTop, FLOOR,
                CEIL, fillTop, CEIL,
                1, 0, 0);
        // Top face
        quadFluid(vc, mat, r, g, b, a, light, overlay, uL, vT, uR, vB,
                FLOOR, fillTop, FLOOR,
                FLOOR, fillTop, CEIL,
                CEIL,  fillTop, CEIL,
                CEIL,  fillTop, FLOOR,
                0, 1, 0);
    }

    @SuppressWarnings("java:S107")
    private static void quadFluid(
            VertexConsumer vc,
            Matrix4f mat,
            int r, int g, int b, int a,
            int light, int overlay,
            float uLeft, float vTop, float uRight, float vBottom,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float nx, float ny, float nz) {
        vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setUv(uLeft,  vBottom).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setUv(uRight, vBottom).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setUv(uRight, vTop).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
        vc.addVertex(mat, x3, y3, z3).setColor(r, g, b, a).setUv(uLeft,  vTop).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
    }
}
