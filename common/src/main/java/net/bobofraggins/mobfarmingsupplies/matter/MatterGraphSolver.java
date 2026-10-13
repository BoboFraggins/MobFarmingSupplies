package net.bobofraggins.mobfarmingsupplies.matter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;

/**
 * Works out matter values for items that have none set, from the ways they can be made
 * ({@link MatterConversion}s). The approach follows <a href="https://github.com/sinkillerj/ProjectE">ProjectE</a>
 * (MIT License), whose EMC system solves the same problem; thanks to the ProjectE team.
 *
 * <p>Every conversion is priced from the values known so far, and an item takes the cheapest price
 * any conversion gives it. That repeats until nothing changes (at most once per conversion), so a
 * value can travel down a chain of recipes of any length. Set values are never replaced. Values are
 * whole numbers per item, rounded up; an item's components don't change its value.
 */
public final class MatterGraphSolver {

    private final Map<Item, Long> values;
    private final Map<Item, Long> fixed;

    /** {@code setValues} are known in advance and never changed. */
    public MatterGraphSolver(Map<Item, Long> setValues) {
        this.fixed = Map.copyOf(setValues);
        this.values = new HashMap<>(setValues);
    }

    /** The values worked out, not counting the set ones. */
    public Map<Item, Long> solve(List<MatterConversion> conversions) {
        int passes = Math.max(1, conversions.size());
        for (int pass = 0; pass < passes; pass++) {
            boolean changed = false;
            for (MatterConversion conversion : conversions) {
                if (fixed.containsKey(conversion.output())) continue;
                Long price = price(conversion);
                if (price == null) continue;
                Long known = values.get(conversion.output());
                if (known == null || price < known) {
                    values.put(conversion.output(), price);
                    changed = true;
                }
            }
            if (!changed) break;
        }
        Map<Item, Long> worked = new HashMap<>(values);
        worked.keySet().removeAll(fixed.keySet());
        return worked;
    }

    /** What one of the output costs, or null while any input has no known value. */
    private Long price(MatterConversion conversion) {
        long total = conversion.cost();
        for (MatterConversion.Input input : conversion.inputs()) {
            Long cheapest = null;
            for (Item item : input.alternatives()) {
                Long value = values.get(item);
                if (value != null && (cheapest == null || value < cheapest)) cheapest = value;
            }
            if (cheapest == null) return null;
            total += cheapest * input.count();
        }
        return (total + conversion.outputCount() - 1) / conversion.outputCount();
    }
}
