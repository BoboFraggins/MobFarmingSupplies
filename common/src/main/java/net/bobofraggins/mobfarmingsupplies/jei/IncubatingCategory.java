package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
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
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.dna.IDnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatingRecipe;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * JEI page for the XP Powered Incubator's {@link IncubatingRecipe}s, one row each: the sample,
 * the input, an arrow, and what it makes, with the experience and time below.
 *
 * <p>A recipe whose result is the sampled mob's spawn egg is shown per sample: a DNA Sample
 * recipe as one entry per mob (that mob's sample and egg, so looking up an egg finds its own
 * entry), a pack as one entry cycling the eggs of the mobs in it.
 */
public final class IncubatingCategory implements IRecipeCategory<IncubatingCategory.Display> {

    public static final RecipeType<Display> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "incubating", Display.class);

    /** One row as shown: the samples, inputs and outputs JEI cycles through. */
    public record Display(List<ItemStack> samples, List<ItemStack> inputs, List<ItemStack> outputs,
                          int experience, int time) {}

    private static final int SLOT = 18;
    private static final int SAMPLE_X = 0, INPUT_X = 24, ARROW_X = 46, OUTPUT_X = 76;
    private static final int ARROW_W = 24;
    private static final int WIDTH = OUTPUT_X + SLOT;
    private static final int TEXT_Y = SLOT + 4;
    private static final int HEIGHT = TEXT_Y + 9;
    private static final int TEXT_COLOR = 0xFF808080;

    private final Component title = Component.translatable("jei.mobfarmingsupplies.incubating");
    private final IDrawable icon;
    private final IGuiHelper helper;

    public IncubatingCategory(IGuiHelper helper) {
        this.helper = helper;
        this.icon = helper.createDrawableItemLike(Registration.INCUBATOR_ITEM.get());
    }

    /** Every row to show for {@code recipes}. */
    public static List<Display> displays(List<IncubatingRecipe> recipes) {
        List<Display> out = new ArrayList<>();
        for (IncubatingRecipe recipe : recipes) {
            List<ItemStack> inputs = stacks(recipe.input());
            if (recipe.fixedResult().isPresent()) {
                out.add(new Display(stacks(recipe.sample()), inputs, List.of(recipe.fixedResult().get().copy()),
                        recipe.experience(), recipe.time()));
                continue;
            }
            for (ItemStack sample : stacks(recipe.sample())) {
                if (sample.getItem() instanceof DnaSampleItem) {
                    for (EntityType<?> mob : sampleableMobs()) {
                        if (!recipe.takesMob(mob)) continue;
                        IncubatingRecipe.egg(mob).ifPresent(egg -> out.add(new Display(
                                List.of(DnaSamplingGuide.sampleOf(mob)), inputs, List.of(new ItemStack(egg)),
                                recipe.experience(), recipe.time())));
                    }
                } else if (sample.getItem() instanceof IDnaSampleItem pack) {
                    List<ItemStack> eggs = pack.displayMobs(sample).stream()
                            .filter(recipe::takesMob)
                            .flatMap(mob -> IncubatingRecipe.egg(mob).stream())
                            .map(egg -> new ItemStack(egg))
                            .toList();
                    if (!eggs.isEmpty()) {
                        out.add(new Display(List.of(sample), inputs, eggs, recipe.experience(), recipe.time()));
                    }
                }
            }
        }
        return out;
    }

    /** The mobs a DNA Sample can be taken from, as the DNA Sampling page lists them. */
    private static List<EntityType<?>> sampleableMobs() {
        return DnaSamplingGuide.recipe().samples().stream()
                .map(sample -> ((DnaSampleItem) sample.getItem()).displayMobs(sample))
                .flatMap(List::stream)
                .sorted(Comparator.comparing(type -> type.getDescription().getString()))
                .toList();
    }

    private static List<ItemStack> stacks(Ingredient ingredient) {
        return java.util.Arrays.stream(ingredient.getItems()).map(ItemStack::copy).toList();
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
        IRecipeSlotBuilder sample = builder.addSlot(RecipeIngredientRole.CATALYST, SAMPLE_X + 1, 1)
                .setStandardSlotBackground()
                .addItemStacks(display.samples())
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable("jei.mobfarmingsupplies.incubating.sample")));
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, 1)
                .setStandardSlotBackground()
                .addItemStacks(display.inputs());
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, 1)
                .setOutputSlotBackground()
                .addItemStacks(display.outputs());
        if (display.samples().size() == display.outputs().size() && display.samples().size() > 1) {
            builder.createFocusLink(sample, output); // each sample with its own result
        }
    }

    /** Hovering the arrow or the cost line: the experience the recipe draws, and how long it takes. */
    @Override
    public void getTooltip(ITooltipBuilder tooltip, Display display, IRecipeSlotsView slots, double mouseX, double mouseY) {
        boolean onArrow = mouseX >= ARROW_X && mouseX < ARROW_X + ARROW_W && mouseY >= 0 && mouseY < SLOT;
        boolean onText = mouseY >= TEXT_Y && mouseY < HEIGHT;
        if (!onArrow && !onText) return;
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.incubating.experience", display.experience()));
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.incubating.time",
                String.format("%.1f", display.time() / 20f)));
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        IDrawableAnimated arrow = helper.createAnimatedRecipeArrow(Math.max(1, display.time()));
        arrow.draw(g, ARROW_X, 1);
        Font font = Minecraft.getInstance().font;
        Component text = Component.translatable("jei.mobfarmingsupplies.incubating.cost",
                display.experience(), String.format("%.1f", display.time() / 20f));
        g.drawString(font, text, (WIDTH - font.width(text)) / 2, TEXT_Y, TEXT_COLOR, false);
    }
}
