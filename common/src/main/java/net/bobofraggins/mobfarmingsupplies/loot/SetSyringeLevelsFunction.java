package net.bobofraggins.mobfarmingsupplies.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.ExperienceSyringeItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function {@code mobfarmingsupplies:set_syringe_levels}: fills an Experience Syringe with
 * the XP of a random number of levels (counted up from level 0) between {@code min_levels} and
 * {@code max_levels}.
 */
public class SetSyringeLevelsFunction extends LootItemConditionalFunction {

    public static final MapCodec<SetSyringeLevelsFunction> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(i.group(
                    Codec.INT.fieldOf("min_levels").forGetter(f -> f.minLevels),
                    Codec.INT.fieldOf("max_levels").forGetter(f -> f.maxLevels)
            )).apply(i, SetSyringeLevelsFunction::new));

    private final int minLevels;
    private final int maxLevels;

    private SetSyringeLevelsFunction(List<LootItemCondition> predicates, int minLevels, int maxLevels) {
        super(predicates);
        this.minLevels = minLevels;
        this.maxLevels = maxLevels;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        int levels = minLevels + context.getRandom().nextInt(Math.max(1, maxLevels - minLevels + 1));
        stack.set(Registration.EXPERIENCE_SYRINGE_STORED_XP.get(),
                Math.min(xpForLevels(levels), ExperienceSyringeItem.CAPACITY));
        return stack;
    }

    /** Total XP points from level 0 to {@code levels} (vanilla's level curve). */
    static int xpForLevels(int levels) {
        if (levels <= 16) return levels * levels + 6 * levels;
        if (levels <= 31) return (int) (2.5 * levels * levels - 40.5 * levels + 360);
        return (int) (4.5 * levels * levels - 162.5 * levels + 2220);
    }

    @Override
    public MapCodec<SetSyringeLevelsFunction> codec() {
        return CODEC;
    }
}
