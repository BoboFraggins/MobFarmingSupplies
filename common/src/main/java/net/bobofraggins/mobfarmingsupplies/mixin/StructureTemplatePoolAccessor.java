package net.bobofraggins.mobfarmingsupplies.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {

    /** The pool's elements, each repeated by its weight; jigsaw placement picks from this list. */
    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> getTemplates();
}
