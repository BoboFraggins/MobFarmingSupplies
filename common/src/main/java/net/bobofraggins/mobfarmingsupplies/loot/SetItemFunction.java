package net.bobofraggins.mobfarmingsupplies.loot;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.compat.LootFunctions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

/**
 * Loot function {@code mobfarmingsupplies:set_item}: turns the stack into {@code item} (with
 * {@code nbt}), when that item exists. For loot naming another mod's item: 1.20.1 can't make a
 * loot table depend on a mod being installed, and a table naming a missing item fails to load.
 */
public class SetItemFunction extends LootItemConditionalFunction {

    public static final LootItemConditionalFunction.Serializer<SetItemFunction> SERIALIZER =
            LootFunctions.serializer((json, conditions) -> new SetItemFunction(conditions,
                    new ResourceLocation(GsonHelper.getAsString(json, "item")),
                    json.has("nbt") ? GsonHelper.getAsString(json, "nbt") : null), (json, f) -> {
                json.addProperty("item", f.item.toString());
                if (f.nbt != null) json.addProperty("nbt", f.nbt);
            });

    private final ResourceLocation item;
    @Nullable private final String nbt;

    private SetItemFunction(LootItemCondition[] predicates, ResourceLocation item, @Nullable String nbt) {
        super(predicates);
        this.item = item;
        this.nbt = nbt;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        Item found = BuiltInRegistries.ITEM.get(item);
        if (found == Items.AIR) return stack;
        ItemStack made = new ItemStack(found, stack.getCount());
        if (nbt != null) {
            try {
                CompoundTag tag = TagParser.parseTag(nbt);
                made.setTag(tag);
            } catch (CommandSyntaxException e) {
                return stack;
            }
        }
        return made;
    }

    @Override
    public LootItemFunctionType getType() {
        return Registration.SET_ITEM.get();
    }
}
