package net.bobofraggins.mobfarmingsupplies.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

import java.util.stream.Stream;

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

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "fluid_container");

    public static final MapCodec<FabricFluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC
                            .xmap(id -> TagKey.create(Registries.FLUID, id), TagKey::location)
                            .fieldOf("fluid_tag")
                            .forGetter(i -> i.fluidTag),
                    Codec.INT
                            .optionalFieldOf("amount", 1000)
                            .forGetter(i -> i.amount)
            ).apply(instance, FabricFluidContainerIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FabricFluidContainerIngredient> PACKET_CODEC =
            StreamCodec.of(
                    (buf, val) -> {
                        buf.writeUtf(val.fluidTag.location().toString());
                        buf.writeVarInt(val.amount);
                    },
                    buf -> new FabricFluidContainerIngredient(
                            TagKey.create(Registries.FLUID, Identifier.parse(buf.readUtf())),
                            buf.readVarInt()
                    )
            );

    public static final CustomIngredientSerializer<FabricFluidContainerIngredient> SERIALIZER =
            new CustomIngredientSerializer<>() {
                @Override
                public Identifier getIdentifier() { return ID; }

                @Override
                public MapCodec<FabricFluidContainerIngredient> getCodec() { return CODEC; }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, FabricFluidContainerIngredient> getStreamCodec() {
                    return PACKET_CODEC;
                }
            };

    private static final long DROPLETS_PER_MB = 81L;

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
                    && view.getAmount() >= (long) amount * DROPLETS_PER_MB) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Stream<Holder<Item>> items() {
        Item item = MGRRegistryHelper.getItem("xp_juice_bucket");
        if (item == null || item == Items.AIR) return Stream.empty();
        return Stream.of(item.builtInRegistryHolder());
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
