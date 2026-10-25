package net.bobofraggins.mobfarmingsupplies.lawnornament;

import dev.architectury.event.events.common.LifecycleEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.mixin.StructureTemplatePoolAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

/**
 * Puts Giant Skeleton Lawn Ornaments in villages: a Halloween yard with the ornament in it
 * ({@code village/halloween_lawn}) is added to each vanilla village's houses pool, so a street can
 * grow one where it would otherwise grow a house. Every village type gets the same yard. Added when
 * the server starts, as the pools are loaded with the world.
 *
 * <p>The lawn goes in once, at weight 1, the least a pool element can have. Nothing else in the
 * pool is touched, so the houses already there (vanilla's, or ones other mods add) keep their
 * weights. A village tries a dozen or so houses, so the lawn turns up in about three villages in
 * five, often twice (measured by placing 64 villages).
 *
 * <p>Some yards have another giant in the skeleton's place ({@link #STAND_INS}, swapped in by
 * {@link OrnamentSwapProcessor}): one in five has Herobrine, and one in five a Giant Player Lawn
 * Ornament belonging to a player who has connected to the world (the skeleton stays when nobody
 * has yet).
 *
 * <p>A village piece is placed level with its street jigsaw, one block above the street's ground.
 * The yard's jigsaw is in its second layer, so its bottom layer of grass and paths lands at the
 * street's ground level instead, replacing the village's ground there.
 */
public final class VillageLawnOrnaments {

    private static final String[] VILLAGES = {"plains", "desert", "savanna", "snowy", "taiga"};

    /** A giant that can stand in for the skeleton: the share of yards it gets, and whether a known player owns it. */
    private record StandIn(Supplier<? extends Block> block, float share, boolean ownedByKnownPlayer) {}

    private static final List<StandIn> STAND_INS = List.of(
            new StandIn(Registration.GIANT_HEROBRINE_ORNAMENT, 0.2f, false),
            new StandIn(Registration.GIANT_PLAYER_ORNAMENT, 0.2f, true));

    private VillageLawnOrnaments() {}

    public static void register() {
        LifecycleEvent.SERVER_BEFORE_START.register(server -> addToVillages(server.registryAccess()));
        KnownPlayers.register();
    }

    /**
     * One swap per stand-in, run in turn. Each only sees the skeletons the ones before it left, so
     * its chance is its share of what's left: 1/5, then 1/5 of the remaining 4/5 is 1/4.
     */
    private static Holder<StructureProcessorList> yardProcessors() {
        List<StructureProcessor> swaps = new ArrayList<>();
        float left = 1f;
        for (StandIn standIn : STAND_INS) {
            swaps.add(new OrnamentSwapProcessor(Registration.GIANT_SKELETON_ORNAMENT.get(), standIn.block().get(),
                    standIn.share() / left, standIn.ownedByKnownPlayer()));
            left -= standIn.share();
        }
        return Holder.direct(new StructureProcessorList(swaps));
    }

    private static void addToVillages(RegistryAccess registries) {
        Registry<StructureTemplatePool> pools = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        for (String village : VILLAGES) {
            StructureTemplatePool pool = pools.get(
                    ResourceLocation.withDefaultNamespace("village/" + village + "/houses"));
            if (pool == null) continue; // removed by a datapack
            String location = MobFarmingSuppliesCommon.MODID + ":village/halloween_lawn";
            ObjectArrayList<StructurePoolElement> templates = ((StructureTemplatePoolAccessor) pool).getTemplates();
            if (templates.stream().anyMatch(element -> element.toString().contains(location))) continue;
            templates.add(StructurePoolElement.single(location, yardProcessors())
                    .apply(StructureTemplatePool.Projection.RIGID));
        }
    }
}
