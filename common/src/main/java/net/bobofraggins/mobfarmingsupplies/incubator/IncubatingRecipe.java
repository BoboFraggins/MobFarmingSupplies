package net.bobofraggins.mobfarmingsupplies.incubator;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.dna.IDnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * {@code mobfarmingsupplies:incubating}: what the XP Powered Incubator makes. Each of its rows
 * turns one {@code input} into the result over {@code time} ticks, drawing {@code experience} mB
 * of experience fluid bit by bit as it goes (pausing when there's none), with the sample in the
 * incubator's sample slot. The sample is never used up.
 * <pre>{@code
 * { "type": "mobfarmingsupplies:incubating",
 *   "sample": "minecraft:dragon_head",
 *   "input": "minecraft:egg",
 *   "experience": 5000,
 *   "time": 1200,
 *   "result": { "id": "minecraft:dragon_egg" } }
 * }</pre>
 * With {@code "result": "sampled_spawn_egg"} the result is the spawn egg of a mob the sample
 * holds: a DNA sample's mob, or for a DNA pack one of its mobs at random (the mob already in the
 * row's output, if it's one of them, so the outputs stack). The optional {@code "mobs"} (entity
 * ids or a tag) limits which sampled mobs the recipe takes.
 */
public class IncubatingRecipe implements Recipe<IncubatingInput> {

    /** The {@code "result"} that means "the sampled mob's spawn egg". */
    public static final String SAMPLED_SPAWN_EGG = "sampled_spawn_egg";

    private static final Codec<Optional<ItemStack>> RESULT_CODEC = Codec.either(
                    Codec.STRING.comapFlatMap(s -> s.equals(SAMPLED_SPAWN_EGG)
                            ? DataResult.success(s)
                            : DataResult.error(() -> "Not a result: " + s), s -> s),
                    ItemStack.STRICT_CODEC)
            .xmap(either -> either.right(), fixed -> fixed.<Either<String, ItemStack>>map(Either::right)
                    .orElse(Either.left(SAMPLED_SPAWN_EGG)));

    public static final MapCodec<IncubatingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("sample").forGetter(IncubatingRecipe::sample),
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).optionalFieldOf("mobs").forGetter(r -> r.mobs),
            Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(IncubatingRecipe::input),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("experience").forGetter(IncubatingRecipe::experience),
            ExtraCodecs.POSITIVE_INT.fieldOf("time").forGetter(IncubatingRecipe::time),
            RESULT_CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, IncubatingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IncubatingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, IncubatingRecipe::sample,
            ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.ENTITY_TYPE)), r -> r.mobs,
            Ingredient.CONTENTS_STREAM_CODEC, IncubatingRecipe::input,
            ByteBufCodecs.VAR_INT, IncubatingRecipe::experience,
            ByteBufCodecs.VAR_INT, IncubatingRecipe::time,
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC), r -> r.result,
            IncubatingRecipe::new);

    private final Ingredient sample;
    private final Optional<HolderSet<EntityType<?>>> mobs;
    private final Ingredient input;
    private final int experience;
    private final int time;
    /** The fixed result, or empty for {@link #SAMPLED_SPAWN_EGG}. */
    private final Optional<ItemStack> result;

    public IncubatingRecipe(Ingredient sample, Optional<HolderSet<EntityType<?>>> mobs, Ingredient input,
                            int experience, int time, Optional<ItemStack> result) {
        this.sample = sample;
        this.mobs = mobs;
        this.input = input;
        this.experience = experience;
        this.time = time;
        this.result = result;
    }

    public Ingredient sample() {
        return sample;
    }

    public Ingredient input() {
        return input;
    }

    /** mB of experience fluid over the whole bake. */
    public int experience() {
        return experience;
    }

    /** Ticks to bake. */
    public int time() {
        return time;
    }

    /** The fixed result, or empty when the result is the sampled mob's spawn egg. */
    public Optional<ItemStack> fixedResult() {
        return result;
    }

    /** Whether the recipe takes this mob (always, unless it lists {@code "mobs"}). */
    @SuppressWarnings("deprecation") // builtInRegistryHolder(): no direct EntityType.is(HolderSet)
    public boolean takesMob(EntityType<?> type) {
        return mobs.map(set -> set.contains(type.builtInRegistryHolder())).orElse(true);
    }

    /** The mobs the sample could hatch that this recipe takes and that have a spawn egg. */
    List<EntityType<?>> candidates(IncubatingInput in, ServerLevel level) {
        if (!(in.sample().getItem() instanceof IDnaSampleItem dna)) return List.of();
        return dna.mobs(in.sample(), level, in.pos()).stream()
                .filter(this::takesMob)
                .filter(type -> egg(type).isPresent())
                .toList();
    }

    @Override
    public boolean matches(IncubatingInput in, Level level) {
        if (!sample.test(in.sample()) || !input.test(in.input())) return false;
        if (result.isPresent()) return true;
        return level instanceof ServerLevel server && !candidates(in, server).isEmpty();
    }

    /**
     * What one bake makes: the fixed result, or the spawn egg of one of the sampled mobs, the one
     * already in {@code output} if it's a candidate, so the row's output keeps stacking.
     */
    ItemStack make(IncubatingInput in, ServerLevel level, RandomSource random, ItemStack output) {
        if (result.isPresent()) return result.get().copy();
        List<EntityType<?>> candidates = candidates(in, level);
        if (candidates.isEmpty()) return ItemStack.EMPTY;
        EntityType<?> mob = candidates.stream()
                .filter(type -> !output.isEmpty() && spawns(output, type))
                .findFirst()
                .orElseGet(() -> candidates.get(random.nextInt(candidates.size())));
        return egg(mob).map(egg -> new ItemStack(egg)).orElse(ItemStack.EMPTY);
    }

    /** Whether a bake's result could go in {@code output}: empty, or more of what one could make. */
    boolean fits(IncubatingInput in, ServerLevel level, ItemStack output) {
        if (output.isEmpty()) return true;
        if (output.getCount() >= output.getMaxStackSize()) return false;
        if (result.isPresent()) return ItemStack.isSameItemSameComponents(output, result.get());
        return candidates(in, level).stream().anyMatch(type -> spawns(output, type));
    }

    /** Only the fixed result can be known without the incubator; a sampled egg is picked as it's made. */
    @Override
    public ItemStack assemble(IncubatingInput in, HolderLookup.Provider registries) {
        return result.map(ItemStack::copy).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.map(ItemStack::copy).orElse(ItemStack.EMPTY);
    }

    /** {@code type}'s spawn egg, if it has one. */
    public static Optional<Item> egg(EntityType<?> type) {
        return Optional.ofNullable(SpawnEggItem.byId(type));
    }

    /** Whether {@code stack} is a spawn egg of {@code type}. */
    static boolean spawns(ItemStack stack, EntityType<?> type) {
        return stack.getItem() instanceof SpawnEggItem egg && egg.getType(stack) == type;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<IncubatingRecipe> getSerializer() {
        return Registration.INCUBATING_SERIALIZER.get();
    }

    @Override
    public RecipeType<IncubatingRecipe> getType() {
        return Registration.INCUBATING_TYPE.get();
    }
}
