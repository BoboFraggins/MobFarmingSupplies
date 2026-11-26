package net.bobofraggins.mobfarmingsupplies.loot;

import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.compat.DataComponentMap;
import net.bobofraggins.mobfarmingsupplies.shared.compat.DataComponentType;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ImplicitComponents;
import net.bobofraggins.mobfarmingsupplies.shared.compat.LootFunctions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function {@code mobfarmingsupplies:copy_components}: 1.20.1's stand-in for 1.20.5+'s
 * {@code minecraft:copy_components} from a block entity. {@code include} names what to copy:
 * {@code minecraft:block_entity_data} (the block entity's save data, as {@code BlockEntityTag}),
 * or a component the block entity puts on its item ({@link ImplicitComponents}), e.g.
 * {@code mobfarmingsupplies:tank_contents} or {@code minecraft:profile}.
 */
public class CopyComponentsFunction extends LootItemConditionalFunction {

    public static final LootItemConditionalFunction.Serializer<CopyComponentsFunction> SERIALIZER =
            LootFunctions.serializer((json, conditions) -> {
                List<String> include = new ArrayList<>();
                if (json.has("include")) {
                    for (var e : GsonHelper.getAsJsonArray(json, "include")) include.add(e.getAsString());
                }
                return new CopyComponentsFunction(conditions, include);
            }, (json, f) -> {
                JsonArray array = new JsonArray();
                f.include.forEach(array::add);
                json.add("include", array);
                json.addProperty("source", "block_entity");
            });

    private final List<String> include;

    private CopyComponentsFunction(LootItemCondition[] predicates, List<String> include) {
        super(predicates);
        this.include = List.copyOf(include);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity be = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (be == null) return stack;
        if (include.contains("minecraft:block_entity_data")) {
            CompoundTag data = be.saveWithoutMetadata();
            if (be instanceof ImplicitComponents holder) holder.removeComponentsFromTag(data);
            if (!data.isEmpty()) stack.getOrCreateTag().put("BlockEntityTag", data);
        }
        if (be instanceof ImplicitComponents holder) {
            DataComponentMap.Builder components = new DataComponentMap.Builder();
            holder.collectImplicitComponents(components);
            components.applyTo(stack, this::included);
        }
        return stack;
    }

    private boolean included(DataComponentType<?> type) {
        String key = type.key();
        if (key.equals("SkullOwner")) return include.contains("minecraft:profile");
        return include.contains(key);
    }

    @Override
    public LootItemFunctionType getType() {
        return Registration.COPY_COMPONENTS.get();
    }
}
