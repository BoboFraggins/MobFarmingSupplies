package net.bobofraggins.mobfarmingsupplies.neoforge.tritanium;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

/** Draws the molten metals. Registered only alongside them (see {@link MoltenTritanium}). */
public final class MoltenMetalClientEvents {

    private MoltenMetalClientEvents() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        for (MoltenMetal metal : MoltenTritanium.all()) {
            event.registerFluidType(new IClientFluidTypeExtensions() {}, metal.type.get());
        }
    }

    @SubscribeEvent
    public static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
        for (MoltenMetal metal : MoltenTritanium.all()) {
            var unbaked = new FluidModel.Unbaked(
                    new Material(MoltenMetal.id("block/" + metal.name + "_still")),
                    new Material(MoltenMetal.id("block/" + metal.name + "_flow")),
                    null,
                    FluidTintSources.constant(0xFFFFFFFF));
            event.register(unbaked, metal.source.get(), metal.flowing.get());
        }
    }
}
