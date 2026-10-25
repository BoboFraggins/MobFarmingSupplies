package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.ui.Dialog;
import net.bobofraggins.mobfarmingsupplies.shared.ui.FilterGhost;
import net.bobofraggins.mobfarmingsupplies.shared.ui.PlayerInventoryPane;
import net.bobofraggins.mobfarmingsupplies.shared.ui.ToggleSwitch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * JEI page for where Item Filters go: one per block that takes them, drawn in the mod's dialog
 * titled with the block, the block beside its AND / OR toggle, then its row of filter slots with a
 * few filters in it (packed to the left, so it doesn't look as if the slot matters) and ghosts in
 * the rest. Shown as a use of the Item Filter and of each block.
 *
 * <p>Each block's page shows one AND / OR setting and one number of filters, picked at random but
 * the same every time (seeded by the block), so the pages differ without flickering.
 */
public final class FilteringCategory implements IRecipeCategory<FilteringCategory.Display> {

    public static final RecipeType<Display> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "filtering", Display.class);

    /** One block's page: the block, AND (or OR), and the filters in each filled slot (cycled). */
    public record Display(ItemStack block, boolean and, List<List<ItemStack>> filters) {}

    private static final int WIDTH = PlayerInventoryPane.WIDTH;
    private static final int SLOT = 18;
    private static final int SLOTS = 9;
    private static final int SLOTS_LEFT = 8;
    private static final int PADDING = 4;
    /** The block's slot and the toggle, side by side. */
    private static final int TOP_ROW_Y = Dialog.TITLE_H + PADDING;
    private static final int SLOTS_Y = TOP_ROW_Y + SLOT + PADDING;
    private static final int BODY_H = SLOTS_Y + SLOT + PADDING - Dialog.TITLE_H;
    private static final int TEXT_COLOR = 0xFF404040;

    private final Component title = Component.translatable("jei.mobfarmingsupplies.filtering");
    private final IDrawable icon;
    private final Dialog dialog = new Dialog(Dialog.blankPane(WIDTH, BODY_H));

    public FilteringCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemLike(Registration.ITEM_FILTER.get());
        dialog.init(0, 0);
    }

    /** A page for every block that takes Item Filters. */
    public static List<Display> displays() {
        List<ItemStack> examples = FilterScribingExamples.create().examples().stream()
                .map(FilterScribingExamples.Example::filter)
                .toList();
        List<Display> out = new ArrayList<>();
        for (Item block : List.of(Registration.OMNI_HOPPER_ITEM.get(), Registration.EINSTEIN_ROSEN_BRIDGE_ITEM.get(),
                Registration.LOGISTIC_SORTER_ITEM.get(), Registration.BLOCK_BREAKER_ITEM.get())) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(block);
            Random random = new Random(id.hashCode());
            int count = 1 + random.nextInt(3);
            List<List<ItemStack>> filters = new ArrayList<>();
            for (int slot = 0; slot < count; slot++) {
                // Each slot cycles through the examples from its own starting point, so they differ.
                List<ItemStack> cycle = new ArrayList<>(examples);
                Collections.rotate(cycle, -(slot * examples.size() / count + random.nextInt(examples.size())));
                filters.add(cycle);
            }
            out.add(new Display(new ItemStack(block), random.nextBoolean(), filters));
        }
        return out;
    }

    @Override
    public RecipeType<Display> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return dialog.totalWidth();
    }

    @Override
    public int getHeight() {
        return dialog.totalHeight();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    /** Left of the block slot and the toggle, the pair centred. */
    private static int blockX(Font font) {
        int group = SLOT + PADDING + ToggleSwitch.WIDTH;
        return (WIDTH - group) / 2;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display display, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, blockX(Minecraft.getInstance().font) + 1, TOP_ROW_Y + 1)
                .setStandardSlotBackground()
                .addItemStack(display.block());
        for (int slot = 0; slot < display.filters().size(); slot++) {
            builder.addSlot(RecipeIngredientRole.INPUT, SLOTS_LEFT + slot * SLOT, SLOTS_Y + 1)
                    .addItemStacks(display.filters().get(slot));
        }
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        // Mouse far away: purely visual, no hover highlights.
        dialog.render(g, font, display.block().getHoverName(), -1000, -1000, 0);
        int toggleX = blockX(font) + SLOT + PADDING;
        int toggleY = TOP_ROW_Y + (SLOT - ToggleSwitch.HEIGHT) / 2;
        ToggleSwitch.draw(g, font, toggleX, toggleY, !display.and(), Component.translatable(
                display.and() ? "gui.mobfarmingsupplies.toggle.and" : "gui.mobfarmingsupplies.toggle.or"));
        for (int slot = 0; slot < SLOTS; slot++) {
            int x = SLOTS_LEFT + slot * SLOT;
            PlayerInventoryPane.drawSlotBg(g, x, SLOTS_Y + 1);
            if (slot >= display.filters().size()) FilterGhost.draw(g, x, SLOTS_Y + 1);
        }
    }
}
