package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.bobofraggins.mobfarmingsupplies.client.devtools.DevCommands;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalScreen;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatingRecipe;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import net.minecraft.world.item.ItemStack;

/**
 * JEI plugin for MobFarmingSupplies — vertical guides for things that aren't crafted directly:
 * how an XP Juice Bucket is obtained (Experience Syringe → Tank or Experience Pylon → bucket,
 * {@link XpJuiceGuide}), how a Bottle o' Enchanting is filled ({@link BottlingGuide})
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
        reg.addRecipeCategories(XpJuiceGuide.category(guiHelper), BottlingGuide.category(guiHelper),
                AnvilCrushingGuide.category(guiHelper),
                new FilterScribingCategory(guiHelper), new FilteringCategory(guiHelper), new XpRepairingCategory(guiHelper), new HeadPikeGuide(guiHelper), BeheadingGuide.category(guiHelper),
                new DnaSamplingGuide(guiHelper), new CloningGuide(guiHelper), new IncubatingCategory(guiHelper),
                new DisenchantingCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        reg.addRecipes(XpJuiceGuide.RECIPE_TYPE, XpJuiceGuide.recipes());
        reg.addRecipes(BottlingGuide.RECIPE_TYPE, BottlingGuide.recipes());
        reg.addRecipes(AnvilCrushingGuide.RECIPE_TYPE, List.of(AnvilCrushingGuide.recipe()));
        reg.addRecipes(FilterScribingCategory.RECIPE_TYPE, List.of(FilterScribingExamples.create()));
        reg.addRecipes(FilteringCategory.RECIPE_TYPE, FilteringCategory.displays());
        reg.addRecipes(XpRepairingCategory.RECIPE_TYPE, List.of(XpRepairingCategory.display()));
        reg.addRecipes(HeadPikeGuide.RECIPE_TYPE, List.of(HeadPikeGuide.recipe()));
        reg.addRecipes(BeheadingGuide.RECIPE_TYPE, List.of(BeheadingGuide.recipe()));
        reg.addRecipes(DnaSamplingGuide.RECIPE_TYPE, List.of(DnaSamplingGuide.recipe()));
        reg.addRecipes(CloningGuide.RECIPE_TYPE, List.of(CloningGuide.recipe()));
        reg.addRecipes(IncubatingCategory.RECIPE_TYPE, IncubatingCategory.displays(incubatingRecipes()));
        reg.addRecipes(DisenchantingCategory.RECIPE_TYPE, DisenchantingCategory.displays());
    }

    /** Each mob's Mob Head is its own JEI entry (one item, told apart by its mob type). */
    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        reg.registerFromDataComponentTypes(Registration.MOB_HEAD_ITEM.get(), Registration.MOB_HEAD_TYPE.get());
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
                Registration.EXPERIENCE_PYLON_ITEM.get());
        reg.addCraftingStation(BottlingGuide.RECIPE_TYPE,
                Registration.EXPERIENCE_SYRINGE.get(),
                Registration.TANK_ITEM.get(),
                Registration.EXPERIENCE_PYLON_ITEM.get());
        reg.addCraftingStation(AnvilCrushingGuide.RECIPE_TYPE, Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL);
        reg.addCraftingStation(FilterScribingCategory.RECIPE_TYPE, Registration.FILTER_SCRIBING_TERMINAL_ITEM.get());
        reg.addCraftingStation(BeheadingGuide.RECIPE_TYPE,
                Registration.MOB_HARVESTER_ITEM.get(), Registration.HARVESTER_UPGRADE_BEHEADING.get());
        reg.addCraftingStation(DnaSamplingGuide.RECIPE_TYPE, Registration.DNA_COLLECTOR.get());
        reg.addCraftingStation(CloningGuide.RECIPE_TYPE, Registration.CLONE_O_MATIC_ITEM.get());
        reg.addCraftingStation(IncubatingCategory.RECIPE_TYPE, Registration.INCUBATOR_ITEM.get());
        reg.addCraftingStation(DisenchantingCategory.RECIPE_TYPE, Registration.DISENCHANTER_ITEM.get());
        reg.addCraftingStation(FilteringCategory.RECIPE_TYPE, Registration.OMNI_HOPPER_ITEM.get(),
                Registration.EINSTEIN_ROSEN_BRIDGE_ITEM.get(), Registration.LOGISTIC_SORTER_ITEM.get(),
                Registration.BLOCK_BREAKER_ITEM.get());
        reg.addCraftingStation(XpRepairingCategory.RECIPE_TYPE, Registration.XP_ANVIL_ITEM.get());
        reg.addCraftingStation(HeadPikeGuide.RECIPE_TYPE, Registration.HEAD_PIKE_ITEM.get());
    }

    /**
     * The incubating recipes the server sent this client. JEI keeps the synced recipes (on Fabric,
     * those whose serializers opt in to syncing, as ours does) in its common module, the same place
     * its own vanilla plugin reads them; it's not public API, so a change there costs only this page.
     */
    private static List<IncubatingRecipe> incubatingRecipes() {
        try {
            return mezz.jei.common.Internal.getClientSyncedRecipes()
                    .byType(Registration.INCUBATING_TYPE.get()).stream()
                    .map(holder -> holder.value())
                    .toList();
        } catch (RuntimeException | LinkageError e) {
            return List.of();
        }
    }

    /** Lets the dev command runner open a recipe page ({@code RECIPES <item>}). */
    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        DevCommands.showRecipes = stack -> runtime.getRecipesGui().show(
                runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT,
                        VanillaTypes.ITEM_STACK, stack));
        DevCommands.showCategory = id -> runtime.getJeiHelpers().getRecipeType(id)
                .ifPresent(type -> runtime.getRecipesGui().showTypes(List.of(type)));
        // As JEI's own "uses" key: the item as an ingredient, and as a crafting station.
        DevCommands.showUses = stack -> runtime.getRecipesGui().show(List.of(
                runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.INPUT,
                        VanillaTypes.ITEM_STACK, stack),
                runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.CRAFTING_STATION,
                        VanillaTypes.ITEM_STACK, stack)));
    }
}
