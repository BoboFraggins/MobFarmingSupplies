package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.xpanvil.XpAnvilBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * JEI page for the XP Powered Anvil: a damaged item, an arrow, and the same item mended, cycling
 * through a few tools and armour pieces together. The experience and time below follow the item
 * showing: a point of experience fluid per point of durability, mended {@value
 * XpAnvilBlockEntity#REPAIR_PER_TICK} points a tick.
 */
public final class XpRepairingCategory implements IRecipeCategory<XpRepairingCategory.Display> {

    public static final RecipeType<Display> RECIPE_TYPE =
            RecipeType.create(MobFarmingSuppliesCommon.MODID, "xp_repairing", Display.class);

    /** The damaged items and, in the same order, their mended selves. */
    public record Display(List<ItemStack> damaged, List<ItemStack> repaired) {}

    private static final int SLOT = 18;
    private static final int INPUT_X = 12, ARROW_X = 40, OUTPUT_X = 76;
    private static final int ARROW_W = 24;
    private static final int WIDTH = OUTPUT_X + SLOT + 12;
    private static final int TEXT_Y = SLOT + 4;
    private static final int HEIGHT = TEXT_Y + 9;
    private static final int TEXT_COLOR = 0xFF808080;

    /** What's shown, and how worn: a fraction of each item's durability that's gone. */
    private static final List<Item> ITEMS = List.of(Items.DIAMOND_PICKAXE, Items.IRON_SWORD, Items.NETHERITE_AXE,
            Items.DIAMOND_CHESTPLATE, Items.BOW, Items.IRON_SHOVEL, Items.ELYTRA, Items.TRIDENT, Items.SHIELD,
            Items.GOLDEN_HELMET, Items.FISHING_ROD, Items.SHEARS);
    private static final float[] WEAR = {0.35f, 0.6f, 0.8f, 0.5f};

    private final Component title = Component.translatable("jei.mobfarmingsupplies.xp_repairing");
    private final IDrawable icon;
    private final IGuiHelper helper;

    public XpRepairingCategory(IGuiHelper helper) {
        this.helper = helper;
        this.icon = helper.createDrawableItemLike(Registration.XP_ANVIL_ITEM.get());
    }

    /** The page's one recipe. */
    public static Display display() {
        List<ItemStack> damaged = new ArrayList<>();
        List<ItemStack> repaired = new ArrayList<>();
        for (int i = 0; i < ITEMS.size(); i++) {
            ItemStack whole = new ItemStack(ITEMS.get(i));
            if (!whole.isDamageableItem()) continue;
            ItemStack worn = whole.copy();
            worn.setDamageValue(Math.max(1, Math.round(whole.getMaxDamage() * WEAR[i % WEAR.length])));
            damaged.add(worn);
            repaired.add(whole);
        }
        return new Display(damaged, repaired);
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
        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, 1)
                .setStandardSlotBackground()
                .addItemStacks(display.damaged());
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, 1)
                .setOutputSlotBackground()
                .addItemStacks(display.repaired());
        builder.createFocusLink(input, output); // each item beside its mended self
    }

    /** The durability the damaged item showing is missing (0 if none is showing). */
    private static int missing(IRecipeSlotsView slots) {
        return slots.getSlotViews(RecipeIngredientRole.INPUT).stream().findFirst()
                .flatMap(view -> view.getDisplayedItemStack())
                .map(ItemStack::getDamageValue).orElse(0);
    }

    /** Hovering the arrow or the cost line: the experience it draws, and how long it takes. */
    @Override
    public void getTooltip(ITooltipBuilder tooltip, Display display, IRecipeSlotsView slots, double mouseX, double mouseY) {
        boolean onArrow = mouseX >= ARROW_X && mouseX < ARROW_X + ARROW_W && mouseY >= 0 && mouseY < SLOT;
        boolean onText = mouseY >= TEXT_Y && mouseY < HEIGHT;
        if (!onArrow && !onText) return;
        int missing = missing(slots);
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.incubating.experience",
                missing * XpAnvilBlockEntity.MB_PER_POINT));
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.incubating.time", seconds(missing)));
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        int missing = missing(slots);
        int ticks = Math.max(1, (missing + XpAnvilBlockEntity.REPAIR_PER_TICK - 1) / XpAnvilBlockEntity.REPAIR_PER_TICK);
        helper.createAnimatedRecipeArrow(ticks).draw(g, ARROW_X, 1);
        Font font = Minecraft.getInstance().font;
        Component text = Component.translatable("jei.mobfarmingsupplies.incubating.cost",
                missing * XpAnvilBlockEntity.MB_PER_POINT, seconds(missing));
        g.drawString(font, text, (WIDTH - font.width(text)) / 2, TEXT_Y, TEXT_COLOR, false);
    }

    /** Seconds to mend {@code points} of durability, to a tenth. */
    private static String seconds(int points) {
        int ticks = (points + XpAnvilBlockEntity.REPAIR_PER_TICK - 1) / XpAnvilBlockEntity.REPAIR_PER_TICK;
        return String.format("%.1f", ticks / 20f);
    }
}
