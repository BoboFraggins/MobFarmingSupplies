package net.bobofraggins.mobfarmingsupplies.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.RegistryObject;

import java.util.stream.Stream;

/**
 * A custom crafting ingredient that matches any item whose fluid capability
 * contains at least {@link #amount} mB of a fluid tagged {@link #fluidTag}.
 *
 * <p>JSON format:
 * <pre>{@code
 * {
 *   "type": "mobfarmingsupplies:fluid_container",
 *   "fluid_tag": "c:experience",
 *   "amount": 1000
 * }
 * }</pre>
 */
public class FluidContainerIngredient extends AbstractIngredient {

    public static final ResourceLocation ID = new ResourceLocation(MobFarmingSuppliesCommon.MODID, "fluid_container");

    public static final MapCodec<FluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC
                            .xmap(id -> TagKey.create(Registries.FLUID, id), TagKey::location)
                            .fieldOf("fluid_tag")
                            .forGetter(i -> i.fluidTag),
                    Codec.INT
                            .optionalFieldOf("amount", 1000)
                            .forGetter(i -> i.amount)
            ).apply(instance, FluidContainerIngredient::new));

    /** Reads and writes the ingredient (Forge 1.20.1 registers it with {@code CraftingHelper.register}). */
    public static final IIngredientSerializer<FluidContainerIngredient> SERIALIZER = new IIngredientSerializer<>() {
        @Override
        public FluidContainerIngredient parse(FriendlyByteBuf buf) {
            return new FluidContainerIngredient(TagKey.create(Registries.FLUID, buf.readResourceLocation()), buf.readVarInt());
        }

        @Override
        public FluidContainerIngredient parse(JsonObject json) {
            return CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> {});
        }

        @Override
        public void write(FriendlyByteBuf buf, FluidContainerIngredient ingredient) {
            buf.writeResourceLocation(ingredient.fluidTag.location());
            buf.writeVarInt(ingredient.amount);
        }
    };

    private final TagKey<Fluid> fluidTag;
    private final int amount;

    public FluidContainerIngredient(TagKey<Fluid> fluidTag, int amount) {
        this.fluidTag = fluidTag;
        this.amount = amount;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var cap = stack.copyWithCount(1).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (cap == null) return false;
        for (int i = 0; i < cap.getTanks(); i++) {
            var fluid = cap.getFluidInTank(i);
            if (!fluid.isEmpty() && fluid.getFluid().is(fluidTag) && fluid.getAmount() >= amount) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack[] getItems() {
        return new ItemStack[]{new ItemStack(MFSRegistryHelper.getItem("xp_juice_bucket"))};
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", ID.toString());
        json.addProperty("fluid_tag", fluidTag.location().toString());
        json.addProperty("amount", amount);
        return json;
    }
}
