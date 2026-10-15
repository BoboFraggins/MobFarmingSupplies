package net.bobofraggins.mobfarmingsupplies.mobharvester;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/**
 * The Mob Harvester's Fire Aspect Upgrade: while a harvester with one attacks, whatever the mobs it
 * kills drop from their loot tables comes out cooked. Each drop with a smelting, smoking or campfire
 * recipe is replaced by that recipe's result, as many as there were. Gear the mob was wearing isn't
 * loot and isn't cooked. Kills drop their loot during the attack itself, so a flag held for the
 * length of the attack is enough to tell which drops are the harvester's.
 */
public final class FireAspectCooking {

    private static final ThreadLocal<Boolean> COOKING = ThreadLocal.withInitial(() -> false);
    private static final List<RecipeType<? extends AbstractCookingRecipe>> RECIPES =
            List.of(RecipeType.SMELTING, RecipeType.SMOKING, RecipeType.CAMPFIRE_COOKING);

    private FireAspectCooking() {}

    /** Runs {@code attack} with its kills' drops cooked. */
    public static void during(Runnable attack) {
        COOKING.set(true);
        try {
            attack.run();
        } finally {
            COOKING.set(false);
        }
    }

    /** {@code drops}, cooking each stack first if a harvester with the upgrade is attacking. */
    public static Consumer<ItemStack> wrap(ServerLevel level, Consumer<ItemStack> drops) {
        if (!COOKING.get()) return drops;
        return stack -> drops.accept(cooked(level, stack));
    }

    private static ItemStack cooked(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return stack;
        SingleRecipeInput input = new SingleRecipeInput(stack.copyWithCount(1));
        for (RecipeType<? extends AbstractCookingRecipe> type : RECIPES) {
            Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> recipe =
                    level.getRecipeManager().getRecipeFor(type, input, level);
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().assemble(input, level.registryAccess());
                if (!result.isEmpty()) return result.copyWithCount(result.getCount() * stack.getCount());
            }
        }
        return stack;
    }
}
