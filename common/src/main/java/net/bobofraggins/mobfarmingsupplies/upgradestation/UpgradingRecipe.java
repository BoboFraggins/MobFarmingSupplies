package net.bobofraggins.mobfarmingsupplies.upgradestation;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * {@code mobfarmingsupplies:upgrading}: what the Upgrade Station makes, instantly, like a crafting
 * table. Taking the result uses up one {@code item} (the top slot) and one {@code input}.
 * <pre>{@code
 * { "type": "mobfarmingsupplies:upgrading",
 *   "item": { "item": "minecraft:iron_sword" },
 *   "input": { "item": "mobfarmingsupplies:blank_upgrade" },
 *   "result": { "id": "mobfarmingsupplies:harvester_upgrade_sharpness" } }
 * }</pre>
 * A recipe whose {@code item} is a Mob Head can name the mob with {@code "mob_head": "minecraft:spider"}.
 */
public class UpgradingRecipe implements Recipe<UpgradingInput> {

    public static final MapCodec<UpgradingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("item").forGetter(UpgradingRecipe::item),
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("mob_head").forGetter(UpgradingRecipe::mobHead),
            Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(UpgradingRecipe::input),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(UpgradingRecipe::result)
    ).apply(i, UpgradingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, UpgradingRecipe::item,
            ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ENTITY_TYPE)), UpgradingRecipe::mobHead,
            Ingredient.CONTENTS_STREAM_CODEC, UpgradingRecipe::input,
            ItemStack.STREAM_CODEC, UpgradingRecipe::result,
            UpgradingRecipe::new);

    private final Ingredient item;
    private final Optional<EntityType<?>> mobHead;
    private final Ingredient input;
    private final ItemStack result;

    public UpgradingRecipe(Ingredient item, Optional<EntityType<?>> mobHead, Ingredient input, ItemStack result) {
        this.item = item;
        this.mobHead = mobHead;
        this.input = input;
        this.result = result;
    }

    public Ingredient item() {
        return item;
    }

    /** Which mob's head {@link #item} must be, if it's a particular one. */
    public Optional<EntityType<?>> mobHead() {
        return mobHead;
    }

    public Ingredient input() {
        return input;
    }

    public ItemStack result() {
        return result;
    }

    @Override
    public boolean matches(UpgradingInput in, Level level) {
        return item.test(in.item()) && input.test(in.input())
                && mobHead.map(mob -> mob == in.item().get(Registration.MOB_HEAD_TYPE.get())).orElse(true);
    }

    @Override
    public ItemStack assemble(UpgradingInput in, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<UpgradingRecipe> getSerializer() {
        return Registration.UPGRADING_SERIALIZER.get();
    }

    @Override
    public RecipeType<UpgradingRecipe> getType() {
        return Registration.UPGRADING_TYPE.get();
    }
}
