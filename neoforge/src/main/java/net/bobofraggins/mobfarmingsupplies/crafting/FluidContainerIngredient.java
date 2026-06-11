package net.bobofraggins.mobfarmingsupplies.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

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
public class FluidContainerIngredient implements ICustomIngredient {

    public static final MapCodec<FluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC
                            .xmap(id -> TagKey.create(Registries.FLUID, id), TagKey::location)
                            .fieldOf("fluid_tag")
                            .forGetter(i -> i.fluidTag),
                    Codec.INT
                            .optionalFieldOf("amount", 1000)
                            .forGetter(i -> i.amount)
            ).apply(instance, FluidContainerIngredient::new));

    // Assigned by Registration.register() so this class doesn't need to import Registration.
    public static DeferredHolder<IngredientType<?>, IngredientType<FluidContainerIngredient>> typeHolder;

    private static Holder<Item> xpJuiceBucketHolder;

    private final TagKey<Fluid> fluidTag;
    private final int amount;

    public FluidContainerIngredient(TagKey<Fluid> fluidTag, int amount) {
        this.fluidTag = fluidTag;
        this.amount = amount;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack.isEmpty()) return false;
        SimpleContainer tmp = new SimpleContainer(stack.copy());
        var wrapper = VanillaContainerWrapper.of(tmp);
        var access = ItemAccess.forHandlerIndex(wrapper, 0).oneByOne();
        var cap = stack.getCapability(Capabilities.Fluid.ITEM, access);
        if (cap == null) return false;
        for (int i = 0; i < cap.size(); i++) {
            FluidResource res = cap.getResource(i);
            if (!res.isEmpty()
                    && res.toStack(1).is(fluidTag)
                    && cap.getAmountAsLong(i) >= amount) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Stream<Holder<Item>> items() {
        if (xpJuiceBucketHolder == null)
            xpJuiceBucketHolder = MGRRegistryHelper.getItem("xp_juice_bucket").builtInRegistryHolder();
        return Stream.of(xpJuiceBucketHolder);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return typeHolder.get();
    }
}
