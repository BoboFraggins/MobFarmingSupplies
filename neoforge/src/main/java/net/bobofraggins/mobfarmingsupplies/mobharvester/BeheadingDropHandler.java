package net.bobofraggins.mobfarmingsupplies.mobharvester;

import net.bobofraggins.mobfarmingsupplies.register.MGRRegistryHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Map;

/**
 * Handles head drops triggered by the Mob Harvester's Beheading Upgrade.
 *
 * <p>Registered on the NeoForge event bus (server-side) from
 * {@link net.bobofraggins.mobfarmingsupplies.MobFarmingSupplies}.
 *
 * <p>Drop chance: {@code random.nextInt(10) < beheadingLevel}, matching MGU's formula.
 * With 1 upgrade = 10 % chance; with 10 upgrades = 100 % chance.
 *
 * <p>Only mobs with a vanilla head item are eligible; babies are excluded.
 */
public class BeheadingDropHandler {

    private static DataComponentType<Integer> beheadingLevelType = null;

    /** Maps entity types that have a corresponding vanilla head item. */
    private static final Map<EntityType<?>, Item> HEAD_MAP = Map.of(
            EntityType.ZOMBIE,          Items.ZOMBIE_HEAD,
            EntityType.SKELETON,        Items.SKELETON_SKULL,
            EntityType.CREEPER,         Items.CREEPER_HEAD,
            EntityType.WITHER_SKELETON, Items.WITHER_SKELETON_SKULL,
            EntityType.PIGLIN,          Items.PIGLIN_HEAD,
            EntityType.ENDER_DRAGON,    Items.DRAGON_HEAD
    );

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity().getHealth() > 0f) return;

        // Must be killed by the harvester's fake player wielding a beheading sword
        if (!(event.getSource().getEntity() instanceof HarvesterFakePlayer fp)) return;

        ItemStack weapon = fp.getMainHandItem();
        if (!(weapon.getItem() instanceof HarvesterSword)) return;

        if (beheadingLevelType == null) beheadingLevelType = MGRRegistryHelper.getDataComponentType("beheading_level");
        int beheadingLevel = weapon.getOrDefault(beheadingLevelType, 0);
        if (beheadingLevel <= 0) return;

        // Roll against the drop chance
        if (event.getEntity().level().getRandom().nextInt(10) >= beheadingLevel) return;

        ItemStack head = getHead(event.getEntity());
        if (head.isEmpty()) return;

        LivingEntity entity = event.getEntity();
        ItemEntity ie = new ItemEntity(
                entity.level(), entity.getX(), entity.getY(), entity.getZ(), head);
        ie.setDefaultPickUpDelay();
        event.getDrops().add(ie);
    }

    private static ItemStack getHead(LivingEntity entity) {
        if (entity.isBaby()) return ItemStack.EMPTY;
        Item headItem = HEAD_MAP.get(entity.getType());
        return headItem != null ? new ItemStack(headItem) : ItemStack.EMPTY;
    }
}
