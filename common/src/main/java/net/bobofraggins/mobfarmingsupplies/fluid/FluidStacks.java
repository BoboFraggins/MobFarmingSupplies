package net.bobofraggins.mobfarmingsupplies.fluid;

import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSStreamCodecs;
import com.mojang.serialization.MapCodec;
import dev.architectury.fluid.FluidStack;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;

import java.util.Optional;

/**
 * Cross-platform fluid codec helpers to fill gaps in Architectury's {@link FluidStack} API.
 *
 * <p>Architectury's {@code FluidStack} only provides {@code CODEC} and {@code STREAM_CODEC}.
 * NeoForge's {@code FluidStack.OPTIONAL_CODEC} and {@code OPTIONAL_STREAM_CODEC} have no
 * equivalent — this class provides them.
 */
public final class FluidStacks {

    private FluidStacks() {}

    /**
     * Returns a {@link MapCodec} for a fluid field that treats absent as empty.
     *
     * <p>Encoding: empty FluidStack → field absent; non-empty → field present with
     * the standard {@link FluidStack#CODEC} format.
     * On NeoForge this produces the same on-disk format as NeoForge's
     * {@code FluidStack.OPTIONAL_CODEC}.
     *
     * <p>Usage in a {@link com.mojang.serialization.codecs.RecordCodecBuilder}:
     * <pre>{@code
     *   FluidStacks.optionalFieldCodec("fluid").forGetter(MyRecord::storedFluid)
     * }</pre>
     */
    public static MapCodec<FluidStack> optionalFieldCodec(String fieldName) {
        return MFSCodecs.FLUID_STACK.optionalFieldOf(fieldName)
                .xmap(opt -> opt.orElse(FluidStack.empty()),
                      fs  -> fs.isEmpty() ? Optional.empty() : Optional.of(fs));
    }

    /**
     * Stream codec that safely handles empty FluidStacks over the network.
     *
     * <p>Wire format: {@code boolean present}, then the full {@link FluidStack#STREAM_CODEC}
     * payload only if {@code present == true}.
     */
    public static final StreamCodec<FriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC =
            StreamCodec.of(
                    (buf, fs) -> {
                        buf.writeBoolean(!fs.isEmpty());
                        if (!fs.isEmpty()) MFSStreamCodecs.FLUID_STACK.encode(buf, fs);
                    },
                    buf -> buf.readBoolean() ? MFSStreamCodecs.FLUID_STACK.decode(buf) : FluidStack.empty()
            );
}
