package net.bobofraggins.mobfarmingsupplies.disenchanter;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlyTowardsPositionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

/**
 * The XP Powered Disenchanter's runes: vanilla's enchanting table runes (the same particle and
 * glyphs, made by vanilla's own provider) coloured XP Juice green, a little lighter or darker each.
 */
public class XpRuneProvider implements ParticleProvider<SimpleParticleType> {

    private static final float RED = 0x39 / 255f, GREEN = 1f, BLUE = 0x14 / 255f;

    private final FlyTowardsPositionParticle.EnchantProvider vanilla;

    public XpRuneProvider(SpriteSet sprites) {
        this.vanilla = new FlyTowardsPositionParticle.EnchantProvider(sprites);
    }

    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                   double dx, double dy, double dz) {
        Particle particle = vanilla.createParticle(type, level, x, y, z, dx, dy, dz);
        if (particle != null) {
            float shade = 0.6f + level.random.nextFloat() * 0.4f;
            particle.setColor(RED * shade, GREEN * shade, BLUE * shade);
        }
        return particle;
    }
}
