package net.bobofraggins.mobfarmingsupplies.neoforge.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.bobofraggins.mobfarmingsupplies.MFSConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import org.jetbrains.annotations.Nullable;

/**
 * Chest-loot modifier for the Magic Hat. The drop chance is read from
 * {@link MFSConfig#getMagicHatChestChance()} at apply-time (rather than baked into the
 * JSON conditions), so it can be changed via the mod's config file without editing data files.
 */
public class MagicHatChestLootModifier extends LootModifier {

    public static final MapCodec<MagicHatChestLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst)
                    .and(Codec.STRING.fieldOf("item").forGetter(m -> m.itemId))
                    .apply(inst, MagicHatChestLootModifier::new));

    private final String itemId;
    @Nullable private transient Item resolvedItem;
    private transient boolean itemResolved = false;

    protected MagicHatChestLootModifier(LootItemCondition[] conditions, String itemId) {
        super(conditions);
        this.itemId = itemId;
    }

    @Nullable
    private Item getItem() {
        if (!itemResolved) {
            itemResolved = true;
            ResourceLocation rl = ResourceLocation.tryParse(itemId);
            if (rl != null) {
                Item found = BuiltInRegistries.ITEM.get(rl);
                resolvedItem = (found != null && found != Items.AIR) ? found : null;
            }
        }
        return resolvedItem;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Item item = getItem();
        if (item != null && context.getRandom().nextFloat() < (float) MFSConfig.getMagicHatChestChance()) {
            generatedLoot.add(new ItemStack(item));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
