package net.bobofraggins.mobfarmingsupplies.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Lets items be dragged from JEI's ingredient list onto the Filter Scribing Terminal's ghost
 * slot. Only a picture of the item is recorded; nothing is given or taken.
 */
public class FilterScribingTerminalGhostHandler implements IGhostIngredientHandler<FilterScribingTerminalScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(
            FilterScribingTerminalScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        if (ingredient.getItemStack().isEmpty()) return List.of();
        Rect2i area = screen.getGhostSlotArea();
        return List.of(new Target<>() {
            @Override
            public Rect2i getArea() {
                return area;
            }

            @Override
            public void accept(I value) {
                if (value instanceof ItemStack stack) screen.setGhostItem(stack);
            }
        });
    }

    @Override
    public void onComplete() {}
}
