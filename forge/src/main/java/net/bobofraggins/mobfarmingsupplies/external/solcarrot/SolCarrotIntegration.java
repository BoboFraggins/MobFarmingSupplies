package net.bobofraggins.mobfarmingsupplies.external.solcarrot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Set;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * Optional integration with Spice of Life: Carrot Edition.
 *
 * <p>All access to SoLC classes is via reflection so no compile-time dependency is needed.
 * Every public method is safe to call regardless of whether the mod is installed.
 */
public final class SolCarrotIntegration {

    private SolCarrotIntegration() {}

    private static boolean initialized = false;
    /** {@code FoodList.get(Player)}: SoL: Carrot Edition 1.20.1 keeps the list in a capability. */
    private static java.lang.reflect.Method foodListGetter;
    private static Field foodsField;
    private static Constructor<?> foodInstanceCtor;

    @SuppressWarnings("unchecked")
    private static void init() {
        if (initialized) return;
        initialized = true;
        if (!ModList.get().isLoaded("solcarrot")) return;
        try {
            Class<?> foodListClass = Class.forName("com.cazsius.solcarrot.tracking.FoodList");
            foodListGetter = foodListClass.getMethod("get", Player.class);
            foodsField = foodListClass.getDeclaredField("foods");
            foodsField.setAccessible(true);

            Class<?> foodInstanceClass = Class.forName("com.cazsius.solcarrot.tracking.FoodInstance");
            foodInstanceCtor = foodInstanceClass.getConstructor(Item.class);
        } catch (Exception e) {
            foodListGetter = null;
            foodsField = null;
            foodInstanceCtor = null;
        }
    }

    /** Returns true if SoLC is installed and its API is accessible. */
    public static boolean isInstalled() {
        init();
        return foodListGetter != null;
    }

    /**
     * Returns true if the player has not yet eaten this food according to SoLC.
     * Always returns false when SoLC is not installed.
     */
    @SuppressWarnings("unchecked")
    public static boolean isNewFood(Player player, ItemStack food) {
        init();
        if (foodListGetter == null || foodsField == null || foodInstanceCtor == null) return false;
        try {
            Object foodList = foodListGetter.invoke(null, player);
            Set<?> foods = (Set<?>) foodsField.get(foodList);
            Object fi = foodInstanceCtor.newInstance(food.getItem());
            return !foods.contains(fi);
        } catch (Exception e) {
            return false;
        }
    }
}
