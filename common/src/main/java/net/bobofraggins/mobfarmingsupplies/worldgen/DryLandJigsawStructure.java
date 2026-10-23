package net.bobofraggins.mobfarmingsupplies.worldgen;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

/**
 * A jigsaw structure that won't start on water: {@code mobfarmingsupplies:dry_land_jigsaw}, written
 * exactly like {@code minecraft:jigsaw}. Before placing, it looks at the surface at the chunk's
 * centre and {@value #REACH} blocks out each way, and gives the spot up if any of them is water (a
 * river, lake, swamp or the sea). Jigsaw structures placed on the surface heightmap otherwise
 * settle into the water, since the water's top counts as the surface.
 */
public class DryLandJigsawStructure extends Structure {

    public static final MapCodec<DryLandJigsawStructure> CODEC =
            JigsawStructure.CODEC.xmap(DryLandJigsawStructure::new, structure -> structure.jigsaw);

    /** How far from the centre to look for water: enough to cover the outhouses and the picnic. */
    private static final int REACH = 8;

    private final JigsawStructure jigsaw;

    public DryLandJigsawStructure(JigsawStructure jigsaw) {
        super(new StructureSettings(jigsaw.biomes(), jigsaw.spawnOverrides(), jigsaw.step(), jigsaw.terrainAdaptation()));
        this.jigsaw = jigsaw;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int cx = chunk.getMiddleBlockX();
        int cz = chunk.getMiddleBlockZ();
        int[][] offsets = {{0, 0}, {REACH, 0}, {-REACH, 0}, {0, REACH}, {0, -REACH}};
        for (int[] offset : offsets) {
            if (isWater(context, cx + offset[0], cz + offset[1])) return Optional.empty();
        }
        return jigsaw.findValidGenerationPoint(context);
    }

    /** Whether the topmost block of the generated terrain at x, z is a fluid. */
    private static boolean isWater(GenerationContext context, int x, int z) {
        ChunkGenerator generator = context.chunkGenerator();
        int top = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState()) - 1;
        NoiseColumn column = generator.getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        return !column.getBlock(top).getFluidState().isEmpty();
    }

    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                           RandomSource random, BoundingBox box, ChunkPos chunk, PiecesContainer pieces) {
        jigsaw.afterPlace(level, structures, generator, random, box, chunk, pieces);
    }

    @Override
    public StructureType<?> type() {
        return Registration.DRY_LAND_JIGSAW.get();
    }
}
