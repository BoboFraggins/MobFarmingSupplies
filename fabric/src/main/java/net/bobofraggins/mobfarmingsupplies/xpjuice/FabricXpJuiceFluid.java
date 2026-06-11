package net.bobofraggins.mobfarmingsupplies.xpjuice;

import dev.architectury.core.fluid.ArchitecturyFlowingFluid;
import dev.architectury.core.fluid.ArchitecturyFluidAttributes;
import dev.architectury.core.fluid.SimpleArchitecturyFluidAttributes;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

/**
 * Fabric XP Juice fluid — Source and Flowing variants extending architectury's
 * {@link ArchitecturyFlowingFluid}, which wires the fluid's name and textures into
 * Fabric's {@code FluidVariantAttributes}/{@code FluidVariantRendering} registries
 * (used by Jade tooltips, item-form rendering, etc.).
 *
 * <p>NeoForge uses {@code XpJuiceFluid} (extends {@code BaseFlowingFluid}) instead.
 * Both are registered under the same registry ID {@code mobfarmingsupplies:xp_juice}.
 */
public final class FabricXpJuiceFluid {

    private FabricXpJuiceFluid() {}

    private static final ArchitecturyFluidAttributes ATTRIBUTES = SimpleArchitecturyFluidAttributes
            .of(() -> Registration.XP_JUICE_FLOWING.get(), () -> Registration.XP_JUICE_SOURCE.get())
            .sourceTexture(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/xp_juice_still"))
            .flowingTexture(Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/xp_juice_flow"))
            .color(0xFF39FF14) // neon green, matches NeoForge XpJuiceClientEvents
            .block(Registration.XP_JUICE_BLOCK)
            .bucketItem(Registration.XP_JUICE_BUCKET::toOptional)
            .slopeFindDistance(2)
            .dropOff(1)
            .tickDelay(20)
            .explosionResistance(100.0F)
            .density(900)
            .temperature(300)
            .viscosity(1500)
            .luminosity(10)
            .fillSound(SoundEvents.PLAYER_LEVELUP)
            .emptySound(SoundEvents.EXPERIENCE_ORB_PICKUP);

    public static class Source extends ArchitecturyFlowingFluid.Source {
        public Source() {
            super(ATTRIBUTES);
        }
    }

    public static class Flowing extends ArchitecturyFlowingFluid.Flowing {
        public Flowing() {
            super(ATTRIBUTES);
        }
    }
}
