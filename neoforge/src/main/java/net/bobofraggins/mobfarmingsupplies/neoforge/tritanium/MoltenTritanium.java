package net.bobofraggins.mobfarmingsupplies.neoforge.tritanium;

import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.ModCompatRegistration;
import org.jetbrains.annotations.Nullable;

/**
 * Molten Tritanium and Molten Tritanium Alloy, which Productive Metalworks melts, alloys and casts.
 * Only registered when Productive Metalworks is installed ({@link #register}); their recipes are in
 * {@code data/mobfarmingsupplies/recipe/productivemetalworks}.
 */
public final class MoltenTritanium {

    private MoltenTritanium() {}

    @Nullable public static MoltenMetal TRITANIUM;
    @Nullable public static MoltenMetal ALLOY;

    public static void register() {
        TRITANIUM = new MoltenMetal("tritanium");
        ALLOY = new MoltenMetal("tritanium_alloy");
        ModCompatRegistration.addTritaniumItem(TRITANIUM.bucket);
        ModCompatRegistration.addTritaniumAlloyItem(ALLOY.bucket);
    }

    public static List<MoltenMetal> all() {
        return TRITANIUM == null ? List.of() : List.of(TRITANIUM, ALLOY);
    }
}
