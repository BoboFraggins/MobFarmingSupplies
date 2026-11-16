package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.oresight.OresightTargets;
import net.bobofraggins.mobfarmingsupplies.potion.MFSBrewing;
import net.bobofraggins.mobfarmingsupplies.potion.PotionTier;
import net.bobofraggins.mobfarmingsupplies.potion.TieredPotionItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Our brewing stand recipes ({@link MFSBrewing}) on JEI's vanilla Brewing page, which only finds
 * vanilla's own mixes by itself: the Flight potions, and the Oresight potions for every ore, each
 * ore's page listing everything that finds it (Gold Ore, Raw Gold, ...).
 */
final class BrewingGuide {

    private BrewingGuide() {}

    static List<IJeiBrewingRecipe> recipes(IVanillaRecipeFactory factory) {
        List<IJeiBrewingRecipe> recipes = new ArrayList<>();
        ItemStack air = new ItemStack(Registration.ESSENCE_BOTTLES.get(Essence.AIR).get());
        tiers(factory, recipes, "flight", List.of(new ItemStack(Items.ELYTRA)), air, TieredPotionItem::flight);

        // What finds each ore, in the tag's order.
        Map<Block, List<ItemStack>> inputs = new LinkedHashMap<>();
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(MFSBrewing.ORESIGHT_INPUTS)) {
            ItemStack stack = new ItemStack(item);
            Block ore = OresightTargets.oreFor(stack);
            if (ore != null) inputs.computeIfAbsent(ore, o -> new ArrayList<>()).add(stack);
        }
        ItemStack sight = new ItemStack(Registration.ESSENCE_BOTTLES.get(Essence.SIGHT).get());
        inputs.forEach((ore, stacks) -> tiers(factory, recipes,
                "oresight/" + BuiltInRegistries.BLOCK.getKey(ore).toString().replace(':', '/'),
                stacks, sight, tier -> TieredPotionItem.oresight(tier, ore)));
        return recipes;
    }

    /** Essence bottle + ingredients → Lesser, then Redstone → Greater, then a Block of Redstone → Superior. */
    private static void tiers(IVanillaRecipeFactory factory, List<IJeiBrewingRecipe> recipes, String path,
                              List<ItemStack> ingredients, ItemStack essence, Function<PotionTier, ItemStack> potion) {
        recipes.add(factory.createBrewingRecipe(ingredients, essence, potion.apply(PotionTier.LESSER), id(path + "/lesser")));
        recipes.add(factory.createBrewingRecipe(List.of(new ItemStack(Items.REDSTONE)),
                potion.apply(PotionTier.LESSER), potion.apply(PotionTier.GREATER), id(path + "/greater")));
        recipes.add(factory.createBrewingRecipe(List.of(new ItemStack(Items.REDSTONE_BLOCK)),
                potion.apply(PotionTier.GREATER), potion.apply(PotionTier.SUPERIOR), id(path + "/superior")));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("mobfarmingsupplies", "brewing/" + path);
    }
}
