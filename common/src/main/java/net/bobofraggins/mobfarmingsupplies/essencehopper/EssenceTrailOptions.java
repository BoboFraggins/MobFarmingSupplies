package net.bobofraggins.mobfarmingsupplies.essencehopper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ByteBufCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.world.phys.Vec3;

/** An essence particle: flies to {@code target} over {@code duration} ticks, in ARGB {@code color}. */
public record EssenceTrailOptions(Vec3 target, int color, int duration) implements ParticleOptions {

    public static final MapCodec<EssenceTrailOptions> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Vec3.CODEC.fieldOf("target").forGetter(EssenceTrailOptions::target),
            Codec.INT.fieldOf("color").forGetter(EssenceTrailOptions::color),
            Codec.INT.fieldOf("duration").forGetter(EssenceTrailOptions::duration)
    ).apply(i, EssenceTrailOptions::new));

    public static final StreamCodec<FriendlyByteBuf, EssenceTrailOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, o -> o.target().x,
            ByteBufCodecs.DOUBLE, o -> o.target().y,
            ByteBufCodecs.DOUBLE, o -> o.target().z,
            ByteBufCodecs.INT, EssenceTrailOptions::color,
            ByteBufCodecs.VAR_INT, EssenceTrailOptions::duration,
            (x, y, z, color, duration) -> new EssenceTrailOptions(new Vec3(x, y, z), color, duration));

    /** 1.20.1 reads particle options from commands and packets through one of these. */
    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<EssenceTrailOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public EssenceTrailOptions fromCommand(ParticleType<EssenceTrailOptions> type, com.mojang.brigadier.StringReader reader)
                throws com.mojang.brigadier.exceptions.CommandSyntaxException {
            reader.expect(' ');
            double x = reader.readDouble();
            reader.expect(' ');
            double y = reader.readDouble();
            reader.expect(' ');
            double z = reader.readDouble();
            reader.expect(' ');
            int color = reader.readInt();
            reader.expect(' ');
            int duration = reader.readInt();
            return new EssenceTrailOptions(new Vec3(x, y, z), color, duration);
        }

        @Override
        public EssenceTrailOptions fromNetwork(ParticleType<EssenceTrailOptions> type, FriendlyByteBuf buf) {
            return STREAM_CODEC.decode(buf);
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        STREAM_CODEC.encode(buf, this);
    }

    @Override
    public String writeToString() {
        return net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.getKey(getType()) + " " + target.x + " "
                + target.y + " " + target.z + " " + color + " " + duration;
    }

    @Override
    public ParticleType<?> getType() {
        return Registration.ESSENCE_TRAIL.get();
    }
}
