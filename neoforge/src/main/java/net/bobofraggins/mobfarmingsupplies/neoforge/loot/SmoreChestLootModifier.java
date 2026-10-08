package net.bobofraggins.mobfarmingsupplies.neoforge.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
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
 * Chest-loot modifier for S'mores in village house chests: 1-3 of them, with the chance read from
 * {@link MFSConfig#getSmoreChestChance()} at apply-time (rather than baked into the JSON
 * conditions), so it can be changed via the mod's config file without editing data files.
 */
public class SmoreChestLootModifier extends LootModifier {

    public static final MapCodec<SmoreChestLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst)
                    .and(Codec.STRING.fieldOf("item").forGetter(m -> m.itemId))
                    .apply(inst, SmoreChestLootModifier::new));

    private final String itemId;
    @Nullable private transient Item resolvedItem;
    private transient boolean itemResolved = false;

    protected SmoreChestLootModifier(LootItemCondition[] conditions, int priority, String itemId) {
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
        if (item != null && context.getRandom().nextFloat() < (float) MFSConfig.getSmoreChestChance()) {
            generatedLoot.add(new ItemStack(item, 1 + context.getRandom().nextInt(3)));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
