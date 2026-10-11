package net.bobofraggins.mobfarmingsupplies.circuitinscriber;

import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * The client's copy of the Circuit Inscribing recipes, for JEI. Clients aren't sent recipes by
 * default; the server sends this type (NeoForge's datapack sync, Fabric's recipe sync), and each
 * platform stores what arrives here before JEI starts.
 */
public final class ClientInscribingRecipes {

    private static volatile List<RecipeHolder<CircuitInscribingRecipe>> recipes = List.of();

    private ClientInscribingRecipes() {}

    public static List<RecipeHolder<CircuitInscribingRecipe>> get() {
        return recipes;
    }

    public static void set(Collection<RecipeHolder<CircuitInscribingRecipe>> received) {
        recipes = List.copyOf(received);
    }
}
