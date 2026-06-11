package net.bobofraggins.mobfarmingsupplies.dna;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component that attaches mob identity to a {@link DnaSampleItem}.
 *
 * <p>{@link #entityNbt} holds the complete entity save produced by
 * {@link net.minecraft.world.entity.Entity#save(net.minecraft.world.level.storage.ValueOutput)},
 * including the {@code "id"} key needed to reconstruct the entity type.
 * {@link #mobName} is the human-readable label captured at collection time
 * (the mob's custom name if set, otherwise its entity-type translation string).
 */
public record DnaSampleContents(CompoundTag entityNbt, String mobName) {

    public static final Codec<DnaSampleContents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CompoundTag.CODEC.fieldOf("entity_nbt").forGetter(DnaSampleContents::entityNbt),
                    Codec.STRING.fieldOf("mob_name").forGetter(DnaSampleContents::mobName)
            ).apply(instance, DnaSampleContents::new));

    /**
     * Stream codec for network synchronisation.
     * Both {@code ByteBufCodecs.COMPOUND_TAG} and {@code STRING_UTF8} operate on
     * plain {@link ByteBuf}, which satisfies the
     * {@code StreamCodec<? super RegistryFriendlyByteBuf, T>} bound required by
     * {@link net.minecraft.core.component.DataComponentType.Builder#networkSynchronized}.
     */
    public static final StreamCodec<ByteBuf, DnaSampleContents> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG, DnaSampleContents::entityNbt,
                    ByteBufCodecs.STRING_UTF8,   DnaSampleContents::mobName,
                    DnaSampleContents::new);
}
