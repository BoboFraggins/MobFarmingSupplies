package net.bobofraggins.mobfarmingsupplies.potion;

import com.mojang.serialization.MapCodec;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Our brewing-stand recipes on 26.3, where brewing is data-driven: written like vanilla's
 * {@code minecraft:brewing} recipes (input, reagent, output; see {@code recipe/brewing_*.json}),
 * but what comes out is {@link MFSBrewing#mix}'s. That's how Oresight learns its ore from the
 * reagent and an upgrade keeps it; the JSON output is only what recipe viewers show.
 */
public class MFSBrewingRecipe extends BrewingRecipe {

    public static final MapCodec<MFSBrewingRecipe> MAP_CODEC =
            BrewingRecipe.MAP_CODEC.xmap(MFSBrewingRecipe::from, r -> r);

    public static final StreamCodec<RegistryFriendlyByteBuf, MFSBrewingRecipe> STREAM_CODEC =
            BrewingRecipe.STREAM_CODEC.map(MFSBrewingRecipe::from, r -> r);

    public MFSBrewingRecipe(PotionIngredient input, PotionIngredient reagent, ItemStackTemplate output) {
        super(input, reagent, output);
    }

    private static MFSBrewingRecipe from(BrewingRecipe recipe) {
        return new MFSBrewingRecipe(recipe.getInput(), recipe.getReagent(), recipe.getOutput());
    }

    @Override
    public boolean matches(BrewingInput input, Level level) {
        return super.matches(input, level) && !MFSBrewing.mix(input.input(), input.reagent()).isEmpty();
    }

    @Override
    public boolean matches(BrewingInput input) {
        return super.matches(input) && !MFSBrewing.mix(input.input(), input.reagent()).isEmpty();
    }

    @Override
    public ItemStack assemble(BrewingInput input) {
        return MFSBrewing.mix(input.input(), input.reagent());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public RecipeSerializer<BrewingRecipe> getSerializer() {
        return (RecipeSerializer) Registration.MFS_BREWING_SERIALIZER.get();
    }
}
