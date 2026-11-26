package net.bobofraggins.mobfarmingsupplies.gametest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * For every block of the mod that opens a screen: a player with a mixed inventory opens it and
 * shift-clicks every slot, twice over (into the machine and back out). Nothing may throw, and the
 * items in the menu must add up the same afterwards: no duplication, nothing lost.
 * Then it moves everything it can into the machine and breaks it: every item must come back out
 * (as drops or still in the player's inventory). Blocks that open no screen pass straight away.
 */
final class MenuTests {

    private MenuTests() {}

    /** What the player carries: things machines take (containers, mob drops, tools) and things they don't. */
    private static final List<ItemStack> INVENTORY = List.of(
            new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET),
            new ItemStack(Items.GLASS_BOTTLE, 16), new ItemStack(Items.EXPERIENCE_BOTTLE, 8),
            new ItemStack(Items.IRON_SWORD), new ItemStack(Items.ROTTEN_FLESH, 32), new ItemStack(Items.BONE, 32),
            new ItemStack(Items.EGG, 16), new ItemStack(Items.BOOK, 4), new ItemStack(Items.ENCHANTED_BOOK),
            new ItemStack(Items.REDSTONE, 32), new ItemStack(Items.DIAMOND_PICKAXE));

    static List<TestFunction> all() {
        List<TestFunction> tests = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!GameTestKit.isOurs(id) || block.asItem() == Items.AIR) continue;
            tests.add(GameTestKit.test("menus", id.getPath(), 60, h -> run(h, block)));
        }
        return tests;
    }

    private static void run(GameTestHelper helper, Block block) {
        ServerPlayer player = GameTestKit.player(helper);
        try {
            BlockState placed = BlockTests.place(helper, player, block.asItem());
            if (placed.isAir()) {
                helper.fail("placing " + block.asItem() + " placed nothing");
                return;
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            for (int i = 0; i < INVENTORY.size(); i++) player.getInventory().setItem(i + 1, INVENTORY.get(i).copy());
            helper.useBlock(GameTestKit.CENTRE, player);
            AbstractContainerMenu menu = player.containerMenu;
            if (menu == player.inventoryMenu) {
                helper.succeed(); // opens no screen
                return;
            }
            GameTestKit.Problems problems = new GameTestKit.Problems();
            if (!menu.stillValid(player)) problems.add("the menu says it isn't valid right after opening");
            for (int pass = 1; pass <= 2; pass++) {
                Map<Item, Integer> before = count(menu);
                for (int slot = 0; slot < menu.slots.size(); slot++) {
                    if (!menu.slots.get(slot).hasItem()) continue;
                    try {
                        menu.clicked(slot, 0, ClickType.QUICK_MOVE, player);
                    } catch (RuntimeException e) {
                        problems.add("shift-clicking slot " + slot + " threw " + e);
                    }
                }
                Map<Item, Integer> after = count(menu);
                if (!before.equals(after)) {
                    problems.add("shift-click pass " + pass + " changed the items: before " + before + ", after " + after);
                }
            }
            // Into the machine once more, then break it: everything must come back out.
            Map<Item, Integer> start = count(menu);
            for (int slot = menu.slots.size() - 1; slot >= 0; slot--) {
                if (menu.slots.get(slot).container == player.getInventory() && menu.slots.get(slot).hasItem()) {
                    menu.clicked(slot, 0, ClickType.QUICK_MOVE, player);
                }
            }
            player.closeContainer();
            String keeps = KEEPS_CONTENTS_ELSEWHERE.get(BuiltInRegistries.BLOCK.getKey(block).getPath());
            if (keeps == null) brokenGivesBack(helper, player, block, start, problems);
            problems.finish(helper);
        } finally {
            GameTestKit.remove(helper, player);
        }
    }

    /**
     * Blocks known to lose what's inside when broken, and why that's accepted. Anything else must
     * give back every item it holds, as drops or inside its dropped item (a shulker box's way).
     */
    private static final String TANK_SLOTS = "known: the fill-and-empty slots aren't dropped (accepted 2026-11-26)";
    private static final Map<String, String> KEEPS_CONTENTS_ELSEWHERE = Map.of(
            "tank", TANK_SLOTS, "gold_tank", TANK_SLOTS, "diamond_tank", TANK_SLOTS, "emerald_tank", TANK_SLOTS,
            "experience_pylon", TANK_SLOTS);

    private static void brokenGivesBack(GameTestHelper helper, ServerPlayer player, Block block,
                                        Map<Item, Integer> start, GameTestKit.Problems problems) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(GameTestKit.CENTRE);
        level.destroyBlock(pos, true, player);
        Map<Item, Integer> after = new HashMap<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) after.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        for (ItemEntity dropped : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3))) {
            after.merge(dropped.getItem().getItem(), dropped.getItem().getCount(), Integer::sum);
            countCarried(dropped.getItem(), after);
            dropped.discard();
        }
        start.forEach((item, count) -> {
            int now = after.getOrDefault(item, 0);
            if (now < count) problems.add("breaking it lost " + (count - now) + " " + item + " that were inside");
        });
    }

    /** Adds the items a dropped block item carries in its block entity data (any list of item stacks). */
    private static void countCarried(ItemStack stack, Map<Item, Integer> counts) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) return;
        CompoundTag data = tag.getCompound("BlockEntityTag");
        for (String key : data.getAllKeys()) {
            if (!(data.get(key) instanceof ListTag list) || list.getElementType() != Tag.TAG_COMPOUND) continue;
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                if (!entry.contains("id")) continue;
                ItemStack carried = ItemStack.of(entry);
                if (!carried.isEmpty()) counts.merge(carried.getItem(), carried.getCount(), Integer::sum);
            }
        }
    }

    private static Map<Item, Integer> count(AbstractContainerMenu menu) {
        Map<Item, Integer> counts = new HashMap<>();
        menu.slots.forEach(slot -> {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        });
        ItemStack carried = menu.getCarried();
        if (!carried.isEmpty()) counts.merge(carried.getItem(), carried.getCount(), Integer::sum);
        return counts;
    }
}
