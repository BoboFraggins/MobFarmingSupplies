package net.bobofraggins.mobfarmingsupplies.tritanium;

import java.util.List;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;

/**
 * A pickaxe, axe, shovel, hoe and sword in one, after Mekanism's Paxel. Mining comes from its
 * tool component (the {@code mobfarmingsupplies:mineable/paxel} tag). Using it on a block tries
 * what an axe would do (strip, scrape, unwax), then a shovel (path, douse a campfire), then a
 * hoe (till), by asking those vanilla items with the Paxel in hand, so they wear the Paxel and
 * pick up other mods' additions to them. On NeoForge {@code TritaniumPaxelItemNeoForge} also
 * claims the axe, shovel and hoe abilities, which NeoForge checks for those actions.
 */
public class TritaniumPaxelItem extends Item {

    private static final List<Item> TOOLS = List.of(Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE);

    public TritaniumPaxelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        for (Item tool : TOOLS) {
            InteractionResult result = tool.useOn(context);
            if (result != InteractionResult.PASS) return result;
        }
        return InteractionResult.PASS;
    }
}
