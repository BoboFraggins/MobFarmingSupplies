package net.bobofraggins.mobfarmingsupplies.circuitinscriber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * {@code mobfarmingsupplies:circuit_inscribing}: what the Circuit Inscriber makes. One of the
 * {@code first} input and, if given, one of the {@code second} become the {@code result}, taking
 * {@code time} ticks (default 100) and {@code energy} in all (default 2000), spread evenly over them:
 * <pre>{"type": "mobfarmingsupplies:circuit_inscribing",
 *  "first": "mobfarmingsupplies:isolinear_optical_chip_mark_1", "second": "#c:ingots/gold",
 *  "result": {"id": "mobfarmingsupplies:isolinear_optical_chip_mark_2"}, "energy": 64000, "time": 300}</pre>
 */
public class CircuitInscribingRecipe implements Recipe<CircuitInscribingRecipe.Input> {

    public static final int DEFAULT_TIME = 100;
    public static final int DEFAULT_ENERGY = 2000;

    public static final MapCodec<CircuitInscribingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("first").forGetter(r -> r.first),
            Ingredient.CODEC.optionalFieldOf("second").forGetter(r -> r.second),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            Codec.intRange(1, 72_000).optionalFieldOf("time", DEFAULT_TIME).forGetter(r -> r.time),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("energy", DEFAULT_ENERGY).forGetter(r -> r.energy)
    ).apply(i, CircuitInscribingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CircuitInscribingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, r -> r.first,
                    Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, r -> r.second,
                    ItemStackTemplate.STREAM_CODEC, r -> r.result,
                    ByteBufCodecs.VAR_INT, r -> r.time,
                    ByteBufCodecs.VAR_INT, r -> r.energy,
                    CircuitInscribingRecipe::new);

    /** The two input slots, as a recipe sees them. */
    public record Input(ItemStack first, ItemStack second) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? first : second;
        }

        @Override
        public int size() {
            return 2;
        }
    }

    private final Ingredient first;
    private final Optional<Ingredient> second;
    private final ItemStackTemplate result;
    private final int time;
    private final int energy;

    public CircuitInscribingRecipe(Ingredient first, Optional<Ingredient> second, ItemStackTemplate result,
                                   int time, int energy) {
        this.first = first;
        this.second = second;
        this.result = result;
        this.time = time;
        this.energy = energy;
    }

    public Ingredient first() {
        return first;
    }

    public Optional<Ingredient> second() {
        return second;
    }

    public ItemStackTemplate result() {
        return result;
    }

    public int time() {
        return time;
    }

    /** Energy for the whole craft. */
    public int energy() {
        return energy;
    }

    /**
     * Energy for the tick that takes the craft from {@code progress} to {@code progress + 1}: an
     * even share, with the remainder spread so the ticks add up to exactly {@link #energy()}.
     */
    public long energyForTick(int progress) {
        return (long) energy * (progress + 1) / time - (long) energy * progress / time;
    }

    /** Whether this recipe takes an item from the second slot. */
    public boolean usesSecond() {
        return second.isPresent();
    }

    @Override
    public boolean matches(Input input, Level level) {
        if (!first.test(input.first())) return false;
        return second.map(ingredient -> ingredient.test(input.second())).orElse(input.second().isEmpty());
    }

    @Override
    public ItemStack assemble(Input input) {
        return result.create();
    }

    @Override
    public boolean isSpecial() {
        return true; // made in the Inscriber, not the recipe book
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<CircuitInscribingRecipe> getSerializer() {
        return Registration.CIRCUIT_INSCRIBING_SERIALIZER.get();
    }

    @Override
    public RecipeType<CircuitInscribingRecipe> getType() {
        return Registration.CIRCUIT_INSCRIBING_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
