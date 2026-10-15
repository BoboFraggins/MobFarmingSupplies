package net.bobofraggins.mobfarmingsupplies.crushing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * {@code mobfarmingsupplies:anvil_crushing} — item entities lying where a falling anvil lands
 * are turned into the result, per item (a stack of 64 inputs gives 64 × the result count).
 * Same JSON shape as stonecutting:
 * <pre>{@code
 * { "type": "mobfarmingsupplies:anvil_crushing",
 *   "ingredient": "minecraft:quartz",
 *   "result": { "id": "mobfarmingsupplies:silicon_clump" } }
 * }</pre>
 * Performed by {@link AnvilCrushing}.
 */
public class AnvilCrushingRecipe extends SingleItemRecipe {

    public static final MapCodec<AnvilCrushingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(SingleItemRecipe::getGroup),
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.ingredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, AnvilCrushingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnvilCrushingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SingleItemRecipe::getGroup,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
            ItemStack.STREAM_CODEC, r -> r.result,
            AnvilCrushingRecipe::new);

    public AnvilCrushingRecipe(String group, Ingredient input, ItemStack result) {
        super(Registration.ANVIL_CRUSHING_TYPE.get(), Registration.ANVIL_CRUSHING_SERIALIZER.get(), group, input, result);
    }

    /** What the recipe crushes. */
    public Ingredient input() {
        return ingredient;
    }

    /** Number of result items produced per input item. */
    public int resultCountPerItem() {
        return result.getCount();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    /** Not a crafting-table recipe — keep it out of the recipe book. */
    @Override
    public boolean isSpecial() {
        return true;
    }
}
