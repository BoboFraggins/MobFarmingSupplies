package net.bobofraggins.mobfarmingsupplies.glamping.present;

import java.util.List;
import net.bobofraggins.mobfarmingsupplies.glamping.NaturalSpawnRolls;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gives a random 5% of naturally-spawning endermen a Present to carry.
 *
 * <p>The enderman only carries a "surprise" Present ({@link PresentBlock#SURPRISE}) - a block
 * state, since an enderman can't carry block-entity data. Its contents are rolled from
 * {@link #rollContents} when it leaves the enderman: in {@link PresentBlock#getDrops} when the
 * enderman dies, or on the next tick after the enderman sets it down.
 *
 * <p>Platform-specific spawn hooks ({@code FinalizeSpawnEvent} on NeoForge, a
 * {@code Mob#finalizeSpawn} mixin on Fabric) filter for genuinely-natural spawns and call
 * {@link #tryGivePresent(EnderMan, RandomSource)}. Clone-O-Matic spawns use
 * {@link #CLONE_O_MATIC_CHANCE} instead (see {@link NaturalSpawnRolls}).
 */
public final class PresentEndermanHandler {

    /** Chance for a naturally-spawned enderman. */
    public static final float NATURAL_SPAWN_CHANCE = 0.05f;
    /** Chance for an enderman made by the Clone-O-Matic. */
    public static final float CLONE_O_MATIC_CHANCE = 0.01f;

    private record Weighted(Block block, int weight) {}

    /** Mostly ordinary blocks: dirt/stone 40%, common ores and andesite 40%, the rest 20%. */
    private static final List<Weighted> CONTENTS = List.of(
            new Weighted(Blocks.DIRT, 20),
            new Weighted(Blocks.STONE, 20),
            new Weighted(Blocks.ANDESITE, 10),
            new Weighted(Blocks.COAL_ORE, 10),
            new Weighted(Blocks.COPPER_ORE, 10),
            new Weighted(Blocks.IRON_ORE, 10),
            new Weighted(Blocks.DIAMOND_ORE, 4),
            new Weighted(Blocks.EMERALD_ORE, 4),
            new Weighted(Blocks.OBSIDIAN, 4),
            new Weighted(Blocks.NETHER_QUARTZ_ORE, 4),
            new Weighted(Blocks.GOLD_ORE, 4));

    private static final int TOTAL_WEIGHT = CONTENTS.stream().mapToInt(Weighted::weight).sum();

    private PresentEndermanHandler() {}

    /** Natural-spawn roll, called from the platform spawn hooks. */
    public static void tryGivePresent(EnderMan enderman, RandomSource random) {
        if (NaturalSpawnRolls.suppressed()) return;
        tryGivePresent(enderman, random, NATURAL_SPAWN_CHANCE);
    }

    public static void tryGivePresent(EnderMan enderman, RandomSource random, float chance) {
        if (enderman.getCarriedBlock() != null) return;
        if (random.nextFloat() >= chance) return;
        enderman.setCarriedBlock(Registration.PRESENT.get().defaultBlockState().setValue(PresentBlock.SURPRISE, true));
    }

    /** A random block for a surprise Present to contain. */
    public static BlockState rollContents(RandomSource random) {
        int r = random.nextInt(TOTAL_WEIGHT);
        for (Weighted entry : CONTENTS) {
            r -= entry.weight();
            if (r < 0) return entry.block().defaultBlockState();
        }
        return Blocks.DIRT.defaultBlockState();
    }
}
