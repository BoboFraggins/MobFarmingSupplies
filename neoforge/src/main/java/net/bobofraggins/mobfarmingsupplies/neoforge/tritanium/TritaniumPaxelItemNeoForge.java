package net.bobofraggins.mobfarmingsupplies.neoforge.tritanium;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.bobofraggins.mobfarmingsupplies.tritanium.TritaniumPaxelItem;
import net.minecraft.world.item.ItemInstance;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * The Paxel on NeoForge, which only lets an item strip, flatten or till if it claims that
 * ability. It claims everything an axe, shovel and hoe can do.
 */
public class TritaniumPaxelItemNeoForge extends TritaniumPaxelItem {

    private static final Set<ItemAbility> ABILITIES = ImmutableSet.<ItemAbility>builder()
            .addAll(ItemAbilities.DEFAULT_AXE_ACTIONS)
            .addAll(ItemAbilities.DEFAULT_SHOVEL_ACTIONS)
            .addAll(ItemAbilities.DEFAULT_HOE_ACTIONS)
            .build();

    public TritaniumPaxelItemNeoForge(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
        return ABILITIES.contains(ability);
    }
}
