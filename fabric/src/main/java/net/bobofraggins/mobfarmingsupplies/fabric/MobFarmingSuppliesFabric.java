package net.bobofraggins.mobfarmingsupplies.fabric;

import dev.architectury.platform.Platform;
import net.bobofraggins.mobfarmingsupplies.fabric.MFSConfigImpl;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.FabricAbsorptionHopperFluidStorage;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.FabricAbsorptionHopperItemStorage;
import net.bobofraggins.mobfarmingsupplies.crafting.FabricFluidContainerIngredient;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.FabricExperienceSyringeFluidStorage;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.fabric.MagicHatTrinketSetup;
import net.bobofraggins.mobfarmingsupplies.loot.FabricLootModifiers;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.SideMode;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.fabric.LogisticSorterItemStorage;
import net.bobofraggins.mobfarmingsupplies.register.ModCompatRegistration;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperSide;
import net.bobofraggins.mobfarmingsupplies.omnihopper.HopperNode;
import net.bobofraggins.mobfarmingsupplies.omnihopper.fabric.OmniHopperInsertStorage;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.tank.FabricTankFluidStorage;
import net.bobofraggins.mobfarmingsupplies.tank.FabricTankItemFluidStorage;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.toilet.fabric.ToiletItemStorage;
import net.bobofraggins.mobfarmingsupplies.toilet.fabric.ToiletWaterStorage;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.item.Items;

@SuppressWarnings("UnstableApiUsage")
public class MobFarmingSuppliesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // TODO Phase 4: server-side events will be registered here (EnderInhibitor ender teleport mixin)
        ServerLifecycleEvents.SERVER_STARTING.register(server -> MFSConfigImpl.load());
        ModCompatRegistration.register();
        Registration.register();
        CustomIngredientSerializer.register(FabricFluidContainerIngredient.SERIALIZER);
        // Fabric only sends vanilla recipe types to clients; without this, JEI (which reads the
        // client's copy) never sees the Tank Upgrade or Einstein-Rosen Bridge recipes.
        RecipeSynchronization.synchronizeRecipeSerializer(Registration.TANK_UPGRADE_SERIALIZER.get());
        RecipeSynchronization.synchronizeRecipeSerializer(Registration.BRIDGE_PAIR_SERIALIZER.get());
        RecipeSynchronization.synchronizeRecipeSerializer(Registration.BRIDGE_LINK_SERIALIZER.get());
        RecipeSynchronization.synchronizeRecipeSerializer(Registration.ANVIL_CRUSHING_SERIALIZER.get());
        FabricLootModifiers.register();
        registerStorages();
        // Magic Hat Trinkets Updated integration — soft dependency, registered only if present.
        // MUST check isModLoaded() before ever calling into MagicHatTrinketSetup: that class
        // references Trinkets API types, and simply loading it (even just to have this class's
        // own bytecode verified) would throw NoClassDefFoundError without Trinkets Updated
        // installed — see MagicHatTrinketSetup's javadoc.
        if (Platform.isModLoaded("trinkets_updated")) {
            MagicHatTrinketSetup.registerCommon();
        }
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
                Registration.TANK_ITEM.get(), Registration.GOLD_TANK_ITEM.get(),
                Registration.DIAMOND_TANK_ITEM.get(), Registration.EMERALD_TANK_ITEM.get());

        // Item-side fluid storage for the Experience Syringe.
        FluidStorage.ITEM.registerForItems(
                (stack, ctx) -> new FabricExperienceSyringeFluidStorage(ctx),
                Registration.EXPERIENCE_SYRINGE.get());

        // Block-side item storage for the Absorption Hopper.
        ItemStorage.SIDED.registerForBlockEntities(
                (be, direction) -> FabricAbsorptionHopperItemStorage.of((AbsorptionHopperBlockEntity) be),
                Registration.ABSORPTION_HOPPER_BE_TYPE.get());

        // Vanilla's plain empty bucket needs its own registration to know it can be
        // filled with (or emptied of) XP Juice — the generic BucketItem fallback that
        // Fabric API provides automatically only covers the FULL custom bucket item
        // (XP_JUICE_BUCKET) draining into / filling from things, not the empty bucket
        // side of that same interaction.
        FluidStorage.combinedItemApiProvider(Items.BUCKET).register(ctx ->
                new FullItemFluidStorage(ctx, Registration.XP_JUICE_BUCKET.get(),
                        FluidVariant.of(Registration.XP_JUICE_SOURCE.get()), FluidConstants.BUCKET));

        // Logistic Sorter: insert-only, and only on INPUT sides.
        ItemStorage.SIDED.registerForBlockEntities(
                (be, direction) -> {
                    LogisticSorterBlockEntity sorter = (LogisticSorterBlockEntity) be;
                    return direction != null && sorter.getSide(direction) == SideMode.INPUT
                            ? new LogisticSorterItemStorage(sorter) : null;
                },
                Registration.LOGISTIC_SORTER_BE_TYPE.get());

        // Omnidirectional Hopper and Einstein-Rosen Bridge: insert-only items and fluids, only on INPUT sides.
        registerHopperNodeStorages(Registration.OMNI_HOPPER_BE_TYPE.get());
        registerHopperNodeStorages(Registration.BRIDGE_BE_TYPE.get());

        // Toilet: voids any item pushed in and supplies unlimited water, on every side.
        ItemStorage.SIDED.registerForBlockEntities(
                (be, direction) -> new ToiletItemStorage(be.getLevel(), be.getBlockPos()),
                Registration.TOILET_BE_TYPE.get());
        FluidStorage.SIDED.registerForBlockEntities(
                (be, direction) -> ToiletWaterStorage.INSTANCE,
                Registration.TOILET_BE_TYPE.get());
    }

    private static void registerHopperNodeStorages(BlockEntityType<?> type) {
        ItemStorage.SIDED.registerForBlockEntities(
                (be, direction) -> {
                    HopperNode node = (HopperNode) be;
                    return direction != null && node.getSide(direction) == HopperSide.INPUT
                            ? new OmniHopperInsertStorage<>(node, ItemStorage.SIDED, v -> node.allowsItem(v.toStack()))
                            : null;
                },
                type);
        FluidStorage.SIDED.registerForBlockEntities(
                (be, direction) -> {
                    HopperNode node = (HopperNode) be;
                    return direction != null && node.getSide(direction) == HopperSide.INPUT
                            ? new OmniHopperInsertStorage<>(node, FluidStorage.SIDED, v -> true)
                            : null;
                },
                type);
    }
}
