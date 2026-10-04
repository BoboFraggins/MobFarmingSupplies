package net.bobofraggins.mobfarmingsupplies.fabric;

import net.bobofraggins.mobfarmingsupplies.fabric.MFSConfigImpl;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.FabricAbsorptionHopperFluidStorage;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.FabricAbsorptionHopperItemStorage;
import net.bobofraggins.mobfarmingsupplies.crafting.FabricFluidContainerIngredient;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.FabricExperienceSyringeFluidStorage;
import net.bobofraggins.mobfarmingsupplies.loot.FabricLootModifiers;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SideMode;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.fabric.LogisticSorterItemStorage;
import net.bobofraggins.mobfarmingsupplies.register.ModCompatRegistration;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.FabricTankFluidStorage;
import net.bobofraggins.mobfarmingsupplies.tank.FabricTankItemFluidStorage;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.toilet.fabric.ToiletItemStorage;
import net.bobofraggins.mobfarmingsupplies.toilet.fabric.ToiletWaterStorage;
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
        ServerLifecycleEvents.SERVER_STARTING.register(server -> MFSConfigImpl.load());
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

        // Logistic Sorter: insert-only, and only on INPUT sides.
        ItemStorage.SIDED.registerForBlockEntities(
                (be, direction) -> {
                    LogisticSorterBlockEntity sorter = (LogisticSorterBlockEntity) be;
                    return direction != null && sorter.getSide(direction) == SideMode.INPUT
                            ? new LogisticSorterItemStorage(sorter) : null;
                },
                Registration.LOGISTIC_SORTER_BE_TYPE.get());

        // Toilet: voids any item pushed in and supplies unlimited water, on every side.
        ItemStorage.SIDED.registerForBlockEntities(
                (be, direction) -> ToiletItemStorage.INSTANCE,
                Registration.TOILET_BE_TYPE.get());
        FluidStorage.SIDED.registerForBlockEntities(
                (be, direction) -> ToiletWaterStorage.INSTANCE,
                Registration.TOILET_BE_TYPE.get());
    }
}
