package net.bobofraggins.mobfarmingsupplies.essencehopper;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;

/** Flies an essence from a dead mob to the hopper, as newer versions' vanilla trail particle does. */
public class EssenceTrailParticle extends TextureSheetParticle {

    private static final int FULL_BRIGHT = 0xF000F0;

    private final Vec3 target;

    private EssenceTrailParticle(ClientLevel level, double x, double y, double z, EssenceTrailOptions options) {
        super(level, x, y, z);
        this.target = options.target();
        this.lifetime = options.duration();
        this.quadSize = 0.26f;
        this.gravity = 0f;
        this.hasPhysics = false;
        this.xd = this.yd = this.zd = 0;
        int color = options.color();
        setColor(FastColor.ARGB32.red(color) / 255f, FastColor.ARGB32.green(color) / 255f, FastColor.ARGB32.blue(color) / 255f);
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
        double share = 1.0 / (lifetime - age + 1);
        x += (target.x - x) * share;
        y += (target.y - y) * share;
        z += (target.z - z) * share;
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<EssenceTrailOptions> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(EssenceTrailOptions options, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            EssenceTrailParticle particle = new EssenceTrailParticle(level, x, y, z, options);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
