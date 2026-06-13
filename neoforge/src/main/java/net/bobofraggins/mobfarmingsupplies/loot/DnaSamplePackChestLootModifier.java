package net.bobofraggins.mobfarmingsupplies.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.bobofraggins.mobfarmingsupplies.MGRConfig;
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
 *
 * <p>The drop chance is read from {@link MGRConfig#getDnaSamplePackCommonChestChance()}
 * / {@link MGRConfig#getDnaSamplePackRareChestChance()} at apply-time (rather than baked
 * into the JSON conditions), so it can be changed via the mod's config file without
 * editing data files. The {@code chance_tier} field selects which configured chance
 * applies:
 * <ul>
 *   <li>{@code "common"} — always use the common-tier chance.</li>
 *   <li>{@code "rare"} — always use the rare-tier chance.</li>
 *   <li>{@code "auto"} (default) — classify the queried loot table via
 *       {@link ChestLootTables#NETHER_END_CHESTS}, using the rare-tier chance for
 *       nether/end chests and the common-tier chance otherwise.</li>
 * </ul>
 */
public class DnaSamplePackChestLootModifier extends LootModifier {

    public static final MapCodec<DnaSamplePackChestLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst)
                    .and(Codec.STRING.fieldOf("item").forGetter(m -> m.itemId))
                    .and(Codec.STRING.optionalFieldOf("chance_tier", "auto").forGetter(m -> m.chanceTier))
                    .apply(inst, DnaSamplePackChestLootModifier::new));

    private final String itemId;
    private final String chanceTier;
    @Nullable private transient Item resolvedItem;
    private transient boolean itemResolved = false;

    protected DnaSamplePackChestLootModifier(LootItemCondition[] conditions, int priority, String itemId, String chanceTier) {
        super(conditions, priority);
        this.itemId = itemId;
        this.chanceTier = chanceTier;
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
        if (item == null) return generatedLoot;

        float chance = switch (chanceTier) {
            case "common" -> (float) MGRConfig.getDnaSamplePackCommonChestChance();
            case "rare" -> (float) MGRConfig.getDnaSamplePackRareChestChance();
            default -> ChestLootTables.NETHER_END_CHESTS.contains(context.getQueriedLootTableId().toString())
                    ? (float) MGRConfig.getDnaSamplePackRareChestChance()
                    : (float) MGRConfig.getDnaSamplePackCommonChestChance();
        };

        if (context.getRandom().nextFloat() < chance) {
            generatedLoot.add(new ItemStack(item));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
