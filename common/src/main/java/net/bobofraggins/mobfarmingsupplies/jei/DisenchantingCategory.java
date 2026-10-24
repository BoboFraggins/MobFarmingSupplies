package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.disenchanter.DisenchanterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * JEI page for the XP Powered Disenchanter, one row per enchantment and level: an item carrying
 * it (cycling, in a shuffled order, through the items it can go on), a book, an arrow, and the
 * enchanted book pulled off, with the experience and time below, as on the incubating page.
 */
public final class DisenchantingCategory implements IRecipeCategory<DisenchantingCategory.Display> {

    public static final RecipeType<Display> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "disenchanting", Display.class);

    /** One row: items carrying the enchantment, and the book it comes off onto. */
    public record Display(List<ItemStack> items, ItemStack book) {}

    private static final int SLOT = 18;
    private static final int ITEM_X = 0, BOOK_X = 24, ARROW_X = 46, OUTPUT_X = 76;
    private static final int ARROW_W = 24;
    private static final int WIDTH = OUTPUT_X + SLOT;
    private static final int TEXT_Y = SLOT + 4;
    private static final int HEIGHT = TEXT_Y + 9;
    private static final int TEXT_COLOR = 0xFF808080;

    private final Component title = Component.translatable("jei.mobfarmingsupplies.disenchanting");
    private final IDrawable icon;
    private final IGuiHelper helper;

    public DisenchantingCategory(IGuiHelper helper) {
        this.helper = helper;
        this.icon = helper.createDrawableItemLike(Registration.DISENCHANTER_ITEM.get());
    }

    /** A row for every level of every enchantment in the world the client is in (none outside one). */
    public static List<Display> displays() {
        var level = Minecraft.getInstance().level;
        if (level == null) return List.of();
        List<Display> out = new ArrayList<>();
        level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(enchantment -> {
            List<Holder<net.minecraft.world.item.Item>> supported = new ArrayList<>();
            enchantment.value().getSupportedItems().forEach(supported::add);
            if (supported.isEmpty()) return;
            // The same "random" order every time, different for each enchantment.
            Collections.shuffle(supported, new Random(enchantment.key().identifier().hashCode()));
            for (int lvl = enchantment.value().getMinLevel(); lvl <= enchantment.value().getMaxLevel(); lvl++) {
                List<ItemStack> items = new ArrayList<>();
                for (Holder<net.minecraft.world.item.Item> item : supported) {
                    ItemStack stack = new ItemStack(item);
                    stack.enchant(enchantment, lvl);
                    items.add(stack);
                }
                out.add(new Display(items, EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, lvl))));
            }
        });
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
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display display, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, ITEM_X + 1, 1)
                .setStandardSlotBackground()
                .addItemStacks(display.items());
        builder.addSlot(RecipeIngredientRole.INPUT, BOOK_X + 1, 1)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(Items.BOOK));
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, 1)
                .setOutputSlotBackground()
                .addItemStack(display.book());
    }

    /** Hovering the arrow or the cost line: the experience it draws, and how long it takes. */
    @Override
    public void getTooltip(ITooltipBuilder tooltip, Display display, IRecipeSlotsView slots, double mouseX, double mouseY) {
        boolean onArrow = mouseX >= ARROW_X && mouseX < ARROW_X + ARROW_W && mouseY >= 0 && mouseY < SLOT;
        boolean onText = mouseY >= TEXT_Y && mouseY < HEIGHT;
        if (!onArrow && !onText) return;
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.incubating.experience",
                DisenchanterBlockEntity.EXPERIENCE));
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.incubating.time", seconds()));
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        IDrawableAnimated arrow = helper.createAnimatedRecipeArrow(DisenchanterBlockEntity.TIME);
        arrow.draw(g, ARROW_X, 1);
        Font font = Minecraft.getInstance().font;
        Component text = Component.translatable("jei.mobfarmingsupplies.incubating.cost",
                DisenchanterBlockEntity.EXPERIENCE, seconds());
        g.text(font, text, (WIDTH - font.width(text)) / 2, TEXT_Y, TEXT_COLOR, false);
    }

    private static String seconds() {
        return String.format("%.1f", DisenchanterBlockEntity.TIME / 20f);
    }
}
