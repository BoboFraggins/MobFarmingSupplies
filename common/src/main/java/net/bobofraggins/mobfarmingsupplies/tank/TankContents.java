package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.fluid.FluidStack;
import net.bobofraggins.mobfarmingsupplies.fluid.FluidStacks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component storing the fluid type and amount for a Tank block item.
 *
 * <p>Also tracks {@code bucketMode}: when true, right-clicking a fluid source block
 * fills/places fluid rather than placing the tank as a block.
 */
public record TankContents(FluidStack storedFluid, long amount, boolean bucketMode) {

    public TankContents(FluidStack storedFluid, long amount) {
        this(storedFluid, amount, false);
    }

    public static final TankContents EMPTY = new TankContents(FluidStack.empty(), 0, false);

    public static final Codec<TankContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    FluidStacks.optionalFieldCodec("fluid").forGetter(TankContents::storedFluid),
                    Codec.LONG.optionalFieldOf("amount", 0L).forGetter(TankContents::amount),
                    Codec.BOOL.optionalFieldOf("bucket_mode", false).forGetter(TankContents::bucketMode))
            .apply(instance, TankContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TankContents> STREAM_CODEC =
            StreamCodec.composite(
                    FluidStacks.OPTIONAL_STREAM_CODEC, TankContents::storedFluid,
                    ByteBufCodecs.VAR_LONG,            TankContents::amount,
                    ByteBufCodecs.BOOL,                TankContents::bucketMode,
                    TankContents::new);

    public boolean isEmpty() {
        return storedFluid.isEmpty() || amount == 0;
    }

    public boolean isLocked() {
        return !storedFluid.isEmpty();
    }
}
