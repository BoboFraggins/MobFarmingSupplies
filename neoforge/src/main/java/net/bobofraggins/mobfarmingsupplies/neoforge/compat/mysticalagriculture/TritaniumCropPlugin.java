package net.bobofraggins.mobfarmingsupplies.neoforge.compat.mysticalagriculture;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.minecraft.resources.Identifier;

/**
 * Adds Tritanium and Tritanium Alloy crops to Mystical Agriculture. Mystical Agriculture finds this class by its
 * annotation, so it's only ever loaded with that mod installed. It makes each crop, its Essence
 * and its Seeds itself, drawn in the crop's colour, along with their seed recipes; the Essence
 * recipes are in {@code data/mobfarmingsupplies/recipe/mysticalagriculture}.
 */
@MysticalAgriculturePlugin
public class TritaniumCropPlugin implements IMysticalAgriculturePlugin {

    @Override
    public void onRegisterCrops(ICropRegistry registry) {
        register(registry, "tritanium", CropTier.THREE, 0x39F014);
        register(registry, "tritanium_alloy", CropTier.FOUR, 0xC8D2DE);
    }

    private static void register(ICropRegistry registry, String metal, CropTier tier, int colour) {
        Crop crop = new Crop(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, metal),
                tier, CropType.RESOURCE, LazyIngredient.tag("c:ingots/" + metal));
        crop.setColor(colour);
        registry.register(crop);
    }
}
