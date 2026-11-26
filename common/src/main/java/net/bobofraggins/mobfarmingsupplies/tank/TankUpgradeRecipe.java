package net.bobofraggins.mobfarmingsupplies.tank;

import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs.ShapedRecipePattern;

/**
 * A shaped crafting recipe that upgrades a tank to the next tier and keeps what's in it: the
 * result gets the {@code tank_contents} of the tank in the grid. The fluid and amount are
 * unchanged, so the tank ends up a quarter as full (a quarter-full basic tank becomes a
 * 1/16-full gold tank).
 *
 * <p>Written like a shaped recipe in JSON, with type {@code mobfarmingsupplies:tank_upgrade}.
 */
public class TankUpgradeRecipe extends ShapedRecipe implements ShapedRecipeCodecs.Shaped {

    public static final RecipeSerializer<TankUpgradeRecipe> SERIALIZER = ShapedRecipeCodecs.serializer(TankUpgradeRecipe::new);

    protected final ShapedRecipePattern pattern;
    protected final ItemStack result;

    public TankUpgradeRecipe(ResourceLocation id, String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                             ItemStack result, boolean showNotification) {
        super(id, group, category, pattern.width(), pattern.height(), pattern.ingredients(), result, showNotification);
        this.pattern = pattern;
        this.result = result;
    }

    @Override
    public ShapedRecipePattern pattern() {
        return pattern;
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registries) {
        ItemStack upgraded = result.copy();
        for (ItemStack ingredient : input.getItems()) {
            TankContents contents = ItemComponents.get(ingredient, Registration.TANK_CONTENTS.get());
            if (contents != null) {
                ItemComponents.set(upgraded, Registration.TANK_CONTENTS.get(), contents);
                break;
            }
        }
        return upgraded;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.TANK_UPGRADE_SERIALIZER.get();
    }
}
