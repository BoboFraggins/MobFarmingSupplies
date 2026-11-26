package net.bobofraggins.mobfarmingsupplies.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.fluid.fabric.FabricFluidUnits;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.bobofraggins.mobfarmingsupplies.shared.compat.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

/**
 * Fabric equivalent of {@code FluidContainerIngredient} (NeoForge).
 * Matches any item whose Fabric fluid storage contains at least {@link #amount}
 * mB of a fluid in {@link #fluidTag}.
 *
 * <p>Recipe JSON format (Fabric):
 * <pre>{@code
 * {
 *   "fabric:type": "mobfarmingsupplies:fluid_container",
 *   "fluid_tag": "c:experience",
 *   "amount": 1000
 * }
 * }</pre>
 *
 * <p>Note: Fabric Transfer API measures fluids in droplets (1 mB = 81 droplets).
 * The {@code amount} field uses mB for parity with the NeoForge ingredient.
 */
@SuppressWarnings("UnstableApiUsage")
public class FabricFluidContainerIngredient implements CustomIngredient {

    public static final ResourceLocation ID =
            new ResourceLocation(MobFarmingSuppliesCommon.MODID, "fluid_container");

    public static final MapCodec<FabricFluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC
                            .xmap(id -> TagKey.create(Registries.FLUID, id), TagKey::location)
                            .fieldOf("fluid_tag")
                            .forGetter(i -> i.fluidTag),
                    Codec.INT
                            .optionalFieldOf("amount", 1000)
                            .forGetter(i -> i.amount)
            ).apply(instance, FabricFluidContainerIngredient::new));

    public static final StreamCodec<FriendlyByteBuf, FabricFluidContainerIngredient> PACKET_CODEC =
            StreamCodec.of(
                    (buf, val) -> {
                        buf.writeUtf(val.fluidTag.location().toString());
                        buf.writeVarInt(val.amount);
                    },
                    buf -> new FabricFluidContainerIngredient(
                            TagKey.create(Registries.FLUID, new ResourceLocation(buf.readUtf())),
                            buf.readVarInt()
                    )
            );

    public static final CustomIngredientSerializer<FabricFluidContainerIngredient> SERIALIZER =
            new CustomIngredientSerializer<>() {
                @Override
                public ResourceLocation getIdentifier() { return ID; }

                @Override
                public FabricFluidContainerIngredient read(com.google.gson.JsonObject json) {
                    return CODEC.codec().parse(com.mojang.serialization.JsonOps.INSTANCE, json)
                            .getOrThrow(false, msg -> {});
                }

                @Override
                public void write(com.google.gson.JsonObject json, FabricFluidContainerIngredient ingredient) {
                    CODEC.codec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, ingredient).result()
                            .ifPresent(e -> e.getAsJsonObject().entrySet().forEach(x -> json.add(x.getKey(), x.getValue())));
                }

                @Override
                public FabricFluidContainerIngredient read(FriendlyByteBuf buf) {
                    return PACKET_CODEC.decode(buf);
                }

                @Override
                public void write(FriendlyByteBuf buf, FabricFluidContainerIngredient ingredient) {
                    PACKET_CODEC.encode(buf, ingredient);
                }
            };

    private final TagKey<Fluid> fluidTag;
    private final int amount;

    public FabricFluidContainerIngredient(TagKey<Fluid> fluidTag, int amount) {
        this.fluidTag = fluidTag;
        this.amount = amount;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ContainerItemContext ctx = ContainerItemContext.withConstant(stack);
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ctx);
        if (storage == null) return false;
        for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
            FluidVariant variant = view.getResource();
            if (variant.getFluid().builtInRegistryHolder().is(fluidTag)
                    && view.getAmount() >= FabricFluidUnits.toDroplets(amount)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        Item item = MFSRegistryHelper.getItem("xp_juice_bucket");
        if (item == null || item == Items.AIR) return List.of();
        return List.of(new ItemStack(item));
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
