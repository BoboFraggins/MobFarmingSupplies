package net.bobofraggins.mobfarmingsupplies.lawnornament;

import dev.architectury.event.events.common.LifecycleEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.mixin.StructureTemplatePoolAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * Puts Giant Skeleton Lawn Ornaments in villages: a small patch with the ornament on it is added to
 * each vanilla village's houses pool, so a street can grow one where it would otherwise grow a
 * house. Added when the server starts, as the pools are loaded with the world.
 *
 * <p>The lawn goes in once, at weight 1, the least a pool element can have. Nothing else in the
 * pool is touched, so the houses already there (vanilla's, or ones other mods add) keep their
 * weights. A village tries a dozen or so houses, so the lawn turns up in about three villages in
 * five, often twice (measured by placing 64 villages).
 *
 * <p>The lawn's bottom layer is the skeleton and its plants: a village piece's bottom layer sits
 * one block above the street's ground, so the lawn stands on the village's own ground.
 */
public final class VillageLawnOrnaments {

    private static final String[] VILLAGES = {"plains", "desert", "savanna", "snowy", "taiga"};

    private VillageLawnOrnaments() {}

    public static void register() {
        LifecycleEvent.SERVER_BEFORE_START.register(server -> addToVillages(server.registryAccess()));
    }

    private static void addToVillages(RegistryAccess registries) {
        Registry<StructureTemplatePool> pools = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        for (String village : VILLAGES) {
            StructureTemplatePool pool = pools.get(
                    ResourceLocation.withDefaultNamespace("village/" + village + "/houses"));
            if (pool == null) continue; // removed by a datapack
            String location = MobFarmingSuppliesCommon.MODID + ":village/" + village + "_giant_skeleton_lawn";
            ObjectArrayList<StructurePoolElement> templates = ((StructureTemplatePoolAccessor) pool).getTemplates();
            if (templates.stream().anyMatch(element -> element.toString().contains(location))) continue;
            templates.add(StructurePoolElement.single(location).apply(StructureTemplatePool.Projection.RIGID));
        }
    }
}
