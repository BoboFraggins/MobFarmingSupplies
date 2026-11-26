package net.bobofraggins.mobfarmingsupplies.loot;

import net.bobofraggins.mobfarmingsupplies.shared.compat.LootFunctions;
import net.minecraft.util.GsonHelper;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
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

    public static final LootItemConditionalFunction.Serializer<SetSyringeLevelsFunction> SERIALIZER =
            LootFunctions.serializer((json, conditions) -> new SetSyringeLevelsFunction(conditions,
                    GsonHelper.getAsInt(json, "min_levels"), GsonHelper.getAsInt(json, "max_levels")), (json, f) -> {
                json.addProperty("min_levels", f.minLevels);
                json.addProperty("max_levels", f.maxLevels);
            });

    private final int minLevels;
    private final int maxLevels;

    private SetSyringeLevelsFunction(LootItemCondition[] predicates, int minLevels, int maxLevels) {
        super(predicates);
        this.minLevels = minLevels;
        this.maxLevels = maxLevels;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        int levels = minLevels + context.getRandom().nextInt(Math.max(1, maxLevels - minLevels + 1));
        ItemComponents.set(stack, Registration.EXPERIENCE_SYRINGE_STORED_XP.get(),
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
    public LootItemFunctionType getType() {
        return Registration.SET_SYRINGE_LEVELS.get();
    }
}
