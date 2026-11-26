package net.bobofraggins.mobfarmingsupplies.forge.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.fml.ModList;

/**
 * {@code mobfarmingsupplies:add_table}: adds a roll of {@code table} to the loot (NeoForge's
 * {@code neoforge:add_table}, which Forge 1.20.1 doesn't have). With {@code required_mod}, only
 * while that mod is installed (the table may name its items).
 */
public class AddTableLootModifier extends LootModifier {

    public static final Codec<AddTableLootModifier> CODEC = RecordCodecBuilder.create(inst ->
            LootModifier.codecStart(inst)
                    .and(ResourceLocation.CODEC.fieldOf("table").forGetter(m -> m.table))
                    .and(Codec.STRING.optionalFieldOf("required_mod").forGetter(m -> m.requiredMod))
                    .apply(inst, AddTableLootModifier::new));

    private final ResourceLocation table;
    private final Optional<String> requiredMod;

    public AddTableLootModifier(LootItemCondition[] conditions, ResourceLocation table, Optional<String> requiredMod) {
        super(conditions);
        this.table = table;
        this.requiredMod = requiredMod;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (requiredMod.isPresent() && !ModList.get().isLoaded(requiredMod.get())) return generatedLoot;
        // The raw roll: no loot modifiers on the added table, so this can't feed back into itself.
        context.getResolver().getLootTable(table).getRandomItemsRaw(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
