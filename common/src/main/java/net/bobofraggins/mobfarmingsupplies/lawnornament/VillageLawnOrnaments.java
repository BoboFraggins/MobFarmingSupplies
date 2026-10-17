package net.bobofraggins.mobfarmingsupplies.lawnornament;

import dev.architectury.event.events.common.LifecycleEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.mixin.StructureTemplatePoolAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

/**
 * Puts Giant Skeleton Lawn Ornaments in villages: a small lawn with the ornament on it is added to
 * each vanilla village's houses pool, so a street can grow one where it would otherwise grow a
 * house. Added when the server starts, as the pools are loaded with the world.
 *
 * <p>The lawn goes in once, at weight 1, the least a pool element can have. Nothing else in the
 * pool is touched, so the houses already there (vanilla's, or ones other mods add) keep their
 * weights. A village tries a dozen or so houses, so the lawn turns up in about three villages in
 * five, often twice (measured by placing 64 villages). To make the skeleton rarer than that, the
 * {@code giant_skeleton_lawn} processor list leaves it off half the lawns, which are then just
 * grass and flowers. With that, about two villages in five have a skeleton (25 of 61 placed).
 */
public final class VillageLawnOrnaments {

    private static final ResourceKey<StructureProcessorList> PROCESSORS = ResourceKey.create(
            Registries.PROCESSOR_LIST, Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "giant_skeleton_lawn"));

    private static final String[] VILLAGES = {"plains", "desert", "savanna", "snowy", "taiga"};

    private VillageLawnOrnaments() {}

    public static void register() {
        LifecycleEvent.SERVER_BEFORE_START.register(server -> addToVillages(server.registryAccess()));
    }

    private static void addToVillages(RegistryAccess registries) {
        Registry<StructureTemplatePool> pools = registries.lookupOrThrow(Registries.TEMPLATE_POOL);
        Holder<StructureProcessorList> processors = registries.lookupOrThrow(Registries.PROCESSOR_LIST).getOrThrow(PROCESSORS);
        for (String village : VILLAGES) {
            StructureTemplatePool pool = pools.getValue(
                    Identifier.withDefaultNamespace("village/" + village + "/houses"));
            if (pool == null) continue; // removed by a datapack
            String location = MobFarmingSuppliesCommon.MODID + ":village/" + village + "_giant_skeleton_lawn";
            ObjectArrayList<StructurePoolElement> templates = ((StructureTemplatePoolAccessor) pool).getTemplates();
            if (templates.stream().anyMatch(element -> element.toString().contains(location))) continue;
            templates.add(StructurePoolElement.single(location, processors).apply(StructureTemplatePool.Projection.RIGID));
        }
    }
}
