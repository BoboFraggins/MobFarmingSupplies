package net.bobofraggins.mobfarmingsupplies.gravitationalanomaly;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * The Gravitational Anomaly's particles: hot gas drawn in from all around, like a nether portal's,
 * and sparks shot out along its jets.
 */
public final class AnomalyParticles {

    private static final int FULL_BRIGHT = 0xF000F0;

    private AnomalyParticles() {}

    /**
     * Yellow, orange or red gas falling in. Spawned at the hole's centre with its starting offset as
     * its "speed", like a portal particle; it spirals in clockwise (seen from above), slowly at
     * first, then faster, shrinking as it goes.
     */
    public static final class Accretion extends SingleQuadParticle {

        private static final float[] YELLOW = {1f, 0.86f, 0.3f}, ORANGE = {1f, 0.52f, 0.12f}, RED = {0.85f, 0.16f, 0.08f};
        /** How far round it spirals on its way in, in radians. */
        private static final float SPIRAL = 2.5f;

        private final double centreX, centreY, centreZ, offsetX, offsetY, offsetZ;
        private final float size;

        Accretion(ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                  TextureAtlasSprite sprite, RandomSource random) {
            super(level, x + dx, y + dy, z + dz, sprite);
            centreX = x;
            centreY = y;
            centreZ = z;
            offsetX = dx;
            offsetY = dy;
            offsetZ = dz;
            xd = yd = zd = 0;
            hasPhysics = false;
            lifetime = 50 + random.nextInt(30);
            size = 0.06f + random.nextFloat() * 0.05f;
            quadSize = size;
            float t = random.nextFloat();
            float[] a = t < 0.5f ? YELLOW : ORANGE, b = t < 0.5f ? ORANGE : RED;
            float k = t < 0.5f ? t * 2 : (t - 0.5f) * 2;
            rCol = Mth.lerp(k, a[0], b[0]);
            gCol = Mth.lerp(k, a[1], b[1]);
            bCol = Mth.lerp(k, a[2], b[2]);
            alpha = 0;
        }

        @Override
        public void tick() {
            xo = x;
            yo = y;
            zo = z;
            if (age++ >= lifetime) {
                remove();
                return;
            }
            float f = (float) age / lifetime;
            float left = 1 - f * f;
            float turn = SPIRAL * f;
            float cos = Mth.cos(turn), sin = Mth.sin(turn);
            x = centreX + (offsetX * cos - offsetZ * sin) * left;
            y = centreY + offsetY * left;
            z = centreZ + (offsetX * sin + offsetZ * cos) * left;
            alpha = Math.min(1, age / 10f);
        }

        @Override
        public float getQuadSize(float partialTick) {
            float f = (age + partialTick) / lifetime;
            return size * Math.min(1, 4 * (1 - f));
        }

        @Override
        protected int getLightCoords(float partialTick) {
            return FULL_BRIGHT;
        }

        @Override
        protected Layer getLayer() {
            return Layer.TRANSLUCENT;
        }
    }

    /** A bluish-white spark shot out along a jet, fading fast. */
    public static final class Jet extends SingleQuadParticle {

        private final float size;

        Jet(ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
            TextureAtlasSprite sprite, RandomSource random) {
            super(level, x, y, z, sprite);
            xd = dx;
            yd = dy;
            zd = dz;
            hasPhysics = false;
            gravity = 0;
            friction = 0.97f;
            lifetime = 6 + random.nextInt(5);
            size = 0.05f + random.nextFloat() * 0.04f;
            quadSize = size;
            float white = random.nextFloat();
            rCol = Mth.lerp(white, 0.6f, 0.95f);
            gCol = Mth.lerp(white, 0.78f, 0.97f);
            bCol = 1f;
        }

        @Override
        public void tick() {
            super.tick();
            alpha = 1 - (float) age / lifetime;
        }

        @Override
        protected int getLightCoords(float partialTick) {
            return FULL_BRIGHT;
        }

        @Override
        protected Layer getLayer() {
            return Layer.TRANSLUCENT;
        }
    }

    public record AccretionProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz, RandomSource random) {
            return new Accretion(level, x, y, z, dx, dy, dz, sprites.get(random), random);
        }
    }

    public record JetProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz, RandomSource random) {
            return new Jet(level, x, y, z, dx, dy, dz, sprites.get(random), random);
        }
    }
}
