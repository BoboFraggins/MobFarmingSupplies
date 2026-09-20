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

/**
 * JEI plugin for MobFarmingSupplies — currently just the XP Juice guide (see
 * {@link XpJuiceGuideCategory}), clarifying how an XP Juice Bucket is actually obtained
 * (Experience Syringe → Tank → bucket) rather than something craftable directly.
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
        reg.addRecipeCategories(new XpJuiceGuideCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        reg.addRecipes(XpJuiceGuideCategory.RECIPE_TYPE, List.of(XpJuiceGuideRecipe.create()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(
                Registration.EXPERIENCE_SYRINGE.get().getDefaultInstance(), XpJuiceGuideCategory.RECIPE_TYPE);
        reg.addRecipeCatalyst(Registration.TANK_ITEM.get().getDefaultInstance(), XpJuiceGuideCategory.RECIPE_TYPE);
        reg.addRecipeCatalyst(
                Registration.XP_JUICE_BUCKET.get().getDefaultInstance(), XpJuiceGuideCategory.RECIPE_TYPE);
    }
}
