package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikes;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeads;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * JEI guide for the Head Pike: a head on a pike keeps its mob from spawning naturally nearby. Shown
 * as a use of every head that wards a mob (vanilla's skulls and this mod's Mob Heads, cycling) and
 * of the Head Pike, in a dialog titled with the pike: the head + the pike, an arrow, then the mob
 * it keeps away with a barrier sign over it. Player and dragon heads only decorate a pike, so they
 * aren't shown.
 */
public final class HeadPikeGuide extends DialogGuideCategory {

    public static final IRecipeType<GuideRecipe> RECIPE_TYPE =
            IRecipeType.create(MobFarmingSuppliesCommon.MODID, "head_pike", GuideRecipe.class);

    private static final int MOB_WIDTH = 40;
    private static final int MOB_HEIGHT = 40;
    /** The barrier sign is drawn at twice an item's size over the mob. */
    private static final int SIGN_SCALE = 2;

    private final MobDisplay mobs = new MobDisplay();
    private ItemStack barrier;

    public HeadPikeGuide(IGuiHelper helper) {
        super(RECIPE_TYPE, Component.translatable("jei.mobfarmingsupplies.head_pike"),
                helper.createDrawableItemLike(Registration.HEAD_PIKE_ITEM.get()), helper, MOB_WIDTH, MOB_HEIGHT);
    }

    /** The guide's one recipe: every head that keeps a mob away, then the pike. */
    public static GuideRecipe recipe() {
        List<ItemStack> heads = MobHeads.allHeads().stream().filter(head -> HeadPikes.mobOf(head) != null).toList();
        return new GuideRecipe(List.of(new GuideRecipe.Row(List.of(
                new GuideRecipe.Step(heads, RecipeIngredientRole.INPUT,
                        Component.translatable("jei.mobfarmingsupplies.head_pike.step.head")),
                new GuideRecipe.Step(new ItemStack(Registration.HEAD_PIKE_ITEM.get()),
                        RecipeIngredientRole.CRAFTING_STATION,
                        Component.translatable("jei.mobfarmingsupplies.head_pike.step.pike"))))))
                .titled(new ItemStack(Registration.HEAD_PIKE_ITEM.get()).getHoverName());
    }

    @Override
    protected void drawTrailing(GuideRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor g, int x, int y,
                                double mouseX, double mouseY) {
        LivingEntity mob = shownMob(slots);
        int top = trailingY(MOB_HEIGHT);
        if (mob != null) MobDisplay.draw(g, mob, x, top, x + MOB_WIDTH, top + MOB_HEIGHT, mouseX, mouseY);
        // The "no" sign, centred over the mob.
        if (barrier == null) barrier = new ItemStack(Items.BARRIER);
        int size = 16 * SIGN_SCALE;
        g.pose().pushMatrix();
        g.pose().translate(x + (MOB_WIDTH - size) / 2f, top + (MOB_HEIGHT - size) / 2f);
        g.pose().scale(SIGN_SCALE, SIGN_SCALE);
        g.item(barrier, 0, 0);
        g.pose().popMatrix();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, GuideRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        int x = trailingX(recipe);
        int top = trailingY(MOB_HEIGHT);
        if (mouseX < x || mouseX >= x + MOB_WIDTH || mouseY < top || mouseY >= top + MOB_HEIGHT) return;
        EntityType<?> type = shownType(slots);
        if (type != null) tooltip.add(type.getDescription());
        tooltip.add(Component.translatable("jei.mobfarmingsupplies.head_pike.step.mob", HeadPikes.RADIUS));
    }

    /** The mob the head showing right now keeps away. */
    @Nullable
    private static EntityType<?> shownType(IRecipeSlotsView slots) {
        ItemStack shown = slots.getSlotViews(RecipeIngredientRole.INPUT).stream().findFirst()
                .flatMap(view -> view.getDisplayedItemStack()).orElse(ItemStack.EMPTY);
        return HeadPikes.mobOf(shown);
    }

    @Nullable
    private LivingEntity shownMob(IRecipeSlotsView slots) {
        EntityType<?> type = shownType(slots);
        return type == null ? null : mobs.get(type, false);
    }
}
