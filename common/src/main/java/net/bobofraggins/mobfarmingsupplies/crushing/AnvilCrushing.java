package net.bobofraggins.mobfarmingsupplies.crushing;

import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * Applies {@link AnvilCrushingRecipe}s when a falling anvil lands (or breaks on landing):
 * every item entity inside the block space it came down in is replaced by the recipe result,
 * one-for-one per item. Called from {@code AnvilBlockMixin}; server side only.
 */
public final class AnvilCrushing {

    private AnvilCrushing() {}

    public static void crush(ServerLevel level, BlockPos pos) {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos),
                ie -> ie.isAlive() && !ie.getItem().isEmpty());
        for (ItemEntity ie : items) {
            ItemStack stack = ie.getItem();
            SingleRecipeInput input = new SingleRecipeInput(stack);
            Optional<RecipeHolder<AnvilCrushingRecipe>> match = level.recipeAccess()
                    .getRecipeFor(Registration.ANVIL_CRUSHING_TYPE.get(), input, level);
            if (match.isEmpty()) continue;

            AnvilCrushingRecipe recipe = match.get().value();
            ItemStack result = recipe.assemble(input);
            int remaining = stack.getCount() * recipe.resultCountPerItem();
            ie.discard();
            while (remaining > 0) {
                int count = Math.min(remaining, result.getMaxStackSize());
                ItemEntity out = new ItemEntity(level, ie.getX(), ie.getY(), ie.getZ(), result.copyWithCount(count));
                out.setDeltaMovement(ie.getDeltaMovement());
                out.setDefaultPickUpDelay();
                level.addFreshEntity(out);
                remaining -= count;
            }
        }
    }
}
