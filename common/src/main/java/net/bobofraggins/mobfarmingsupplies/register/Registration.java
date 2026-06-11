package net.bobofraggins.mobfarmingsupplies.register;

import com.mojang.serialization.Codec;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.MGRConfig;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlock;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperMenu;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticBlock;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticBlockEntity;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticMenu;
import net.bobofraggins.mobfarmingsupplies.dna.DnaCollectorItem;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleContents;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSamplePackItem;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlock;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntity;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.ExperienceSyringeItem;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlock;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntity;
import net.bobofraggins.mobfarmingsupplies.fan.FanMenu;
import net.bobofraggins.mobfarmingsupplies.fan.FanUpgradeItem;
import net.bobofraggins.mobfarmingsupplies.mobharvester.HarvesterSword;
import net.bobofraggins.mobfarmingsupplies.mobharvester.HarvesterUpgradeItem;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterBlock;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterMenu;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlock;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockItem;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.bobofraggins.mobfarmingsupplies.tank.TankMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.bobofraggins.mobfarmingsupplies.mobexclusionglass.MobExclusionGlassBlock;
import net.bobofraggins.mobfarmingsupplies.vectorplate.VectorPlateBlock;
import net.bobofraggins.mobfarmingsupplies.witherproofglass.WitherProofGlassBlock;
import net.bobofraggins.mobfarmingsupplies.xpjuice.XpJuicePlatformHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class Registration {

    private Registration() {}

    // MC 26.x requires Properties.setId() before block/item construction.
    // Architectury's DeferredRegister does not inject the id automatically,
    // so we call setId() explicitly in every registration lambda.
    private static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, name));
    }

    static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, name));
    }

    // ── Fluid tags ──────────────────────────────────────────────────────────────

    public static final TagKey<Fluid> TAG_EXPERIENCE_FLUID =
            TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "experience"));

    // ── Deferred registers ──────────────────────────────────────────────────────

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.BLOCK);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.ITEM);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.BLOCK_ENTITY_TYPE);

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.MENU);

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.DATA_COMPONENT_TYPE);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.FLUID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.CREATIVE_MODE_TAB);

    // ── Data components ─────────────────────────────────────────────────────────

    public static final RegistrySupplier<DataComponentType<DnaSampleContents>> DNA_SAMPLE_CONTENTS =
            DATA_COMPONENTS.register("dna_sample_contents",
                    () -> DataComponentType.<DnaSampleContents>builder()
                            .persistent(DnaSampleContents.CODEC)
                            .networkSynchronized(DnaSampleContents.STREAM_CODEC)
                            .build());

    public static final RegistrySupplier<DataComponentType<Integer>> BEHEADING_LEVEL =
            DATA_COMPONENTS.register("beheading_level",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build());

    public static final RegistrySupplier<DataComponentType<Integer>> EXPERIENCE_SYRINGE_STORED_XP =
            DATA_COMPONENTS.register("experience_syringe_stored_xp",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.intRange(0, ExperienceSyringeItem.CAPACITY))
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    public static final RegistrySupplier<DataComponentType<TankContents>> TANK_CONTENTS =
            DATA_COMPONENTS.register("tank_contents",
                    () -> DataComponentType.<TankContents>builder()
                            .persistent(TankContents.CODEC)
                            .networkSynchronized(TankContents.STREAM_CODEC)
                            .build());

    // ── DNA items ────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<DnaCollectorItem> DNA_COLLECTOR =
            ITEMS.register("dna_collector",
                    () -> new DnaCollectorItem(new Item.Properties()
                            .setId(itemKey("dna_collector"))
                            .stacksTo(DnaCollectorItem.STACK_SIZE)));

    public static final RegistrySupplier<DnaSampleItem> DNA_SAMPLE =
            ITEMS.register("dna_sample",
                    () -> new DnaSampleItem(new Item.Properties()
                            .setId(itemKey("dna_sample"))
                            .stacksTo(1)));

    // ── Mob Harvester ────────────────────────────────────────────────────────────

    public static final RegistrySupplier<MobHarvesterBlock> MOB_HARVESTER =
            BLOCKS.register("mob_harvester",
                    () -> new MobHarvesterBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("mob_harvester"))
                            .strength(3.5f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> MOB_HARVESTER_ITEM =
            ITEMS.register("mob_harvester",
                    () -> new BlockItem(MOB_HARVESTER.get(), new Item.Properties()
                            .setId(itemKey("mob_harvester"))));

    public static final RegistrySupplier<MenuType<MobHarvesterMenu>> MOB_HARVESTER_MENU =
            MENUS.register("mob_harvester", () -> MenuRegistry.ofExtended(MobHarvesterMenu::new));

    public static final RegistrySupplier<BlockEntityType<MobHarvesterBlockEntity>> MOB_HARVESTER_BE_TYPE =
            BLOCK_ENTITIES.register("mob_harvester",
                    () -> BlockEntityTypePlatform.create(MobHarvesterBlockEntity::new, MOB_HARVESTER.get()));

    public static final RegistrySupplier<HarvesterUpgradeItem> HARVESTER_UPGRADE_SHARPNESS =
            ITEMS.register("harvester_upgrade_sharpness",
                    () -> new HarvesterUpgradeItem(new Item.Properties()
                            .setId(itemKey("harvester_upgrade_sharpness")),
                            HarvesterUpgradeItem.UpgradeType.SHARPNESS));

    public static final RegistrySupplier<HarvesterUpgradeItem> HARVESTER_UPGRADE_LOOTING =
            ITEMS.register("harvester_upgrade_looting",
                    () -> new HarvesterUpgradeItem(new Item.Properties()
                            .setId(itemKey("harvester_upgrade_looting")),
                            HarvesterUpgradeItem.UpgradeType.LOOTING));

    public static final RegistrySupplier<HarvesterUpgradeItem> HARVESTER_UPGRADE_BEHEADING =
            ITEMS.register("harvester_upgrade_beheading",
                    () -> new HarvesterUpgradeItem(new Item.Properties()
                            .setId(itemKey("harvester_upgrade_beheading")),
                            HarvesterUpgradeItem.UpgradeType.BEHEADING));

    public static final RegistrySupplier<Item> HARVESTER_SWORD =
            ITEMS.register("harvester_sword",
                    () -> new HarvesterSword(new Item.Properties()
                            .setId(itemKey("harvester_sword"))));

    // ── Clone-O-Matic ─────────────────────────────────────────────────────────────

    public static final RegistrySupplier<CloneOMaticBlock> CLONE_O_MATIC =
            BLOCKS.register("clone_o_matic",
                    () -> new CloneOMaticBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("clone_o_matic"))
                            .strength(3.5f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> CLONE_O_MATIC_ITEM =
            ITEMS.register("clone_o_matic",
                    () -> new BlockItem(CLONE_O_MATIC.get(), new Item.Properties()
                            .setId(itemKey("clone_o_matic"))));

    public static final RegistrySupplier<MenuType<CloneOMaticMenu>> CLONE_O_MATIC_MENU =
            MENUS.register("clone_o_matic", () -> MenuRegistry.ofExtended(CloneOMaticMenu::new));

    public static final RegistrySupplier<BlockEntityType<CloneOMaticBlockEntity>> CLONE_O_MATIC_BE_TYPE =
            BLOCK_ENTITIES.register("clone_o_matic",
                    () -> BlockEntityTypePlatform.create(CloneOMaticBlockEntity::new, CLONE_O_MATIC.get()));

    // ── Absorption Hopper ─────────────────────────────────────────────────────────

    public static final RegistrySupplier<AbsorptionHopperBlock> ABSORPTION_HOPPER =
            BLOCKS.register("absorption_hopper",
                    () -> new AbsorptionHopperBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("absorption_hopper"))
                            .strength(3.5f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> ABSORPTION_HOPPER_ITEM =
            ITEMS.register("absorption_hopper",
                    () -> new BlockItem(ABSORPTION_HOPPER.get(), new Item.Properties()
                            .setId(itemKey("absorption_hopper"))));

    public static final RegistrySupplier<MenuType<AbsorptionHopperMenu>> ABSORPTION_HOPPER_MENU =
            MENUS.register("absorption_hopper", () -> MenuRegistry.ofExtended(AbsorptionHopperMenu::new));

    public static final RegistrySupplier<BlockEntityType<AbsorptionHopperBlockEntity>> ABSORPTION_HOPPER_BE_TYPE =
            BLOCK_ENTITIES.register("absorption_hopper",
                    () -> BlockEntityTypePlatform.create(AbsorptionHopperBlockEntity::new, ABSORPTION_HOPPER.get()));

    // ── Ender Inhibitor ───────────────────────────────────────────────────────────

    public static final RegistrySupplier<EnderInhibitorBlock> ENDER_INHIBITOR =
            BLOCKS.register("ender_inhibitor",
                    () -> new EnderInhibitorBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("ender_inhibitor"))
                            .strength(3.0f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> ENDER_INHIBITOR_ITEM =
            ITEMS.register("ender_inhibitor",
                    () -> new BlockItem(ENDER_INHIBITOR.get(), new Item.Properties()
                            .setId(itemKey("ender_inhibitor"))));

    public static final RegistrySupplier<BlockEntityType<EnderInhibitorBlockEntity>> ENDER_INHIBITOR_BE_TYPE =
            BLOCK_ENTITIES.register("ender_inhibitor",
                    () -> BlockEntityTypePlatform.create(EnderInhibitorBlockEntity::new, ENDER_INHIBITOR.get()));

    // ── Fan ───────────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<FanBlock> FAN =
            BLOCKS.register("fan",
                    () -> new FanBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("fan"))
                            .strength(2.5f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> FAN_ITEM =
            ITEMS.register("fan",
                    () -> new BlockItem(FAN.get(), new Item.Properties()
                            .setId(itemKey("fan"))));

    public static final RegistrySupplier<MenuType<FanMenu>> FAN_MENU =
            MENUS.register("fan", () -> MenuRegistry.ofExtended(FanMenu::new));

    public static final RegistrySupplier<BlockEntityType<FanBlockEntity>> FAN_BE_TYPE =
            BLOCK_ENTITIES.register("fan",
                    () -> BlockEntityTypePlatform.create(FanBlockEntity::new, FAN.get()));

    public static final RegistrySupplier<FanUpgradeItem> FAN_UPGRADE_WIDTH =
            ITEMS.register("fan_upgrade_width",
                    () -> new FanUpgradeItem(FanUpgradeItem.UpgradeType.WIDTH, new Item.Properties()
                            .setId(itemKey("fan_upgrade_width"))));

    public static final RegistrySupplier<FanUpgradeItem> FAN_UPGRADE_HEIGHT =
            ITEMS.register("fan_upgrade_height",
                    () -> new FanUpgradeItem(FanUpgradeItem.UpgradeType.HEIGHT, new Item.Properties()
                            .setId(itemKey("fan_upgrade_height"))));

    public static final RegistrySupplier<FanUpgradeItem> FAN_UPGRADE_DISTANCE =
            ITEMS.register("fan_upgrade_distance",
                    () -> new FanUpgradeItem(FanUpgradeItem.UpgradeType.DISTANCE, new Item.Properties()
                            .setId(itemKey("fan_upgrade_distance"))));

    // ── Vector Plate ──────────────────────────────────────────────────────────────

    public static final RegistrySupplier<VectorPlateBlock> VECTOR_PLATE =
            BLOCKS.register("vector_plate",
                    () -> new VectorPlateBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("vector_plate"))
                            .strength(2.0f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.METAL)
                            .isValidSpawn((state, level, pos, entityType) -> true)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> VECTOR_PLATE_ITEM =
            ITEMS.register("vector_plate",
                    () -> new BlockItem(VECTOR_PLATE.get(), new Item.Properties()
                            .setId(itemKey("vector_plate"))));

    // ── Wither-Proof Glass ────────────────────────────────────────────────────────

    public static final RegistrySupplier<WitherProofGlassBlock> WITHER_PROOF_GLASS =
            BLOCKS.register("wither_proof_glass",
                    () -> new WitherProofGlassBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("wither_proof_glass"))
                            .strength(1.5f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .isViewBlocking((s, r, p) -> false)));

    public static final RegistrySupplier<BlockItem> WITHER_PROOF_GLASS_ITEM =
            ITEMS.register("wither_proof_glass",
                    () -> new BlockItem(WITHER_PROOF_GLASS.get(), new Item.Properties()
                            .setId(itemKey("wither_proof_glass"))));

    // ── Mob Exclusion Glass ───────────────────────────────────────────────────────

    public static final RegistrySupplier<MobExclusionGlassBlock> MOB_EXCLUSION_GLASS =
            BLOCKS.register("mob_exclusion_glass",
                    () -> new MobExclusionGlassBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("mob_exclusion_glass"))
                            .strength(1.5f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .isViewBlocking((s, r, p) -> false)));

    public static final RegistrySupplier<BlockItem> MOB_EXCLUSION_GLASS_ITEM =
            ITEMS.register("mob_exclusion_glass",
                    () -> new BlockItem(MOB_EXCLUSION_GLASS.get(), new Item.Properties()
                            .setId(itemKey("mob_exclusion_glass"))));

    // ── XP Juice fluid ────────────────────────────────────────────────────────────

    public static final RegistrySupplier<FlowingFluid> XP_JUICE_SOURCE =
            FLUIDS.register("xp_juice", XpJuicePlatformHelper::createSourceFluid);

    public static final RegistrySupplier<FlowingFluid> XP_JUICE_FLOWING =
            FLUIDS.register("xp_juice_flowing", XpJuicePlatformHelper::createFlowingFluid);

    // Must be declared after SOURCE so the supplier resolves correctly.
    public static final RegistrySupplier<LiquidBlock> XP_JUICE_BLOCK =
            BLOCKS.register("xp_juice",
                    () -> new LiquidBlock(XP_JUICE_SOURCE.get(),
                            BlockBehaviour.Properties.of()
                                    .setId(blockKey("xp_juice"))
                                    .noCollision()
                                    .strength(100f)
                                    .noLootTable()
                                    .liquid()
                                    .replaceable()
                                    .pushReaction(PushReaction.DESTROY)));

    public static final RegistrySupplier<BucketItem> XP_JUICE_BUCKET =
            ITEMS.register("xp_juice_bucket",
                    () -> new BucketItem(XP_JUICE_SOURCE.get(),
                            new Item.Properties()
                                    .setId(itemKey("xp_juice_bucket"))
                                    .stacksTo(1)
                                    .craftRemainder(Items.BUCKET)));

    // ── Tank ──────────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<TankBlock> TANK =
            BLOCKS.register("tank",
                    () -> new TankBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("tank"))
                            .mapColor(MapColor.NONE)
                            .strength(0.3f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .isViewBlocking((s, r, p) -> false)));

    public static final RegistrySupplier<TankBlockItem> TANK_ITEM =
            ITEMS.register("tank",
                    () -> new TankBlockItem(TANK.get(), new Item.Properties()
                            .setId(itemKey("tank"))));

    public static final RegistrySupplier<BlockEntityType<TankBlockEntity>> TANK_BE_TYPE =
            BLOCK_ENTITIES.register("tank",
                    () -> BlockEntityTypePlatform.create(TankBlockEntity::new, TANK.get()));

    public static final RegistrySupplier<MenuType<TankMenu>> TANK_MENU =
            MENUS.register("tank", () -> MenuRegistry.ofExtended(TankMenu::new));

    // ── Experience Syringe ────────────────────────────────────────────────────────

    public static final RegistrySupplier<ExperienceSyringeItem> EXPERIENCE_SYRINGE =
            ITEMS.register("experience_syringe",
                    () -> new ExperienceSyringeItem(new Item.Properties()
                            .setId(itemKey("experience_syringe"))
                            .stacksTo(1)));

    // ── DNA Sample Pack items ─────────────────────────────────────────────────────
    // Mob lists are config-driven (MGRConfig) and read lazily via supplier.

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_COMMON_HOSTILE =
            ITEMS.register("dna_sample_common_hostile",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_common_hostile"))
                            .stacksTo(1),
                            MGRConfig::getCommonHostilePackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_COMMON_PASSIVE =
            ITEMS.register("dna_sample_common_passive",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_common_passive"))
                            .stacksTo(1),
                            MGRConfig::getCommonPassivePackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_AQUATIC =
            ITEMS.register("dna_sample_aquatic",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_aquatic"))
                            .stacksTo(1),
                            MGRConfig::getAquaticPackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_RARE =
            ITEMS.register("dna_sample_rare",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_rare"))
                            .stacksTo(1),
                            MGRConfig::getRareHostilePackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_PASSIVE_RARE =
            ITEMS.register("dna_sample_passive_rare",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_passive_rare"))
                            .stacksTo(1),
                            MGRConfig::getRarePassivePackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_NETHER =
            ITEMS.register("dna_sample_nether",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_nether"))
                            .stacksTo(1),
                            MGRConfig::getNetherPackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_BABY =
            ITEMS.register("dna_sample_baby",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_baby"))
                            .stacksTo(1),
                            MGRConfig::getBabyPackMobs, true));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_WRONG =
            ITEMS.register("dna_sample_wrong",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_wrong"))
                            .stacksTo(1),
                            MGRConfig::getWrongMobsPackMobs));

    // ── Creative tab ──────────────────────────────────────────────────────────────

    public static final RegistrySupplier<CreativeModeTab> CREATIVE_TAB =
            CREATIVE_TABS.register("main", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.mobfarmingsupplies"))
                    .icon(() -> FAN_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ABSORPTION_HOPPER_ITEM.get());
                        output.accept(FAN_ITEM.get());
                        output.accept(FAN_UPGRADE_WIDTH.get());
                        output.accept(FAN_UPGRADE_HEIGHT.get());
                        output.accept(FAN_UPGRADE_DISTANCE.get());
                        output.accept(VECTOR_PLATE_ITEM.get());
                        output.accept(CLONE_O_MATIC_ITEM.get());
                        output.accept(MOB_HARVESTER_ITEM.get());
                        output.accept(HARVESTER_UPGRADE_SHARPNESS.get());
                        output.accept(HARVESTER_UPGRADE_LOOTING.get());
                        output.accept(HARVESTER_UPGRADE_BEHEADING.get());
                        output.accept(DNA_COLLECTOR.get());
                        output.accept(DNA_SAMPLE.get());
                        output.accept(DNA_SAMPLE_COMMON_PASSIVE.get());
                        output.accept(DNA_SAMPLE_COMMON_HOSTILE.get());
                        output.accept(DNA_SAMPLE_PASSIVE_RARE.get());
                        output.accept(DNA_SAMPLE_RARE.get());
                        output.accept(DNA_SAMPLE_AQUATIC.get());
                        output.accept(DNA_SAMPLE_NETHER.get());
                        output.accept(DNA_SAMPLE_BABY.get());
                        output.accept(DNA_SAMPLE_WRONG.get());
                        if (ModCompatRegistration.DNA_BOOSTER_AQUACULTURE != null)
                            output.accept(ModCompatRegistration.DNA_BOOSTER_AQUACULTURE.get());
                        if (ModCompatRegistration.DNA_BOOSTER_AETHER_PASSIVE != null)
                            output.accept(ModCompatRegistration.DNA_BOOSTER_AETHER_PASSIVE.get());
                        if (ModCompatRegistration.DNA_BOOSTER_AETHER_HOSTILE != null)
                            output.accept(ModCompatRegistration.DNA_BOOSTER_AETHER_HOSTILE.get());
                        if (ModCompatRegistration.DNA_BOOSTER_EVILCRAFT != null)
                            output.accept(ModCompatRegistration.DNA_BOOSTER_EVILCRAFT.get());
                        output.accept(WITHER_PROOF_GLASS_ITEM.get());
                        output.accept(MOB_EXCLUSION_GLASS_ITEM.get());
                        output.accept(ENDER_INHIBITOR_ITEM.get());
                        output.accept(TANK_ITEM.get());
                        output.accept(EXPERIENCE_SYRINGE.get());
                        output.accept(XP_JUICE_BUCKET.get());
                    })
                    .build());

    // ── Bootstrap ─────────────────────────────────────────────────────────────────

    /**
     * Subscribes all common DeferredRegisters to the platform's registry events.
     * Called from both the NeoForge and Fabric entry points.
     * NeoForgeOnlyRegistration adds its entries to these registers before calling
     * this method, so all entries (common + NeoForge-only) are submitted together.
     */
    public static void register() {
        FLUIDS.register();
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITIES.register();
        MENUS.register();
        DATA_COMPONENTS.register();
        CREATIVE_TABS.register();
    }
}
