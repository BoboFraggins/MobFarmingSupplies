package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * JEI plugin for MobFarmingSupplies — currently just the XP Juice guides (see
 * {@link XpJuiceGuideCategory}), clarifying how an XP Juice Bucket or a bottle o' enchanting is
 * actually obtained (Experience Syringe → Tank → bucket/bottle) rather than something
 * craftable directly.
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
        reg.addRecipeCategories(new XpJuiceGuideCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        reg.addRecipes(
                XpJuiceGuideCategory.RECIPE_TYPE,
                List.of(XpJuiceGuideRecipe.bucket(), XpJuiceGuideRecipe.bottle()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(
                Registration.EXPERIENCE_SYRINGE.get().getDefaultInstance(), XpJuiceGuideCategory.RECIPE_TYPE);
        reg.addRecipeCatalyst(Registration.TANK_ITEM.get().getDefaultInstance(), XpJuiceGuideCategory.RECIPE_TYPE);
        reg.addRecipeCatalyst(
                Registration.XP_JUICE_BUCKET.get().getDefaultInstance(), XpJuiceGuideCategory.RECIPE_TYPE);
        reg.addRecipeCatalyst(new ItemStack(Items.EXPERIENCE_BOTTLE), XpJuiceGuideCategory.RECIPE_TYPE);
    }
}
