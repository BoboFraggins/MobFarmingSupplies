package net.bobofraggins.mobfarmingsupplies.matter;

import java.util.List;
import net.minecraft.world.item.Item;

/**
 * One way of getting an item: {@code outputCount} of {@code output} from the given ingredients, plus a
 * fixed {@code cost} (used for drops, which take no ingredients). Each ingredient lists every item that
 * could fill it (one for a plain ingredient, several for a tag), and {@link MatterGraphSolver} prices
 * it by the cheapest.
 */
public record MatterConversion(Item output, int outputCount, List<Input> inputs, long cost) {

    /** Made from ingredients alone. */
    public MatterConversion(Item output, int outputCount, List<Input> inputs) {
        this(output, outputCount, inputs, 0);
    }

    /** Got for a fixed cost, with no ingredients. */
    public static MatterConversion fixed(Item output, long cost) {
        return new MatterConversion(output, 1, List.of(), cost);
    }

    /** {@code count} of whichever of {@code alternatives} is cheapest. */
    public record Input(List<Item> alternatives, int count) {}

    /** {@code output} made one for one from {@code input}. */
    public static MatterConversion oneForOne(Item output, Item input) {
        return new MatterConversion(output, 1, List.of(new Input(List.of(input), 1)));
    }
}
