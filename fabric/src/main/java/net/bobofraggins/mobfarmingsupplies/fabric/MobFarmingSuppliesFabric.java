package net.bobofraggins.mobfarmingsupplies.fabric;

import net.bobofraggins.mobfarmingsupplies.fabric.MGRConfigImpl;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.FabricAbsorptionHopperFluidStorage;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.FabricAbsorptionHopperItemStorage;
import net.bobofraggins.mobfarmingsupplies.crafting.FabricFluidContainerIngredient;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.FabricExperienceSyringeFluidStorage;
import net.bobofraggins.mobfarmingsupplies.loot.FabricLootModifiers;
import net.bobofraggins.mobfarmingsupplies.register.ModCompatRegistration;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.FabricTankFluidStorage;
import net.bobofraggins.mobfarmingsupplies.tank.FabricTankItemFluidStorage;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

@SuppressWarnings("UnstableApiUsage")
public class MobFarmingSuppliesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // TODO Phase 4: server-side events will be registered here (EnderInhibitor ender teleport mixin)
        ServerLifecycleEvents.SERVER_STARTING.register(server -> MGRConfigImpl.load());
        ModCompatRegistration.register();
        Registration.register();
        CustomIngredientSerializer.register(FabricFluidContainerIngredient.SERIALIZER);
        FabricLootModifiers.register();
        registerStorages();
        MobFarmingSuppliesCommon.init();
    }

    private static void registerStorages() {
        // Block-side fluid storage for TankBlockEntity.
        FluidStorage.SIDED.registerForBlockEntities(
                (be, direction) -> new FabricTankFluidStorage((TankBlockEntity) be),
                Registration.TANK_BE_TYPE.get());

        // Block-side fluid storage for AbsorptionHopperBlockEntity.
        FluidStorage.SIDED.registerForBlockEntities(
                (be, direction) -> new FabricAbsorptionHopperFluidStorage((AbsorptionHopperBlockEntity) be),
                Registration.ABSORPTION_HOPPER_BE_TYPE.get());

        // Item-side fluid storage for the Tank block item.
        FluidStorage.ITEM.registerForItems(
                (stack, ctx) -> new FabricTankItemFluidStorage(stack, ctx),
                Registration.TANK_ITEM.get());

        // Item-side fluid storage for the Experience Syringe.
        FluidStorage.ITEM.registerForItems(
                (stack, ctx) -> new FabricExperienceSyringeFluidStorage(ctx),
                Registration.EXPERIENCE_SYRINGE.get());

        // Block-side item storage for the Absorption Hopper.
        ItemStorage.SIDED.registerForBlockEntities(
                (be, direction) -> FabricAbsorptionHopperItemStorage.of((AbsorptionHopperBlockEntity) be),
                Registration.ABSORPTION_HOPPER_BE_TYPE.get());
    }
}
