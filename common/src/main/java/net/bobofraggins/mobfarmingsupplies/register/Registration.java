package net.bobofraggins.mobfarmingsupplies.register;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlock;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperMenu;
import net.bobofraggins.mobfarmingsupplies.biosludge.BiosludgeItem;
import net.bobofraggins.mobfarmingsupplies.bridge.BridgeLinkRecipe;
import net.bobofraggins.mobfarmingsupplies.bridge.BridgePairRecipe;
import net.bobofraggins.mobfarmingsupplies.bridge.EinsteinRosenBridgeBlock;
import net.bobofraggins.mobfarmingsupplies.bridge.EinsteinRosenBridgeBlockEntity;
import net.bobofraggins.mobfarmingsupplies.bridge.EinsteinRosenBridgeBlockItem;
import net.bobofraggins.mobfarmingsupplies.bridge.EinsteinRosenBridgeMenu;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticBlock;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticBlockEntity;
import net.bobofraggins.mobfarmingsupplies.cloneomatic.CloneOMaticMenu;
import net.bobofraggins.mobfarmingsupplies.crushing.AnvilCrushingRecipe;
import net.bobofraggins.mobfarmingsupplies.dna.DnaCollectorItem;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleContents;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSamplePackItem;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlock;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorBlockEntity;
import net.bobofraggins.mobfarmingsupplies.enderinhibitor.EnderInhibitorMenu;
import net.bobofraggins.mobfarmingsupplies.experiencesyringe.ExperienceSyringeItem;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlock;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlockEntity;
import net.bobofraggins.mobfarmingsupplies.fan.FanMenu;
import net.bobofraggins.mobfarmingsupplies.fan.FanUpgradeItem;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalBlock;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalBlockEntity;
import net.bobofraggins.mobfarmingsupplies.filterscribingterminal.FilterScribingTerminalMenu;
import net.bobofraggins.mobfarmingsupplies.glamping.magichat.MagicHatItem;
import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentBlock;
import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentBlockEntity;
import net.bobofraggins.mobfarmingsupplies.glamping.present.PresentBlockItem;
import net.bobofraggins.mobfarmingsupplies.guide.GuideBook;
import net.bobofraggins.mobfarmingsupplies.haltingplate.HaltingPlateBlock;
import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeBlock;
import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeBlockEntity;
import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeBlockItem;
import net.bobofraggins.mobfarmingsupplies.headpike.HeadPikeMenu;
import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemFilterData;
import net.bobofraggins.mobfarmingsupplies.itemfilter.ItemFilterItem;
import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantOrnament;
import net.bobofraggins.mobfarmingsupplies.disenchanter.DisenchanterBlock;
import net.bobofraggins.mobfarmingsupplies.disenchanter.DisenchanterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.disenchanter.DisenchanterMenu;
import net.bobofraggins.mobfarmingsupplies.worldgen.DryLandJigsawStructure;
import net.bobofraggins.mobfarmingsupplies.xpanvil.XpAnvilBlock;
import net.bobofraggins.mobfarmingsupplies.xpanvil.XpAnvilBlockEntity;
import net.bobofraggins.mobfarmingsupplies.xpanvil.XpAnvilMenu;
import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantOrnamentBlock;
import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantOrnamentBlockEntity;
import net.bobofraggins.mobfarmingsupplies.lawnornament.GiantPlayerOrnamentItem;
import net.bobofraggins.mobfarmingsupplies.lawnornament.OrnamentSwapProcessor;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlock;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterBlockItem;
import net.bobofraggins.mobfarmingsupplies.logisticsorter.LogisticSorterMenu;
import net.bobofraggins.mobfarmingsupplies.loot.SetBridgeChannelFunction;
import net.bobofraggins.mobfarmingsupplies.loot.SetSyringeLevelsFunction;
import net.bobofraggins.mobfarmingsupplies.mobexclusionglass.MobExclusionGlassBlock;
import net.bobofraggins.mobfarmingsupplies.playerexclusionglass.PlayerExclusionGlassBlock;
import net.bobofraggins.mobfarmingsupplies.mobharvester.HarvesterSword;
import net.bobofraggins.mobfarmingsupplies.mobharvester.HarvesterUpgradeItem;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterBlock;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterMenu;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadBlock;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadBlockEntity;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeadItem;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobHeads;
import net.bobofraggins.mobfarmingsupplies.mobhead.MobWallHeadBlock;
import net.bobofraggins.mobfarmingsupplies.blockbreaker.BlockBreakerBlock;
import net.bobofraggins.mobfarmingsupplies.blockbreaker.BlockBreakerBlockEntity;
import net.bobofraggins.mobfarmingsupplies.blockbreaker.BlockBreakerBlockItem;
import net.bobofraggins.mobfarmingsupplies.blockbreaker.BlockBreakerMenu;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlock;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperBlockItem;
import net.bobofraggins.mobfarmingsupplies.omnihopper.OmniHopperMenu;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketBlock;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketBlockEntity;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketItem;
import net.bobofraggins.mobfarmingsupplies.picnicbasket.PicnicBasketMenu;
import net.bobofraggins.mobfarmingsupplies.shared.sides.OrientedBlockItem;
import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightHookBlock;
import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightHookBlockEntity;
import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLights;
import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightsDyeRecipe;
import net.bobofraggins.mobfarmingsupplies.skulllights.SkullLightsRecipe;
import net.bobofraggins.mobfarmingsupplies.skulllights.StringOfSkullLightsItem;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlock;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockItem;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.bobofraggins.mobfarmingsupplies.tank.TankMenu;
import net.bobofraggins.mobfarmingsupplies.tank.TankTier;
import net.bobofraggins.mobfarmingsupplies.tank.ExperienceTankRecipe;
import net.bobofraggins.mobfarmingsupplies.tank.TankUpgradeRecipe;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlock;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatingRecipe;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorBlock;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorBlockEntity;
import net.bobofraggins.mobfarmingsupplies.incubator.IncubatorMenu;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockEntity;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonBlockItem;
import net.bobofraggins.mobfarmingsupplies.experiencepylon.ExperiencePylonMenu;
import net.bobofraggins.mobfarmingsupplies.togglebutton.ToggleButtonBlock;
import net.bobofraggins.mobfarmingsupplies.toilet.ToiletBlock;
import net.bobofraggins.mobfarmingsupplies.toilet.ToiletBlockEntity;
import net.bobofraggins.mobfarmingsupplies.toilet.ToiletBlockItem;
import net.bobofraggins.mobfarmingsupplies.toilet.ToiletSeatEntity;
import net.bobofraggins.mobfarmingsupplies.vectorplate.VectorPlateBlock;
import net.bobofraggins.mobfarmingsupplies.witherproofglass.WitherProofGlassBlock;
import net.bobofraggins.mobfarmingsupplies.xpjuice.XpJuicePlatformHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

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

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.ENTITY_TYPE);

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.DATA_COMPONENT_TYPE);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.FLUID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.CREATIVE_MODE_TAB);

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.SOUND_EVENT);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.RECIPE_TYPE);

    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.LOOT_FUNCTION_TYPE);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.RECIPE_SERIALIZER);

    public static final DeferredRegister<MapCodec<? extends StructureProcessor>> STRUCTURE_PROCESSORS =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.STRUCTURE_PROCESSOR);

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.PARTICLE_TYPE);

    /** The XP Powered Disenchanter's runes: vanilla's enchanting table runes in XP Juice green. */
    public static final RegistrySupplier<SimpleParticleType> XP_RUNE =
            PARTICLE_TYPES.register("xp_rune", () -> new SimpleParticleType(false) {});

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(MobFarmingSuppliesCommon.MODID, Registries.STRUCTURE_TYPE);

    /** A jigsaw structure that won't start on water (the outhouses and the picnic). */
    public static final RegistrySupplier<StructureType<DryLandJigsawStructure>> DRY_LAND_JIGSAW =
            STRUCTURE_TYPES.register("dry_land_jigsaw", () -> () -> DryLandJigsawStructure.CODEC);

    // ── Recipes ──────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<RecipeType<AnvilCrushingRecipe>> ANVIL_CRUSHING_TYPE =
            RECIPE_TYPES.register("anvil_crushing", () -> new RecipeType<AnvilCrushingRecipe>() {
                @Override
                public String toString() {
                    return MobFarmingSuppliesCommon.MODID + ":anvil_crushing";
                }
            });

    public static final RegistrySupplier<RecipeType<IncubatingRecipe>> INCUBATING_TYPE =
            RECIPE_TYPES.register("incubating", () -> new RecipeType<IncubatingRecipe>() {
                @Override
                public String toString() {
                    return MobFarmingSuppliesCommon.MODID + ":incubating";
                }
            });

    /** What the XP Powered Incubator makes: see {@link IncubatingRecipe}. */
    public static final RegistrySupplier<RecipeSerializer<IncubatingRecipe>> INCUBATING_SERIALIZER =
            RECIPE_SERIALIZERS.register("incubating",
                    () -> new RecipeSerializer<>(IncubatingRecipe.MAP_CODEC, IncubatingRecipe.STREAM_CODEC));

    public static final RegistrySupplier<RecipeSerializer<AnvilCrushingRecipe>> ANVIL_CRUSHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("anvil_crushing",
                    () -> new RecipeSerializer<>(AnvilCrushingRecipe.MAP_CODEC, AnvilCrushingRecipe.STREAM_CODEC));

    /** Shaped recipe that upgrades a tank a tier and keeps its contents. */
    public static final RegistrySupplier<RecipeSerializer<TankUpgradeRecipe>> TANK_UPGRADE_SERIALIZER =
            RECIPE_SERIALIZERS.register("tank_upgrade",
                    () -> new RecipeSerializer<>(TankUpgradeRecipe.MAP_CODEC, TankUpgradeRecipe.STREAM_CODEC));

    /** Shaped recipe that makes a block from a tank (Experience Pylon, Incubator), moving the tank's experience into it. */
    public static final RegistrySupplier<RecipeSerializer<ExperienceTankRecipe>> EXPERIENCE_TANK_SERIALIZER =
            RECIPE_SERIALIZERS.register("experience_tank",
                    () -> new RecipeSerializer<>(ExperienceTankRecipe.MAP_CODEC, ExperienceTankRecipe.STREAM_CODEC));

    public static final RegistrySupplier<RecipeSerializer<SkullLightsRecipe>> SKULL_LIGHTS_SERIALIZER =
            RECIPE_SERIALIZERS.register("skull_lights",
                    () -> new RecipeSerializer<>(SkullLightsRecipe.MAP_CODEC, SkullLightsRecipe.STREAM_CODEC));

    /** Recolours a String of Skull Lights with a dye, keeping its skulls. */
    public static final RegistrySupplier<RecipeSerializer<SkullLightsDyeRecipe>> SKULL_LIGHTS_DYE_SERIALIZER =
            RECIPE_SERIALIZERS.register("skull_lights_dye",
                    () -> new RecipeSerializer<>(SkullLightsDyeRecipe.MAP_CODEC, SkullLightsDyeRecipe.STREAM_CODEC));

    /** Shaped recipe for a linked pair of Einstein-Rosen Bridges on a new random channel. */
    public static final RegistrySupplier<RecipeSerializer<BridgePairRecipe>> BRIDGE_PAIR_SERIALIZER =
            RECIPE_SERIALIZERS.register("bridge_pair",
                    () -> new RecipeSerializer<>(BridgePairRecipe.MAP_CODEC, BridgePairRecipe.STREAM_CODEC));

    /** Shapeless recipe adding another Einstein-Rosen Bridge to an existing bridge's channel. */
    public static final RegistrySupplier<RecipeSerializer<BridgeLinkRecipe>> BRIDGE_LINK_SERIALIZER =
            RECIPE_SERIALIZERS.register("bridge_link",
                    () -> new RecipeSerializer<>(BridgeLinkRecipe.MAP_CODEC, BridgeLinkRecipe.STREAM_CODEC));

    // ── Loot functions (chest loot) ───────────────────────────────────────────────

    public static final RegistrySupplier<MapCodec<SetSyringeLevelsFunction>> SET_SYRINGE_LEVELS =
            LOOT_FUNCTIONS.register("set_syringe_levels", () -> SetSyringeLevelsFunction.CODEC);

    public static final RegistrySupplier<MapCodec<SetBridgeChannelFunction>> SET_BRIDGE_CHANNEL =
            LOOT_FUNCTIONS.register("set_bridge_channel", () -> SetBridgeChannelFunction.CODEC);

    public static final RegistrySupplier<MapCodec<net.bobofraggins.mobfarmingsupplies.loot.SetDnaSampleFunction>> SET_DNA_SAMPLE =
            LOOT_FUNCTIONS.register("set_dna_sample", () -> net.bobofraggins.mobfarmingsupplies.loot.SetDnaSampleFunction.CODEC);

    // ── Sound events ─────────────────────────────────────────────────────────────

    public static final RegistrySupplier<SoundEvent> RED_ALERT_SOUND =
            SOUND_EVENTS.register("red_alert",
                    () -> SoundEvent.createVariableRangeEvent(
                            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "red_alert")));

    public static final RegistrySupplier<SoundEvent> DRAMATIC_SOUND =
            SOUND_EVENTS.register("dramatic",
                    () -> SoundEvent.createVariableRangeEvent(
                            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "dramatic")));

    public static final RegistrySupplier<SoundEvent> RIMSHOT_SOUND =
            SOUND_EVENTS.register("rimshot",
                    () -> SoundEvent.createVariableRangeEvent(
                            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "rimshot")));

    public static final RegistrySupplier<SoundEvent> WILHELM_SOUND =
            SOUND_EVENTS.register("wilhelm",
                    () -> SoundEvent.createVariableRangeEvent(
                            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "wilhelm")));

    public static final RegistrySupplier<SoundEvent> TOILET_FLUSH_SOUND =
            SOUND_EVENTS.register("flush",
                    () -> SoundEvent.createVariableRangeEvent(
                            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "flush")));

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

    /** Present on a Superconducting Magnet while it's switched on. */
    public static final RegistrySupplier<DataComponentType<net.minecraft.util.Unit>> MAGNET_ON =
            DATA_COMPONENTS.register("magnet_on",
                    () -> DataComponentType.<net.minecraft.util.Unit>builder()
                            .persistent(net.minecraft.util.Unit.CODEC)
                            .networkSynchronized(net.minecraft.network.codec.StreamCodec.unit(net.minecraft.util.Unit.INSTANCE))
                            .build());

    public static final RegistrySupplier<DataComponentType<Integer>> EXPERIENCE_SYRINGE_STORED_XP =
            DATA_COMPONENTS.register("experience_syringe_stored_xp",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.intRange(0, ExperienceSyringeItem.CAPACITY))
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    /** Which mob a Mob Head is the head of. */
    public static final RegistrySupplier<DataComponentType<EntityType<?>>> MOB_HEAD_TYPE =
            DATA_COMPONENTS.register("mob_head_type",
                    () -> DataComponentType.<EntityType<?>>builder()
                            .persistent(BuiltInRegistries.ENTITY_TYPE.byNameCodec())
                            .networkSynchronized(ByteBufCodecs.registry(Registries.ENTITY_TYPE))
                            .build());

    /** The Einstein-Rosen Bridge channel a bridge item is on (a random 32-bit number). */
    public static final RegistrySupplier<DataComponentType<Integer>> BRIDGE_CHANNEL =
            DATA_COMPONENTS.register("bridge_channel",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build());

    public static final RegistrySupplier<DataComponentType<TankContents>> TANK_CONTENTS =
            DATA_COMPONENTS.register("tank_contents",
                    () -> DataComponentType.<TankContents>builder()
                            .persistent(TankContents.CODEC)
                            .networkSynchronized(TankContents.STREAM_CODEC)
                            .build());

    public static final RegistrySupplier<DataComponentType<SkullLights>> SKULL_LIGHTS =
            DATA_COMPONENTS.register("skull_lights",
                    () -> DataComponentType.<SkullLights>builder()
                            .persistent(SkullLights.CODEC)
                            .networkSynchronized(SkullLights.STREAM_CODEC)
                            .build());

    /** Where a String of Skull Lights being hung was started. */
    public static final RegistrySupplier<DataComponentType<StringOfSkullLightsItem.Start>> SKULL_LIGHTS_START =
            DATA_COMPONENTS.register("skull_lights_start",
                    () -> DataComponentType.<StringOfSkullLightsItem.Start>builder()
                            .persistent(StringOfSkullLightsItem.Start.CODEC)
                            .networkSynchronized(StringOfSkullLightsItem.Start.STREAM_CODEC)
                            .build());

    public static final RegistrySupplier<DataComponentType<ItemFilterData>> ITEM_FILTER_DATA =
            DATA_COMPONENTS.register("item_filter",
                    () -> DataComponentType.<ItemFilterData>builder()
                            .persistent(ItemFilterData.CODEC)
                            .networkSynchronized(ItemFilterData.STREAM_CODEC)
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

    // ── Magic Hat ────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<MagicHatItem> MAGIC_HAT_ITEM =
            ITEMS.register("magic_hat",
                    () -> new MagicHatItem(new Item.Properties()
                            .setId(itemKey("magic_hat"))
                            .stacksTo(1)
                            .attributes(MagicHatItem.DEFAULT_MODIFIERS)
                            .component(
                                    DataComponents.EQUIPPABLE,
                                    Equippable.builder(EquipmentSlot.HEAD)
                                            .setAsset(ResourceKey.create(
                                                    EquipmentAssets.ROOT_ID,
                                                    Identifier.fromNamespaceAndPath(
                                                            MobFarmingSuppliesCommon.MODID, "magic_hat")))
                                            .build())));

    // ── Present ──────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<PresentBlock> PRESENT =
            BLOCKS.register("present",
                    () -> new PresentBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("present"))
                            .strength(3.0f, 1000.0f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    public static final RegistrySupplier<PresentBlockItem> PRESENT_ITEM =
            ITEMS.register("present",
                    () -> new PresentBlockItem(PRESENT.get(), new Item.Properties()
                            .setId(itemKey("present"))));

    public static final RegistrySupplier<BlockEntityType<PresentBlockEntity>> PRESENT_BE_TYPE =
            BLOCK_ENTITIES.register("present",
                    () -> BlockEntityTypePlatform.create(PresentBlockEntity::new, PRESENT.get()));

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

    public static final RegistrySupplier<HarvesterUpgradeItem> HARVESTER_UPGRADE_FIRE_ASPECT =
            ITEMS.register("harvester_upgrade_fire_aspect",
                    () -> new HarvesterUpgradeItem(new Item.Properties()
                            .setId(itemKey("harvester_upgrade_fire_aspect")),
                            HarvesterUpgradeItem.UpgradeType.FIRE_ASPECT));

    public static final RegistrySupplier<HarvesterUpgradeItem> HARVESTER_UPGRADE_SMITE =
            ITEMS.register("harvester_upgrade_smite",
                    () -> new HarvesterUpgradeItem(new Item.Properties()
                            .setId(itemKey("harvester_upgrade_smite")),
                            HarvesterUpgradeItem.UpgradeType.SMITE));

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
                    () -> new OrientedBlockItem(ABSORPTION_HOPPER.get(), new Item.Properties()
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

    public static final RegistrySupplier<MenuType<EnderInhibitorMenu>> ENDER_INHIBITOR_MENU =
            MENUS.register("ender_inhibitor", () -> MenuRegistry.ofExtended(EnderInhibitorMenu::new));

    public static final RegistrySupplier<BlockEntityType<EnderInhibitorBlockEntity>> ENDER_INHIBITOR_BE_TYPE =
            BLOCK_ENTITIES.register("ender_inhibitor",
                    () -> BlockEntityTypePlatform.create(EnderInhibitorBlockEntity::new, ENDER_INHIBITOR.get()));

    // ── Wither Inhibitor ──────────────────────────────────────────────────────────

    public static final RegistrySupplier<net.bobofraggins.mobfarmingsupplies.witherinhibitor.WitherInhibitorBlock> WITHER_INHIBITOR =
            BLOCKS.register("wither_inhibitor",
                    () -> new net.bobofraggins.mobfarmingsupplies.witherinhibitor.WitherInhibitorBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("wither_inhibitor"))
                            .strength(3.0f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> WITHER_INHIBITOR_ITEM =
            ITEMS.register("wither_inhibitor",
                    () -> new BlockItem(WITHER_INHIBITOR.get(), new Item.Properties()
                            .setId(itemKey("wither_inhibitor"))));

    public static final RegistrySupplier<BlockEntityType<net.bobofraggins.mobfarmingsupplies.witherinhibitor.WitherInhibitorBlockEntity>> WITHER_INHIBITOR_BE_TYPE =
            BLOCK_ENTITIES.register("wither_inhibitor",
                    () -> BlockEntityTypePlatform.create(net.bobofraggins.mobfarmingsupplies.witherinhibitor.WitherInhibitorBlockEntity::new,
                            WITHER_INHIBITOR.get()));

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

    // ── Halting Plate ─────────────────────────────────────────────────────────────

    public static final RegistrySupplier<HaltingPlateBlock> HALTING_PLATE =
            BLOCKS.register("halting_plate",
                    () -> new HaltingPlateBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("halting_plate"))
                            .strength(2.0f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.METAL)
                            .isValidSpawn((state, level, pos, entityType) -> true)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> HALTING_PLATE_ITEM =
            ITEMS.register("halting_plate",
                    () -> new BlockItem(HALTING_PLATE.get(), new Item.Properties()
                            .setId(itemKey("halting_plate"))));

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
                            .isSuffocating((s, r, p) -> false)
                            .isViewBlocking((s, r, p) -> false)));

    public static final RegistrySupplier<BlockItem> MOB_EXCLUSION_GLASS_ITEM =
            ITEMS.register("mob_exclusion_glass",
                    () -> new BlockItem(MOB_EXCLUSION_GLASS.get(), new Item.Properties()
                            .setId(itemKey("mob_exclusion_glass"))));

    // ── Player Exclusion Glass ────────────────────────────────────────────────────

    public static final RegistrySupplier<PlayerExclusionGlassBlock> PLAYER_EXCLUSION_GLASS =
            BLOCKS.register("player_exclusion_glass",
                    () -> new PlayerExclusionGlassBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("player_exclusion_glass"))
                            .strength(1.5f)
                            .explosionResistance(Float.MAX_VALUE)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .isSuffocating((s, r, p) -> false)
                            .isViewBlocking((s, r, p) -> false)));

    public static final RegistrySupplier<BlockItem> PLAYER_EXCLUSION_GLASS_ITEM =
            ITEMS.register("player_exclusion_glass",
                    () -> new BlockItem(PLAYER_EXCLUSION_GLASS.get(), new Item.Properties()
                            .setId(itemKey("player_exclusion_glass"))));

    // ── Toggle Buttons ────────────────────────────────────────────────────────────

    public static final RegistrySupplier<ToggleButtonBlock> RED_ALERT_BUTTON =
            BLOCKS.register("red_alert_button",
                    () -> new ToggleButtonBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("red_alert_button"))
                            .strength(0.5f)
                            .sound(SoundType.STONE)
                            .noOcclusion(),
                            RED_ALERT_SOUND));

    public static final RegistrySupplier<BlockItem> RED_ALERT_BUTTON_ITEM =
            ITEMS.register("red_alert_button",
                    () -> new BlockItem(RED_ALERT_BUTTON.get(), new Item.Properties()
                            .setId(itemKey("red_alert_button"))));

    public static final RegistrySupplier<ToggleButtonBlock> DRAMATIC_BUTTON =
            BLOCKS.register("dramatic_button",
                    () -> new ToggleButtonBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("dramatic_button"))
                            .strength(0.5f)
                            .sound(SoundType.STONE)
                            .noOcclusion(),
                            DRAMATIC_SOUND));

    public static final RegistrySupplier<BlockItem> DRAMATIC_BUTTON_ITEM =
            ITEMS.register("dramatic_button",
                    () -> new BlockItem(DRAMATIC_BUTTON.get(), new Item.Properties()
                            .setId(itemKey("dramatic_button"))));

    public static final RegistrySupplier<ToggleButtonBlock> RIMSHOT_BUTTON =
            BLOCKS.register("rimshot_button",
                    () -> new ToggleButtonBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("rimshot_button"))
                            .strength(0.5f)
                            .sound(SoundType.STONE)
                            .noOcclusion(),
                            RIMSHOT_SOUND));

    public static final RegistrySupplier<BlockItem> RIMSHOT_BUTTON_ITEM =
            ITEMS.register("rimshot_button",
                    () -> new BlockItem(RIMSHOT_BUTTON.get(), new Item.Properties()
                            .setId(itemKey("rimshot_button"))));

    public static final RegistrySupplier<ToggleButtonBlock> WILHELM_BUTTON =
            BLOCKS.register("wilhelm_button",
                    () -> new ToggleButtonBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("wilhelm_button"))
                            .strength(0.5f)
                            .sound(SoundType.STONE)
                            .noOcclusion(),
                            WILHELM_SOUND));

    public static final RegistrySupplier<BlockItem> WILHELM_BUTTON_ITEM =
            ITEMS.register("wilhelm_button",
                    () -> new BlockItem(WILHELM_BUTTON.get(), new Item.Properties()
                            .setId(itemKey("wilhelm_button"))));

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

    public static final RegistrySupplier<TankBlock> TANK = registerTank("tank", TankTier.BASIC);
    public static final RegistrySupplier<TankBlock> GOLD_TANK = registerTank("gold_tank", TankTier.GOLD);
    public static final RegistrySupplier<TankBlock> DIAMOND_TANK = registerTank("diamond_tank", TankTier.DIAMOND);
    public static final RegistrySupplier<TankBlock> EMERALD_TANK = registerTank("emerald_tank", TankTier.EMERALD);

    public static final RegistrySupplier<TankBlockItem> TANK_ITEM = registerTankItem("tank", TANK);
    public static final RegistrySupplier<TankBlockItem> GOLD_TANK_ITEM = registerTankItem("gold_tank", GOLD_TANK);
    public static final RegistrySupplier<TankBlockItem> DIAMOND_TANK_ITEM = registerTankItem("diamond_tank", DIAMOND_TANK);
    public static final RegistrySupplier<TankBlockItem> EMERALD_TANK_ITEM = registerTankItem("emerald_tank", EMERALD_TANK);

    /** One block entity type for every tier: the capacity comes from the block (see {@link TankTier}). */
    public static final RegistrySupplier<BlockEntityType<TankBlockEntity>> TANK_BE_TYPE =
            BLOCK_ENTITIES.register("tank",
                    () -> BlockEntityTypePlatform.create(TankBlockEntity::new,
                            TANK.get(), GOLD_TANK.get(), DIAMOND_TANK.get(), EMERALD_TANK.get()));

    /** Every tier is the same glass tank; only its capacity and texture differ. */
    private static RegistrySupplier<TankBlock> registerTank(String name, TankTier tier) {
        return BLOCKS.register(name,
                () -> new TankBlock(tier, BlockBehaviour.Properties.of()
                        .setId(blockKey(name))
                        .mapColor(MapColor.NONE)
                        .strength(0.3f)
                        .explosionResistance(Float.MAX_VALUE)
                        .sound(SoundType.GLASS)
                        .noOcclusion()
                        .isViewBlocking((s, r, p) -> false)));
    }

    private static RegistrySupplier<TankBlockItem> registerTankItem(String name, RegistrySupplier<TankBlock> block) {
        return ITEMS.register(name,
                () -> new TankBlockItem(block.get(), new Item.Properties()
                        .setId(itemKey(name))));
    }

    public static final RegistrySupplier<MenuType<TankMenu>> TANK_MENU =
            MENUS.register("tank", () -> MenuRegistry.ofExtended(TankMenu::new));

    // ── XP Powered Disenchanter ───────────────────────────────────────────────────

    public static final RegistrySupplier<DisenchanterBlock> DISENCHANTER =
            BLOCKS.register("disenchanter",
                    () -> new DisenchanterBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("disenchanter"))
                            .mapColor(MapColor.COLOR_RED)
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> 7)
                            .strength(5.0f, 1200.0f)));

    public static final RegistrySupplier<BlockItem> DISENCHANTER_ITEM =
            ITEMS.register("disenchanter",
                    () -> new BlockItem(DISENCHANTER.get(), new Item.Properties().setId(itemKey("disenchanter"))));

    public static final RegistrySupplier<BlockEntityType<DisenchanterBlockEntity>> DISENCHANTER_BE_TYPE =
            BLOCK_ENTITIES.register("disenchanter",
                    () -> BlockEntityTypePlatform.create(DisenchanterBlockEntity::new, DISENCHANTER.get()));

    public static final RegistrySupplier<MenuType<DisenchanterMenu>> DISENCHANTER_MENU =
            MENUS.register("disenchanter", () -> MenuRegistry.ofExtended(DisenchanterMenu::new));

    // ── XP Powered Anvil ──────────────────────────────────────────────────────────

    public static final RegistrySupplier<XpAnvilBlock> XP_ANVIL =
            BLOCKS.register("anvil",
                    () -> new XpAnvilBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("anvil"))
                            .mapColor(MapColor.METAL)
                            .strength(5.0f, 1200.0f)
                            .sound(SoundType.ANVIL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> XP_ANVIL_ITEM =
            ITEMS.register("anvil",
                    () -> new BlockItem(XP_ANVIL.get(), new Item.Properties().setId(itemKey("anvil"))));

    public static final RegistrySupplier<BlockEntityType<XpAnvilBlockEntity>> XP_ANVIL_BE_TYPE =
            BLOCK_ENTITIES.register("anvil",
                    () -> BlockEntityTypePlatform.create(XpAnvilBlockEntity::new, XP_ANVIL.get()));

    public static final RegistrySupplier<MenuType<XpAnvilMenu>> XP_ANVIL_MENU =
            MENUS.register("anvil", () -> MenuRegistry.ofExtended(XpAnvilMenu::new));

    // ── Incubator ─────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<IncubatorBlock> INCUBATOR =
            BLOCKS.register("incubator",
                    () -> new IncubatorBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("incubator"))
                            .mapColor(MapColor.QUARTZ)
                            .strength(0.8f)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .isViewBlocking((s, r, p) -> false)));

    public static final RegistrySupplier<BlockItem> INCUBATOR_ITEM =
            ITEMS.register("incubator",
                    () -> new BlockItem(INCUBATOR.get(), new Item.Properties().setId(itemKey("incubator"))));

    public static final RegistrySupplier<BlockEntityType<IncubatorBlockEntity>> INCUBATOR_BE_TYPE =
            BLOCK_ENTITIES.register("incubator",
                    () -> BlockEntityTypePlatform.create(IncubatorBlockEntity::new, INCUBATOR.get()));

    public static final RegistrySupplier<MenuType<IncubatorMenu>> INCUBATOR_MENU =
            MENUS.register("incubator", () -> MenuRegistry.ofExtended(IncubatorMenu::new));

    // ── Experience Pylon ──────────────────────────────────────────────────────────

    public static final RegistrySupplier<ExperiencePylonBlock> EXPERIENCE_PYLON =
            BLOCKS.register("experience_pylon",
                    () -> new ExperiencePylonBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("experience_pylon"))
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(0.3f, 1200.0f) // a tank's mining time, more blast resistance
                            .lightLevel(state -> 15)
                            .sound(SoundType.STONE)
                            .noOcclusion()));

    public static final RegistrySupplier<ExperiencePylonBlockItem> EXPERIENCE_PYLON_ITEM =
            ITEMS.register("experience_pylon",
                    () -> new ExperiencePylonBlockItem(EXPERIENCE_PYLON.get(), new Item.Properties()
                            .setId(itemKey("experience_pylon"))));

    public static final RegistrySupplier<BlockEntityType<ExperiencePylonBlockEntity>> EXPERIENCE_PYLON_BE_TYPE =
            BLOCK_ENTITIES.register("experience_pylon",
                    () -> BlockEntityTypePlatform.create(ExperiencePylonBlockEntity::new, EXPERIENCE_PYLON.get()));

    public static final RegistrySupplier<MenuType<ExperiencePylonMenu>> EXPERIENCE_PYLON_MENU =
            MENUS.register("experience_pylon", () -> MenuRegistry.ofExtended(ExperiencePylonMenu::new));

    // ── Experience Syringe ────────────────────────────────────────────────────────

    public static final RegistrySupplier<net.bobofraggins.mobfarmingsupplies.magnet.SuperconductingMagnetItem>
            SUPERCONDUCTING_MAGNET = ITEMS.register("superconducting_magnet",
                    () -> new net.bobofraggins.mobfarmingsupplies.magnet.SuperconductingMagnetItem(new Item.Properties()
                            .setId(itemKey("superconducting_magnet"))
                            .stacksTo(1)));

    public static final RegistrySupplier<ExperienceSyringeItem> EXPERIENCE_SYRINGE =
            ITEMS.register("experience_syringe",
                    () -> new ExperienceSyringeItem(new Item.Properties()
                            .setId(itemKey("experience_syringe"))
                            .stacksTo(1)));

    // ── DNA Sample Pack items ─────────────────────────────────────────────────────
    // Mob lists are config-driven (MFSConfig) and read lazily via supplier. The Common packs
    // also add the natural spawns (of the given category) of the Clone-O-Matic's biome.

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_COMMON_HOSTILE =
            ITEMS.register("dna_sample_common_hostile",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_common_hostile"))
                            .stacksTo(1),
                            MFSConfig::getCommonHostilePackMobs, MobCategory.MONSTER));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_COMMON_PASSIVE =
            ITEMS.register("dna_sample_common_passive",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_common_passive"))
                            .stacksTo(1),
                            MFSConfig::getCommonPassivePackMobs, MobCategory.CREATURE));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_AQUATIC =
            ITEMS.register("dna_sample_aquatic",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_aquatic"))
                            .stacksTo(1),
                            MFSConfig::getAquaticPackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_RARE =
            ITEMS.register("dna_sample_rare",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_rare"))
                            .stacksTo(1),
                            MFSConfig::getRareHostilePackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_PASSIVE_RARE =
            ITEMS.register("dna_sample_passive_rare",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_passive_rare"))
                            .stacksTo(1),
                            MFSConfig::getRarePassivePackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_NETHER =
            ITEMS.register("dna_sample_nether",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_nether"))
                            .stacksTo(1),
                            MFSConfig::getNetherPackMobs));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_BABY =
            ITEMS.register("dna_sample_baby",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_baby"))
                            .stacksTo(1),
                            MFSConfig::getBabyPackMobs, true));

    public static final RegistrySupplier<DnaSamplePackItem> DNA_SAMPLE_WRONG =
            ITEMS.register("dna_sample_wrong",
                    () -> new DnaSamplePackItem(new Item.Properties()
                            .setId(itemKey("dna_sample_wrong"))
                            .stacksTo(1),
                            MFSConfig::getWrongMobsPackMobs));

    // ── Picnic Basket ─────────────────────────────────────────────────────────────

    public static final RegistrySupplier<PicnicBasketBlock> PICNIC_BASKET =
            BLOCKS.register("picnic_basket",
                    () -> new PicnicBasketBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("picnic_basket"))
                            .strength(2.5f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    public static final RegistrySupplier<PicnicBasketItem> PICNIC_BASKET_ITEM =
            ITEMS.register("picnic_basket",
                    () -> new PicnicBasketItem(PICNIC_BASKET.get(), new Item.Properties()
                            .setId(itemKey("picnic_basket"))
                            .stacksTo(1)));

    public static final RegistrySupplier<MenuType<PicnicBasketMenu>> PICNIC_BASKET_MENU =
            MENUS.register("picnic_basket", () -> MenuRegistry.ofExtended(PicnicBasketMenu::new));

    public static final RegistrySupplier<BlockEntityType<PicnicBasketBlockEntity>> PICNIC_BASKET_BE_TYPE =
            BLOCK_ENTITIES.register("picnic_basket",
                    () -> BlockEntityTypePlatform.create(PicnicBasketBlockEntity::new, PICNIC_BASKET.get()));

    // ── S'mores ───────────────────────────────────────────────────────────────────

    private static final FoodProperties SNACK_FOOD =
            new FoodProperties.Builder().nutrition(2).saturationModifier(0.25f).build();

    private static final FoodProperties SMORE_FOOD =
            new FoodProperties.Builder().nutrition(10).saturationModifier(0.25f).build();

    public static final RegistrySupplier<Item> GRAHAM_CRACKER =
            ITEMS.register("graham_cracker",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("graham_cracker"))
                            .food(SNACK_FOOD)));

    public static final RegistrySupplier<Item> CHOCOLATE_BAR =
            ITEMS.register("chocolate_bar",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("chocolate_bar"))
                            .food(SNACK_FOOD)));

    public static final RegistrySupplier<Item> MARSHMALLOW =
            ITEMS.register("marshmallow",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("marshmallow"))
                            .food(SNACK_FOOD)));

    public static final RegistrySupplier<Item> TOASTED_MARSHMALLOW =
            ITEMS.register("toasted_marshmallow",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("toasted_marshmallow"))
                            .food(SNACK_FOOD)));

    public static final RegistrySupplier<Item> SMORE =
            ITEMS.register("smore",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("smore"))
                            .food(SMORE_FOOD)));

    // ── Filter Scribing Terminal ──────────────────────────────────────────────────

    public static final RegistrySupplier<FilterScribingTerminalBlock> FILTER_SCRIBING_TERMINAL =
            BLOCKS.register("filter_scribing_terminal",
                    () -> new FilterScribingTerminalBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("filter_scribing_terminal"))
                            .strength(2.5f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> FILTER_SCRIBING_TERMINAL_ITEM =
            ITEMS.register("filter_scribing_terminal",
                    () -> new BlockItem(FILTER_SCRIBING_TERMINAL.get(), new Item.Properties()
                            .setId(itemKey("filter_scribing_terminal"))));

    public static final RegistrySupplier<BlockEntityType<FilterScribingTerminalBlockEntity>> FILTER_SCRIBING_TERMINAL_BE_TYPE =
            BLOCK_ENTITIES.register("filter_scribing_terminal",
                    () -> BlockEntityTypePlatform.create(
                            FilterScribingTerminalBlockEntity::new, FILTER_SCRIBING_TERMINAL.get()));

    public static final RegistrySupplier<MenuType<FilterScribingTerminalMenu>> FILTER_SCRIBING_TERMINAL_MENU =
            MENUS.register("filter_scribing_terminal",
                    () -> MenuRegistry.ofExtended(FilterScribingTerminalMenu::new));

    // ── Logistic Sorter ───────────────────────────────────────────────────────────

    public static final RegistrySupplier<LogisticSorterBlock> LOGISTIC_SORTER =
            BLOCKS.register("logistic_sorter",
                    () -> new LogisticSorterBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("logistic_sorter"))
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> LOGISTIC_SORTER_ITEM =
            ITEMS.register("logistic_sorter",
                    () -> new LogisticSorterBlockItem(LOGISTIC_SORTER.get(), new Item.Properties()
                            .setId(itemKey("logistic_sorter"))));

    public static final RegistrySupplier<BlockEntityType<LogisticSorterBlockEntity>> LOGISTIC_SORTER_BE_TYPE =
            BLOCK_ENTITIES.register("logistic_sorter",
                    () -> BlockEntityTypePlatform.create(LogisticSorterBlockEntity::new, LOGISTIC_SORTER.get()));

    public static final RegistrySupplier<MenuType<LogisticSorterMenu>> LOGISTIC_SORTER_MENU =
            MENUS.register("logistic_sorter", () -> MenuRegistry.ofExtended(LogisticSorterMenu::new));

    // ── Block Breaker ────────────────────────────────────────────────────────────

    public static final RegistrySupplier<BlockBreakerBlock> BLOCK_BREAKER =
            BLOCKS.register("block_breaker",
                    () -> new BlockBreakerBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("block_breaker"))
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    /** Remembers how it was placed, for its side grid. */
    public static final RegistrySupplier<BlockItem> BLOCK_BREAKER_ITEM =
            ITEMS.register("block_breaker",
                    () -> new BlockBreakerBlockItem(BLOCK_BREAKER.get(), new Item.Properties()
                            .setId(itemKey("block_breaker"))));

    public static final RegistrySupplier<BlockEntityType<BlockBreakerBlockEntity>> BLOCK_BREAKER_BE_TYPE =
            BLOCK_ENTITIES.register("block_breaker",
                    () -> BlockEntityTypePlatform.create(BlockBreakerBlockEntity::new, BLOCK_BREAKER.get()));

    // ── Omnidirectional Hopper ───────────────────────────────────────────────────

    public static final RegistrySupplier<OmniHopperBlock> OMNI_HOPPER =
            BLOCKS.register("omnidirectional_hopper",
                    () -> new OmniHopperBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("omnidirectional_hopper"))
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> OMNI_HOPPER_ITEM =
            ITEMS.register("omnidirectional_hopper",
                    () -> new OmniHopperBlockItem(OMNI_HOPPER.get(), new Item.Properties()
                            .setId(itemKey("omnidirectional_hopper"))));

    public static final RegistrySupplier<BlockEntityType<OmniHopperBlockEntity>> OMNI_HOPPER_BE_TYPE =
            BLOCK_ENTITIES.register("omnidirectional_hopper",
                    () -> BlockEntityTypePlatform.create(OmniHopperBlockEntity::new, OMNI_HOPPER.get()));

    public static final RegistrySupplier<MenuType<BlockBreakerMenu>> BLOCK_BREAKER_MENU =
            MENUS.register("block_breaker", () -> MenuRegistry.ofExtended(BlockBreakerMenu::new));

    public static final RegistrySupplier<MenuType<OmniHopperMenu>> OMNI_HOPPER_MENU =
            MENUS.register("omnidirectional_hopper", () -> MenuRegistry.ofExtended(OmniHopperMenu::new));

    // ── Einstein-Rosen Bridge ────────────────────────────────────────────────────

    public static final RegistrySupplier<EinsteinRosenBridgeBlock> EINSTEIN_ROSEN_BRIDGE =
            BLOCKS.register("einstein_rosen_bridge",
                    () -> new EinsteinRosenBridgeBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("einstein_rosen_bridge"))
                            .lightLevel(state -> 11) // like a nether portal
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> EINSTEIN_ROSEN_BRIDGE_ITEM =
            ITEMS.register("einstein_rosen_bridge",
                    () -> new EinsteinRosenBridgeBlockItem(EINSTEIN_ROSEN_BRIDGE.get(), new Item.Properties()
                            .setId(itemKey("einstein_rosen_bridge"))));

    public static final RegistrySupplier<BlockEntityType<EinsteinRosenBridgeBlockEntity>> BRIDGE_BE_TYPE =
            BLOCK_ENTITIES.register("einstein_rosen_bridge",
                    () -> BlockEntityTypePlatform.create(EinsteinRosenBridgeBlockEntity::new, EINSTEIN_ROSEN_BRIDGE.get()));

    public static final RegistrySupplier<MenuType<EinsteinRosenBridgeMenu>> BRIDGE_MENU =
            MENUS.register("einstein_rosen_bridge", () -> MenuRegistry.ofExtended(EinsteinRosenBridgeMenu::new));

    // ── Mob Heads ─────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<MobHeadBlock> MOB_HEAD =
            BLOCKS.register("mob_head",
                    () -> new MobHeadBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("mob_head"))
                            .instrument(NoteBlockInstrument.CUSTOM_HEAD)
                            .strength(1.0f)
                            .pushReaction(PushReaction.DESTROY)));

    public static final RegistrySupplier<MobWallHeadBlock> MOB_WALL_HEAD =
            BLOCKS.register("mob_wall_head",
                    () -> new MobWallHeadBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("mob_wall_head"))
                            .instrument(NoteBlockInstrument.CUSTOM_HEAD)
                            .strength(1.0f)
                            .pushReaction(PushReaction.DESTROY)
                            .overrideLootTable(MOB_HEAD.get().getLootTable())
                            .overrideDescription(MOB_HEAD.get().getDescriptionId())));

    public static final RegistrySupplier<MobHeadItem> MOB_HEAD_ITEM =
            ITEMS.register("mob_head",
                    () -> new MobHeadItem(MOB_HEAD.get(), MOB_WALL_HEAD.get(), new Item.Properties()
                            .setId(itemKey("mob_head"))
                            .rarity(Rarity.UNCOMMON)
                            .equippableUnswappable(EquipmentSlot.HEAD)
                            // Worn, a head protects like an Iron Helmet and takes helmet
                            // enchantments, but has no durability so heads still stack.
                            .attributes(ArmorMaterials.IRON.createAttributes(ArmorType.HELMET))
                            .enchantable(ArmorMaterials.IRON.enchantmentValue())));

    public static final RegistrySupplier<BlockEntityType<MobHeadBlockEntity>> MOB_HEAD_BE_TYPE =
            BLOCK_ENTITIES.register("mob_head",
                    () -> BlockEntityTypePlatform.create(MobHeadBlockEntity::new, MOB_HEAD.get(), MOB_WALL_HEAD.get()));

    // ── Toilet ────────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<ToiletBlock> TOILET =
            BLOCKS.register("toilet",
                    () -> new ToiletBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("toilet"))
                            .strength(0.8f)
                            .sound(SoundType.CALCITE)
                            .noOcclusion()));

    public static final RegistrySupplier<BlockItem> TOILET_ITEM =
            ITEMS.register("toilet",
                    () -> new ToiletBlockItem(TOILET.get(), new Item.Properties()
                            .setId(itemKey("toilet"))));

    public static final RegistrySupplier<BlockEntityType<ToiletBlockEntity>> TOILET_BE_TYPE =
            BLOCK_ENTITIES.register("toilet",
                    () -> BlockEntityTypePlatform.create(ToiletBlockEntity::new, TOILET.get()));

    // ── Giant Lawn Ornaments ─────────────────────────────────────────────────────

    public static final RegistrySupplier<GiantOrnamentBlock> GIANT_SKELETON_ORNAMENT =
            registerGiantOrnament(GiantOrnament.SKELETON, SoundType.BONE_BLOCK);
    public static final RegistrySupplier<GiantOrnamentBlock> GIANT_HEROBRINE_ORNAMENT =
            registerGiantOrnament(GiantOrnament.HEROBRINE, SoundType.STONE);
    public static final RegistrySupplier<GiantOrnamentBlock> GIANT_PLAYER_ORNAMENT =
            registerGiantOrnament(GiantOrnament.PLAYER, SoundType.STONE);

    public static final RegistrySupplier<BlockItem> GIANT_SKELETON_ORNAMENT_ITEM =
            registerGiantOrnamentItem(GiantOrnament.SKELETON, GIANT_SKELETON_ORNAMENT);
    public static final RegistrySupplier<BlockItem> GIANT_HEROBRINE_ORNAMENT_ITEM =
            registerGiantOrnamentItem(GiantOrnament.HEROBRINE, GIANT_HEROBRINE_ORNAMENT);
    /** Named after, and wearing the skin of, its owner: whoever first places it. */
    public static final RegistrySupplier<GiantPlayerOrnamentItem> GIANT_PLAYER_ORNAMENT_ITEM =
            ITEMS.register(GiantOrnament.PLAYER.id(), () -> new GiantPlayerOrnamentItem(GIANT_PLAYER_ORNAMENT.get(),
                    new Item.Properties().setId(itemKey(GiantOrnament.PLAYER.id()))));

    /**
     * One block entity type for every giant ornament. It keeps the skeleton's id, which worlds and
     * the village yard template have already saved.
     */
    public static final RegistrySupplier<BlockEntityType<GiantOrnamentBlockEntity>> GIANT_ORNAMENT_BE_TYPE =
            BLOCK_ENTITIES.register("giant_skeleton_lawn_ornament",
                    () -> BlockEntityTypePlatform.create(GiantOrnamentBlockEntity::new,
                            GIANT_SKELETON_ORNAMENT.get(), GIANT_HEROBRINE_ORNAMENT.get(),
                            GIANT_PLAYER_ORNAMENT.get()));

    private static RegistrySupplier<GiantOrnamentBlock> registerGiantOrnament(GiantOrnament ornament, SoundType sound) {
        return BLOCKS.register(ornament.id(),
                () -> new GiantOrnamentBlock(ornament, BlockBehaviour.Properties.of()
                        .setId(blockKey(ornament.id()))
                        .strength(1.5f)
                        .sound(sound)
                        .noOcclusion()));
    }

    private static RegistrySupplier<BlockItem> registerGiantOrnamentItem(
            GiantOrnament ornament, RegistrySupplier<GiantOrnamentBlock> block) {
        return ITEMS.register(ornament.id(),
                () -> new BlockItem(block.get(), new Item.Properties().setId(itemKey(ornament.id()))));
    }

    /** Swaps one block of a structure for another as it's placed (the village yards' Herobrine). */
    public static final RegistrySupplier<MapCodec<OrnamentSwapProcessor>> ORNAMENT_SWAP_PROCESSOR =
            STRUCTURE_PROCESSORS.register("ornament_swap", () -> OrnamentSwapProcessor.CODEC);

    // ── Head Pike ────────────────────────────────────────────────────────────────

    public static final RegistrySupplier<HeadPikeBlock> HEAD_PIKE =
            BLOCKS.register("head_pike",
                    () -> new HeadPikeBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("head_pike"))
                            .strength(1.0f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .pushReaction(PushReaction.DESTROY)));

    public static final RegistrySupplier<HeadPikeBlockItem> HEAD_PIKE_ITEM =
            ITEMS.register("head_pike",
                    () -> new HeadPikeBlockItem(HEAD_PIKE.get(), new Item.Properties()
                            .setId(itemKey("head_pike"))));

    public static final RegistrySupplier<BlockEntityType<HeadPikeBlockEntity>> HEAD_PIKE_BE_TYPE =
            BLOCK_ENTITIES.register("head_pike",
                    () -> BlockEntityTypePlatform.create(HeadPikeBlockEntity::new, HEAD_PIKE.get()));

    public static final RegistrySupplier<MenuType<HeadPikeMenu>> HEAD_PIKE_MENU =
            MENUS.register("head_pike", () -> new MenuType<>(HeadPikeMenu::new, FeatureFlags.VANILLA_SET));

    // ── String of Skull Lights ───────────────────────────────────────────────────

    public static final RegistrySupplier<SkullLightHookBlock> SKULL_LIGHT_HOOK =
            BLOCKS.register("skull_light_hook",
                    () -> new SkullLightHookBlock(BlockBehaviour.Properties.of()
                            .setId(blockKey("skull_light_hook"))
                            .strength(0.3f)
                            .sound(SoundType.CHAIN)
                            .noCollision()
                            .noOcclusion()
                            .pushReaction(PushReaction.DESTROY)));

    public static final RegistrySupplier<BlockEntityType<SkullLightHookBlockEntity>> SKULL_LIGHT_HOOK_BE_TYPE =
            BLOCK_ENTITIES.register("skull_light_hook",
                    () -> BlockEntityTypePlatform.create(SkullLightHookBlockEntity::new, SKULL_LIGHT_HOOK.get()));

    public static final RegistrySupplier<StringOfSkullLightsItem> STRING_OF_SKULL_LIGHTS =
            ITEMS.register("string_of_skull_lights",
                    () -> new StringOfSkullLightsItem(new Item.Properties()
                            .setId(itemKey("string_of_skull_lights"))
                            .stacksTo(16)));

    public static final RegistrySupplier<EntityType<ToiletSeatEntity>> TOILET_SEAT =
            ENTITY_TYPES.register("toilet_seat",
                    () -> EntityType.Builder.<ToiletSeatEntity>of(ToiletSeatEntity::new, MobCategory.MISC)
                            .sized(0.0f, 0.0f)
                            .noSummon()
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "toilet_seat"))));

    // ── Crafting materials ────────────────────────────────────────────────────────

    public static final RegistrySupplier<Item> SILICON =
            ITEMS.register("silicon",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("silicon"))));

    public static final RegistrySupplier<Item> SILICON_CLUMP =
            ITEMS.register("silicon_clump",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("silicon_clump"))));

    public static final RegistrySupplier<BiosludgeItem> BIOSLUDGE =
            ITEMS.register("biosludge",
                    () -> new BiosludgeItem(new Item.Properties()
                            .setId(itemKey("biosludge"))));

    /** Smelted from Biosludge. */
    public static final RegistrySupplier<Item> BIOPLASTIC =
            ITEMS.register("bioplastic",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("bioplastic"))));

    public static final RegistrySupplier<Item> SILICON_WAFER =
            ITEMS.register("silicon_wafer",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("silicon_wafer"))));

    public static final RegistrySupplier<Item> BLANK_FILTER =
            ITEMS.register("blank_filter",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("blank_filter"))));

    public static final RegistrySupplier<Item> BLANK_UPGRADE =
            ITEMS.register("blank_upgrade",
                    () -> new Item(new Item.Properties()
                            .setId(itemKey("blank_upgrade"))));

    public static final RegistrySupplier<ItemFilterItem> ITEM_FILTER =
            ITEMS.register("item_filter",
                    () -> new ItemFilterItem(new Item.Properties()
                            .setId(itemKey("item_filter"))));

    // ── Creative tab ──────────────────────────────────────────────────────────────

    public static final RegistrySupplier<CreativeModeTab> CREATIVE_TAB =
            CREATIVE_TABS.register("main", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.mobfarmingsupplies"))
                    .icon(() -> FAN_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        // The guide comes first when Modonomicon is installed to show it.
                        ItemStack guide = GuideBook.stack(params.holders());
                        if (!guide.isEmpty()) output.accept(guide);
                        output.accept(ABSORPTION_HOPPER_ITEM.get());
                        output.accept(OMNI_HOPPER_ITEM.get());
                        output.accept(EINSTEIN_ROSEN_BRIDGE_ITEM.get());
                        output.accept(LOGISTIC_SORTER_ITEM.get());
                        output.accept(BLOCK_BREAKER_ITEM.get());
                        output.accept(SUPERCONDUCTING_MAGNET.get());
                        output.accept(SILICON_CLUMP.get());
                        output.accept(SILICON.get());
                        output.accept(SILICON_WAFER.get());
                        output.accept(BIOSLUDGE.get());
                        output.accept(BIOPLASTIC.get());
                        output.accept(BLANK_UPGRADE.get());
                        output.accept(FILTER_SCRIBING_TERMINAL_ITEM.get());
                        output.accept(BLANK_FILTER.get());
                        output.accept(ITEM_FILTER.get());
                        output.accept(VECTOR_PLATE_ITEM.get());
                        output.accept(HALTING_PLATE_ITEM.get());
                        output.accept(TOILET_ITEM.get());
                        output.accept(FAN_ITEM.get());
                        output.accept(FAN_UPGRADE_WIDTH.get());
                        output.accept(FAN_UPGRADE_HEIGHT.get());
                        output.accept(FAN_UPGRADE_DISTANCE.get());
                        output.accept(MOB_HARVESTER_ITEM.get());
                        output.accept(HARVESTER_UPGRADE_SHARPNESS.get());
                        output.accept(HARVESTER_UPGRADE_LOOTING.get());
                        output.accept(HARVESTER_UPGRADE_BEHEADING.get());
                        output.accept(HARVESTER_UPGRADE_FIRE_ASPECT.get());
                        output.accept(HARVESTER_UPGRADE_SMITE.get());
                        output.accept(CLONE_O_MATIC_ITEM.get());
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
                        output.accept(WITHER_PROOF_GLASS_ITEM.get());
                        output.accept(MOB_EXCLUSION_GLASS_ITEM.get());
                        output.accept(PLAYER_EXCLUSION_GLASS_ITEM.get());
                        output.accept(EXPERIENCE_SYRINGE.get());
                        output.accept(XP_JUICE_BUCKET.get());
                        output.accept(TANK_ITEM.get());
                        output.accept(GOLD_TANK_ITEM.get());
                        output.accept(DIAMOND_TANK_ITEM.get());
                        output.accept(EMERALD_TANK_ITEM.get());
                        output.accept(EXPERIENCE_PYLON_ITEM.get());
                        output.accept(INCUBATOR_ITEM.get());
                        output.accept(XP_ANVIL_ITEM.get());
                        output.accept(DISENCHANTER_ITEM.get());
                        output.accept(ENDER_INHIBITOR_ITEM.get());
                        output.accept(WITHER_INHIBITOR_ITEM.get());
                        output.accept(MAGIC_HAT_ITEM.get());
                        output.accept(PRESENT_ITEM.get());
                        output.accept(PICNIC_BASKET_ITEM.get());
                        output.accept(GRAHAM_CRACKER.get());
                        output.accept(CHOCOLATE_BAR.get());
                        output.accept(MARSHMALLOW.get());
                        output.accept(TOASTED_MARSHMALLOW.get());
                        output.accept(SMORE.get());
                        if (ModCompatRegistration.DNA_BOOSTER_AQUACULTURE != null)
                            output.accept(ModCompatRegistration.DNA_BOOSTER_AQUACULTURE.get());
                        if (ModCompatRegistration.DNA_BOOSTER_AETHER_PASSIVE != null)
                            output.accept(ModCompatRegistration.DNA_BOOSTER_AETHER_PASSIVE.get());
                        if (ModCompatRegistration.DNA_BOOSTER_AETHER_HOSTILE != null)
                            output.accept(ModCompatRegistration.DNA_BOOSTER_AETHER_HOSTILE.get());
                        if (ModCompatRegistration.DNA_BOOSTER_EVILCRAFT != null)
                            output.accept(ModCompatRegistration.DNA_BOOSTER_EVILCRAFT.get());
                        output.accept(GIANT_SKELETON_ORNAMENT_ITEM.get());
                        output.accept(GIANT_PLAYER_ORNAMENT_ITEM.get());
                        output.accept(GIANT_HEROBRINE_ORNAMENT_ITEM.get());
                        ItemStack skullLights = new ItemStack(STRING_OF_SKULL_LIGHTS.get());
                        skullLights.set(SKULL_LIGHTS.get(), SkullLights.defaultLights());
                        output.accept(skullLights);
                        output.accept(HEAD_PIKE_ITEM.get());
                        // A Mob Head for every mob without a vanilla head (the ones Beheading drops).
                        for (EntityType<?> type : MobHeads.MOD_HEADS) output.accept(MobHeads.modHead(type));
                        output.accept(RED_ALERT_BUTTON_ITEM.get());
                        output.accept(DRAMATIC_BUTTON_ITEM.get());
                        output.accept(RIMSHOT_BUTTON_ITEM.get());
                        output.accept(WILHELM_BUTTON_ITEM.get());
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
        ENTITY_TYPES.register();
        MENUS.register();
        DATA_COMPONENTS.register();
        CREATIVE_TABS.register();
        SOUND_EVENTS.register();
        RECIPE_TYPES.register();
        RECIPE_SERIALIZERS.register();
        LOOT_FUNCTIONS.register();
        STRUCTURE_PROCESSORS.register();
        STRUCTURE_TYPES.register();
        PARTICLE_TYPES.register();
        net.bobofraggins.mobfarmingsupplies.advancement.MFSTriggers.TRIGGERS.register();
    }
}
