package net.bobofraggins.mobfarmingsupplies.shared.compat;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Entity ids or a tag, as written in data ({@code "#minecraft:raiders"}, {@code "minecraft:cow"} or a
 * list). 1.20.5+ reads these as a {@code HolderSet}, which needs the registries; 1.20.1 recipes are
 * read without them, so this keeps the tag by name and checks it when asked.
 */
public record EntitySet(Optional<TagKey<EntityType<?>>> tag, List<EntityType<?>> types) {

    private static final Codec<EntityType<?>> TYPE = BuiltInRegistries.ENTITY_TYPE.byNameCodec();

    public static final Codec<EntitySet> CODEC = Codec.either(TagKey.hashedCodec(Registries.ENTITY_TYPE),
                    Codec.either(TYPE, TYPE.listOf()))
            .xmap(e -> e.map(tag -> new EntitySet(Optional.of(tag), List.of()),
                            ids -> new EntitySet(Optional.empty(), ids.map(List::of, List::copyOf))),
                    set -> set.tag().<Either<TagKey<EntityType<?>>, Either<EntityType<?>, List<EntityType<?>>>>>map(Either::left)
                            .orElseGet(() -> Either.right(Either.right(set.types()))));

    public static final StreamCodec<FriendlyByteBuf, EntitySet> STREAM_CODEC = StreamCodec.of((buf, set) -> {
        buf.writeBoolean(set.tag().isPresent());
        if (set.tag().isPresent()) {
            buf.writeResourceLocation(set.tag().get().location());
        } else {
            buf.writeVarInt(set.types().size());
            for (EntityType<?> type : set.types()) buf.writeVarInt(BuiltInRegistries.ENTITY_TYPE.getId(type));
        }
    }, buf -> {
        if (buf.readBoolean()) {
            ResourceLocation id = buf.readResourceLocation();
            return new EntitySet(Optional.of(TagKey.create(Registries.ENTITY_TYPE, id)), List.of());
        }
        int n = buf.readVarInt();
        EntityType<?>[] types = new EntityType<?>[n];
        for (int i = 0; i < n; i++) types[i] = BuiltInRegistries.ENTITY_TYPE.byId(buf.readVarInt());
        return new EntitySet(Optional.empty(), List.of(types));
    });

    public boolean contains(EntityType<?> type) {
        return tag.map(type::is).orElseGet(() -> types.contains(type));
    }
}
