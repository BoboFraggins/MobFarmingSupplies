package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalScreen;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import net.minecraft.world.item.ItemStack;

/**
 * JEI plugin for MobFarmingSupplies — vertical guides for things that aren't crafted directly:
 * how an XP Juice Bucket is obtained (Experience Syringe → Tank → bucket, {@link XpJuiceGuide})
 * and anvil crushing (Anvil → Nether Quartz → Silicon Clump, {@link AnvilCrushingGuide}).
 * Plus the Filter Scribing Terminal: a recipe view of scribing Item Filters
 * ({@link FilterScribingCategory}) and the ghost-ingredient handler for its matcher slot.
 *
 * <p>JEI is a soft dependency; this class is only ever loaded by JEI itself scanning for
 * {@link JeiPlugin}-annotated classes, which only happens when JEI is actually installed — so
 * no {@code isModLoaded} guard is needed here the way Curios/Trinkets integrations need one
 * elsewhere in this codebase (see {@code feedback_soft_dependency_classloading}). This mirrors
 * {@code TankJadePlugin}'s equivalent situation with Jade.
 */
@JeiPlugin
public class MobFarmingSuppliesJeiPlugin implements IModPlugin {

    private static final Identifier PLUGIN_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        IGuiHelper guiHelper = reg.getJeiHelpers().getGuiHelper();
        reg.addRecipeCategories(XpJuiceGuide.category(guiHelper), AnvilCrushingGuide.category(guiHelper),
                new FilterScribingCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        reg.addRecipes(XpJuiceGuide.RECIPE_TYPE, List.of(XpJuiceGuide.recipe()));
        reg.addRecipes(AnvilCrushingGuide.RECIPE_TYPE, List.of(AnvilCrushingGuide.recipe()));
        reg.addRecipes(FilterScribingCategory.RECIPE_TYPE, List.of(FilterScribingExamples.create()));
    }

    /**
     * The Item Filter isn't in the creative tab (it only comes from the Filter Scribing
     * Terminal), and JEI only lists items found in creative tabs — add it so players can look
     * up its recipe. Filters with different settings share one JEI entry (no subtype
     * interpreter), so this finds the scribing recipe's outputs.
     */
    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration reg) {
        reg.addExtraItemStacks(List.of(new ItemStack(Registration.ITEM_FILTER.get())));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration reg) {
        reg.addGhostIngredientHandler(FilterScribingTerminalScreen.class, new FilterScribingTerminalGhostHandler());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(
                Registration.EXPERIENCE_SYRINGE.get().getDefaultInstance(), XpJuiceGuide.RECIPE_TYPE);
        reg.addRecipeCatalyst(Registration.TANK_ITEM.get().getDefaultInstance(), XpJuiceGuide.RECIPE_TYPE);
        reg.addRecipeCatalyst(
                Registration.XP_JUICE_BUCKET.get().getDefaultInstance(), XpJuiceGuide.RECIPE_TYPE);
        for (var anvil : List.of(Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL)) {
            reg.addRecipeCatalyst(anvil.getDefaultInstance(), AnvilCrushingGuide.RECIPE_TYPE);
        }
        reg.addRecipeCatalyst(Registration.FILTER_SCRIBING_TERMINAL_ITEM.get().getDefaultInstance(),
                FilterScribingCategory.RECIPE_TYPE);
    }
}
