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
 * how XP Juice is obtained (Experience Syringe → Tank → bucket or bottle, {@link XpJuiceGuide})
 * and anvil crushing (Anvil → Nether Quartz → Silicon Clump, {@link AnvilCrushingGuide}).
 * Plus the Filter Scribing Terminal: a recipe view of scribing Item Filters
 * ({@link FilterScribingCategory}) and the ghost-ingredient handler for its matcher slot.
 *
 * <p>JEI is a soft dependency. On NeoForge/Forge, {@code @JeiPlugin}-annotated classes are
 * found via classpath annotation scanning — no extra wiring needed there. On Fabric, JEI does
 * no scanning at all; it requires the {@code jei_mod_plugin} entrypoint declared in
 * {@code fabric.mod.json} pointing at this class, or it's silently never loaded (present in the
 * jar, no error, just never registered — see the {@code jei_integration} project memory). That
 * entrypoint is lazy (only loaded if JEI itself queries it), so — unlike the Curios/Trinkets
 * integrations elsewhere in this codebase — no {@code isModLoaded} guard is needed here.
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
        reg.addRecipes(XpJuiceGuide.RECIPE_TYPE, List.of(XpJuiceGuide.bucket(), XpJuiceGuide.bottle()));
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
        reg.addCraftingStation(XpJuiceGuide.RECIPE_TYPE,
                Registration.EXPERIENCE_SYRINGE.get(),
                Registration.TANK_ITEM.get(),
                Registration.XP_JUICE_BUCKET.get(),
                Items.EXPERIENCE_BOTTLE);
        reg.addCraftingStation(AnvilCrushingGuide.RECIPE_TYPE, Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL);
        reg.addCraftingStation(FilterScribingCategory.RECIPE_TYPE, Registration.FILTER_SCRIBING_TERMINAL_ITEM.get());
    }
}
