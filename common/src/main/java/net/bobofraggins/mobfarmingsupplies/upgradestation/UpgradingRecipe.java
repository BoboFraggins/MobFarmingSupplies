package net.bobofraggins.mobfarmingsupplies.upgradestation;

import net.bobofraggins.mobfarmingsupplies.shared.recipe.IdentifiedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.MFSStreamCodecs;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ByteBufCodecs;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
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
public class UpgradingRecipe implements Recipe<UpgradingInput>, IdentifiedRecipe {

    private ResourceLocation id = new ResourceLocation("mobfarmingsupplies", "unnamed");

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public static final MapCodec<UpgradingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            MFSCodecs.INGREDIENT_NONEMPTY.fieldOf("item").forGetter(UpgradingRecipe::item),
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("mob_head").forGetter(UpgradingRecipe::mobHead),
            MFSCodecs.INGREDIENT_NONEMPTY.fieldOf("input").forGetter(UpgradingRecipe::input),
            MFSCodecs.RESULT_STACK.fieldOf("result").forGetter(UpgradingRecipe::result)
    ).apply(i, UpgradingRecipe::new));

    public static final StreamCodec<FriendlyByteBuf, UpgradingRecipe> STREAM_CODEC = StreamCodec.composite(
            MFSStreamCodecs.INGREDIENT, UpgradingRecipe::item,
            ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ENTITY_TYPE)), UpgradingRecipe::mobHead,
            MFSStreamCodecs.INGREDIENT, UpgradingRecipe::input,
            MFSStreamCodecs.ITEM_STACK, UpgradingRecipe::result,
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
                && mobHead.map(mob -> mob == ItemComponents.get(in.item(), Registration.MOB_HEAD_TYPE.get())).orElse(true);
    }

    @Override
    public ItemStack assemble(UpgradingInput in, net.minecraft.core.RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registries) {
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
