package net.bobofraggins.mobfarmingsupplies.tank;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.recipe.ShapedRecipeCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * A shaped crafting recipe that upgrades a tank to the next tier and keeps what's in it: the
 * result gets the {@code tank_contents} of the tank in the grid. The fluid and amount are
 * unchanged, so the tank ends up a quarter as full (a quarter-full basic tank becomes a
 * 1/16-full gold tank).
 *
 * <p>Written like a shaped recipe in JSON, with type {@code mobfarmingsupplies:tank_upgrade}.
 */
public class TankUpgradeRecipe extends ShapedRecipe implements ShapedRecipeCodecs.Shaped {

    public static final MapCodec<TankUpgradeRecipe> MAP_CODEC = ShapedRecipeCodecs.mapCodec(TankUpgradeRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, TankUpgradeRecipe> STREAM_CODEC =
            ShapedRecipeCodecs.streamCodec(TankUpgradeRecipe::new);

    protected final ShapedRecipePattern pattern;
    protected final ItemStack result;

    public TankUpgradeRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                             ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
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
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack upgraded = result.copy();
        for (ItemStack ingredient : input.items()) {
            TankContents contents = ingredient.get(Registration.TANK_CONTENTS.get());
            if (contents != null) {
                upgraded.set(Registration.TANK_CONTENTS.get(), contents);
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
