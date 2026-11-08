package net.bobofraggins.mobfarmingsupplies.essencehopper.fabric;

import dev.architectury.core.fluid.ArchitecturyFlowingFluid;
import dev.architectury.core.fluid.ArchitecturyFluidAttributes;
import dev.architectury.core.fluid.SimpleArchitecturyFluidAttributes;
import java.util.EnumMap;
import java.util.Map;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;

public final class EssenceFluidPlatformImpl {

    private static final Map<Essence, ArchitecturyFluidAttributes> ATTRIBUTES = new EnumMap<>(Essence.class);

    private EssenceFluidPlatformImpl() {}

    public static FlowingFluid createSource(Essence essence) {
        return new ArchitecturyFlowingFluid.Source(attributes(essence));
    }

    public static FlowingFluid createFlowing(Essence essence) {
        return new ArchitecturyFlowingFluid.Flowing(attributes(essence));
    }

    /** Lava's flow. Attributes don't convert to new sources unless asked to. */
    private static ArchitecturyFluidAttributes attributes(Essence essence) {
        return ATTRIBUTES.computeIfAbsent(essence, e -> SimpleArchitecturyFluidAttributes
                .of(() -> Registration.ESSENCE_FLOWING.get(e).get(), () -> Registration.ESSENCE_SOURCES.get(e).get())
                .sourceTexture(ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/" + e.fluidId() + "_still"))
                .flowingTexture(ResourceLocation.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/" + e.fluidId() + "_flow"))
                .color(0xFFFFFFFF) // no tint: the textures are coloured themselves
                .block(Registration.ESSENCE_BLOCKS.get(e))
                .bucketItem(() -> Registration.ESSENCE_BUCKETS.get(e).toOptional().map(item -> (Item) item))
                .slopeFindDistance(2)
                .dropOff(2)
                .tickDelay(30)
                .explosionResistance(100.0F)
                .density(3000)
                .temperature(300)
                .viscosity(6000)
                .luminosity(15)
                .fillSound(SoundEvents.BUCKET_FILL_LAVA)
                .emptySound(SoundEvents.BUCKET_EMPTY_LAVA));
    }
}
