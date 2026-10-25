package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * Replaces one block in a structure with another, {@code chance} of the time, as it's placed. The
 * replacement keeps every block state property the two have in common (a giant ornament's facing,
 * already turned with the structure) and the block entity data. Vanilla's rule processor sets a
 * fixed state instead, which would face every replacement the same way.
 *
 * <p>With {@code owned_by_known_player}, the replacement is a Giant Player Lawn Ornament given to a
 * random player who has connected to the world ({@link KnownPlayers}); with nobody to give it to,
 * nothing is swapped.
 */
public class OrnamentSwapProcessor extends StructureProcessor {

    public static final MapCodec<OrnamentSwapProcessor> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("from").forGetter(p -> p.from),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("to").forGetter(p -> p.to),
            Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(p -> p.chance),
            Codec.BOOL.optionalFieldOf("owned_by_known_player", false).forGetter(p -> p.ownedByKnownPlayer)
    ).apply(i, OrnamentSwapProcessor::new));

    private final Block from;
    private final Block to;
    private final float chance;
    private final boolean ownedByKnownPlayer;

    public OrnamentSwapProcessor(Block from, Block to, float chance, boolean ownedByKnownPlayer) {
        this.from = from;
        this.to = to;
        this.chance = chance;
        this.ownedByKnownPlayer = ownedByKnownPlayer;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level, BlockPos offset, BlockPos pivot,
            StructureTemplate.StructureBlockInfo original, StructureTemplate.StructureBlockInfo processed,
            StructurePlaceSettings settings) {
        if (!processed.state().is(from)) return processed;
        // Salted with the replacement, so several swaps on one block don't all draw the same number
        // when the structure's random is seeded by position.
        RandomSource random = RandomSource.create(
                settings.getRandom(processed.pos()).nextLong() ^ BuiltInRegistries.BLOCK.getKey(to).hashCode());
        if (random.nextFloat() >= chance) return processed;
        CompoundTag nbt = processed.nbt() == null ? new CompoundTag() : processed.nbt().copy();
        if (ownedByKnownPlayer) {
            Optional<ResolvableProfile> owner = KnownPlayers.random(random);
            if (owner.isEmpty()) return processed;
            ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, owner.get())
                    .ifSuccess(tag -> nbt.put(GiantOrnamentBlockEntity.TAG_PROFILE, tag));
        }
        BlockState swapped = to.defaultBlockState();
        for (Property<?> property : processed.state().getProperties()) {
            swapped = copy(processed.state(), swapped, property);
        }
        return new StructureTemplate.StructureBlockInfo(processed.pos(), swapped, nbt);
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState source, BlockState target, Property<T> property) {
        return target.hasProperty(property) ? target.setValue(property, source.getValue(property)) : target;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Registration.ORNAMENT_SWAP_PROCESSOR.get();
    }
}
