package net.bobofraggins.mobfarmingsupplies.shared.compat;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.fluid.FluidStack;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

/**
 * Codecs that 1.20.5+ has on the classes themselves ({@code MFSCodecs.INGREDIENT},
 * {@code MFSCodecs.RESULT_STACK}, {@code MFSCodecs.FLUID_STACK}, ...) and 1.20.1 doesn't.
 */
public final class MFSCodecs {

    private MFSCodecs() {}

    /** An ingredient in its usual JSON form ({@code {"item": ...}}, {@code {"tag": ...}}, a list, or a loader type). */
    public static final Codec<Ingredient> INGREDIENT = Codec.PASSTHROUGH.comapFlatMap(
            dynamic -> {
                try {
                    JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
                    return DataResult.success(Ingredient.fromJson(json));
                } catch (RuntimeException e) {
                    return DataResult.error(e::getMessage);
                }
            },
            ingredient -> new Dynamic<>(JsonOps.INSTANCE, ingredient.toJson()));

    /** {@link #INGREDIENT}, refusing an empty one (1.20.5+'s {@code CODEC_NONEMPTY}). */
    public static final Codec<Ingredient> INGREDIENT_NONEMPTY = INGREDIENT.flatXmap(
            i -> i.isEmpty() ? DataResult.error(() -> "Item array cannot be empty") : DataResult.success(i),
            DataResult::success);

    private static final Codec<Item> ITEM = BuiltInRegistries.ITEM.byNameCodec();

    /**
     * A recipe result: {@code {"item": id, "count": n, "nbt": {...}}} (1.20.1's own form); {@code "id"} is
     * read too, for JSON written the newer way.
     */
    public static final Codec<ItemStack> RESULT_STACK = RecordCodecBuilder.<ResultParts>create(i -> i.group(
            ITEM.optionalFieldOf("item").forGetter(p -> Optional.of(p.item())),
            ITEM.optionalFieldOf("id").forGetter(p -> Optional.empty()),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ResultParts::count),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(ResultParts::nbt)
    ).apply(i, (item, id, count, nbt) -> new ResultParts(item.or(() -> id).orElse(Items.AIR), count, nbt)))
            .flatXmap(parts -> {
                if (parts.item() == Items.AIR) return DataResult.error(() -> "Result needs an \"item\"");
                ItemStack stack = new ItemStack(parts.item(), parts.count());
                parts.nbt().ifPresent(tag -> stack.setTag(tag.copy()));
                return DataResult.success(stack);
            }, stack -> DataResult.success(new ResultParts(stack.getItem(), stack.getCount(),
                    Optional.ofNullable(stack.getTag()).map(CompoundTag::copy))));

    private record ResultParts(Item item, int count, Optional<CompoundTag> nbt) {}

    /** Any item stack, empty included, in the 1.20.1 save form ({@code id}, {@code Count}, {@code tag}). */
    public static final Codec<ItemStack> OPTIONAL_ITEM_STACK = ItemStack.CODEC;

    /** A player profile in 1.20.1's NBT form (as a head's {@code SkullOwner}). */
    public static final Codec<com.mojang.authlib.GameProfile> GAME_PROFILE = CompoundTag.CODEC.xmap(
            net.minecraft.nbt.NbtUtils::readGameProfile,
            profile -> net.minecraft.nbt.NbtUtils.writeGameProfile(new CompoundTag(), profile));

    /** A fluid stack as {@code {"id": fluid, "amount": n, "tag": {...}}}. Amounts are in the loader's units. */
    public static final Codec<FluidStack> FLUID_STACK = RecordCodecBuilder.create(i -> i.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidStack::getFluid),
            Codec.LONG.fieldOf("amount").forGetter(FluidStack::getAmount),
            CompoundTag.CODEC.optionalFieldOf("tag").forGetter(fs -> Optional.ofNullable(fs.getTag()))
    ).apply(i, (Fluid fluid, Long amount, Optional<CompoundTag> tag) ->
            FluidStack.create(fluid, amount, tag.map(CompoundTag::copy).orElse(null))));
}
