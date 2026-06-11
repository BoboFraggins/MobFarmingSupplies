package net.bobofraggins.mobfarmingsupplies.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import org.jetbrains.annotations.Nullable;

/**
 * Generic chest-loot modifier that adds one copy of a configurable item.
 * The item ID is stored as a plain string and resolved lazily on first use,
 * so this modifier is safe to load even when the target mod (and thus the
 * item) is absent — it simply produces no output in that case.
 */
public class DnaSamplePackChestLootModifier extends LootModifier {

    public static final MapCodec<DnaSamplePackChestLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst)
                    .and(Codec.STRING.fieldOf("item").forGetter(m -> m.itemId))
                    .apply(inst, DnaSamplePackChestLootModifier::new));

    private final String itemId;
    @Nullable private transient Item resolvedItem;
    private transient boolean itemResolved = false;

    protected DnaSamplePackChestLootModifier(LootItemCondition[] conditions, int priority, String itemId) {
        super(conditions, priority);
        this.itemId = itemId;
    }

    @Nullable
    private Item getItem() {
        if (!itemResolved) {
            itemResolved = true;
            Identifier rl = Identifier.tryParse(itemId);
            if (rl != null) {
                Item found = BuiltInRegistries.ITEM.getValue(rl);
                resolvedItem = (found != null && found != Items.AIR) ? found : null;
            }
        }
        return resolvedItem;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Item item = getItem();
        if (item != null) generatedLoot.add(new ItemStack(item));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
