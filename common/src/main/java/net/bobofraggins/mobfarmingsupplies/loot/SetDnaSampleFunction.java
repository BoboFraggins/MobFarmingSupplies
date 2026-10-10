package net.bobofraggins.mobfarmingsupplies.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSampleItem;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSamplePackItem;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

/**
 * Loot function {@code mobfarmingsupplies:set_dna_sample}: makes a DNA Sample of a random mob from
 * a DNA Pack's list ({@code pack}, e.g. {@code mobfarmingsupplies:dna_sample_rare}), so it follows
 * the pack's configured mobs. The mob is created and set up as the pack would spawn it, saved
 * into the sample as the DNA Collector would, and never added to the world.
 */
public class SetDnaSampleFunction extends LootItemConditionalFunction {

    public static final MapCodec<SetDnaSampleFunction> CODEC = RecordCodecBuilder.mapCodec(i ->
            commonFields(i).and(Identifier.CODEC.fieldOf("pack").forGetter(f -> f.pack))
                    .apply(i, SetDnaSampleFunction::new));

    private final Identifier pack;

    private SetDnaSampleFunction(Optional<Holder<LootItemCondition>> condition, Identifier pack) {
        super(condition);
        this.pack = pack;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!(BuiltInRegistries.ITEM.getValue(pack) instanceof DnaSamplePackItem packItem)) return stack;
        ServerLevel level = context.getLevel();
        Vec3 origin = context.getOptional(LootContextParams.ORIGIN);
        BlockPos pos = origin != null ? BlockPos.containing(origin) : BlockPos.ZERO;
        ItemStack packStack = new ItemStack(packItem);
        Entity mob = packItem.createSpawnEntity(packStack, level, pos, context.getRandom());
        if (mob == null) return stack;
        mob.setPos(Vec3.atBottomCenterOf(pos));
        packItem.onSpawnPositioned(packStack, level, mob);
        stack.set(Registration.DNA_SAMPLE_CONTENTS.get(),
                DnaSampleItem.createSample(mob).get(Registration.DNA_SAMPLE_CONTENTS.get()));
        return stack;
    }

    @Override
    public MapCodec<SetDnaSampleFunction> codec() {
        return CODEC;
    }
}
