package net.bobofraggins.mobfarmingsupplies.essencehopper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

/** An essence particle: flies to {@code target} over {@code duration} ticks, in ARGB {@code color}. */
public record EssenceTrailOptions(Vec3 target, int color, int duration) implements ParticleOptions {

    public static final MapCodec<EssenceTrailOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Vec3.CODEC.fieldOf("target").forGetter(EssenceTrailOptions::target),
            Codec.INT.fieldOf("color").forGetter(EssenceTrailOptions::color),
            Codec.INT.fieldOf("duration").forGetter(EssenceTrailOptions::duration)
    ).apply(i, EssenceTrailOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EssenceTrailOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, o -> o.target().x,
            ByteBufCodecs.DOUBLE, o -> o.target().y,
            ByteBufCodecs.DOUBLE, o -> o.target().z,
            ByteBufCodecs.INT, EssenceTrailOptions::color,
            ByteBufCodecs.VAR_INT, EssenceTrailOptions::duration,
            (x, y, z, color, duration) -> new EssenceTrailOptions(new Vec3(x, y, z), color, duration));

    @Override
    public ParticleType<?> getType() {
        return Registration.ESSENCE_TRAIL.get();
    }
}
