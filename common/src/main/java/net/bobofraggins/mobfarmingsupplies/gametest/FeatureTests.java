package net.bobofraggins.mobfarmingsupplies.gametest;

import dev.architectury.fluid.FluidStack;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.absorptionhopper.AbsorptionHopperBlockEntity;
import net.bobofraggins.mobfarmingsupplies.disenchanter.DisenchanterBlockEntity;
import net.bobofraggins.mobfarmingsupplies.essencehopper.Essence;
import net.bobofraggins.mobfarmingsupplies.fan.FanBlock;
import net.bobofraggins.mobfarmingsupplies.mobharvester.MobHarvesterBlock;
import net.bobofraggins.mobfarmingsupplies.potion.TieredPotionItem;
import net.bobofraggins.mobfarmingsupplies.register.MFSRegistryHelper;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.bobofraggins.mobfarmingsupplies.shared.compat.ItemComponents;
import net.bobofraggins.mobfarmingsupplies.tank.TankBlockEntity;
import net.bobofraggins.mobfarmingsupplies.tank.TankContents;
import net.bobofraggins.mobfarmingsupplies.upgradestation.UpgradeStationBlockEntity;
import net.bobofraggins.mobfarmingsupplies.upgradestation.UpgradingRecipe;
import net.bobofraggins.mobfarmingsupplies.xpanvil.XpAnvilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** What the machines actually do, a scenario or two each. */
final class FeatureTests {

    private FeatureTests() {}

    static List<TestFunction> all() {
        List<TestFunction> t = new ArrayList<>();
        t.add(GameTestKit.test("tank", "bucket_in_and_out", 40, FeatureTests::tankBuckets));
        t.add(GameTestKit.test("tank", "xp_bottles", 40, FeatureTests::tankBottles));
        t.add(GameTestKit.test("tank", "fill_slot_empties_bucket", 60, FeatureTests::tankFillSlot));
        t.add(GameTestKit.test("tank", "keeps_contents_when_broken", 40, FeatureTests::tankKeepsContents));
        t.add(GameTestKit.test("absorption_hopper", "picks_up_and_pushes", 200, FeatureTests::absorptionHopperItems));
        t.add(GameTestKit.test("absorption_hopper", "collects_xp", 100, FeatureTests::absorptionHopperXp));
        t.add(GameTestKit.test("mob_harvester", "kills_and_drops", 200, FeatureTests::harvester));
        t.add(GameTestKit.test("fan", "pushes_items", 100, FeatureTests::fan));
        t.add(GameTestKit.test("brewing", "oresight", 600, h -> brew(h, Essence.SIGHT, Items.RAW_GOLD, TieredPotionItem.Kind.ORESIGHT)));
        t.add(GameTestKit.test("brewing", "flight", 600, h -> brew(h, Essence.AIR, Items.ELYTRA, TieredPotionItem.Kind.FLIGHT)));
        t.add(GameTestKit.test("xp_anvil", "repairs", 100, FeatureTests::anvil));
        t.add(GameTestKit.test("disenchanter", "pulls_enchantment", 400, FeatureTests::disenchanter));
        t.add(GameTestKit.test("upgrade_station", "every_recipe", 40, FeatureTests::upgradeStation));
        return t;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private static Fluid xpJuice() {
        return MFSRegistryHelper.getFluid("xp_juice");
    }

    /** Places {@code item} at {@link GameTestKit#CENTRE} and hands its block entity to {@code then}. */
    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> void withPlaced(GameTestHelper helper, Item item, Class<T> type,
                                                          java.util.function.BiConsumer<ServerPlayer, T> then) {
        ServerPlayer player = GameTestKit.player(helper);
        BlockTests.place(helper, player, item);
        BlockEntity be = helper.getBlockEntity(GameTestKit.CENTRE);
        if (!type.isInstance(be)) {
            helper.fail("expected a " + type.getSimpleName() + " after placing " + item + ", found " + be);
            return;
        }
        then.accept(player, (T) be);
    }

    private static void check(GameTestHelper helper, boolean ok, String problem) {
        if (!ok) helper.fail(problem);
    }

    private static ItemStack hand(ServerPlayer player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND);
    }

    // ── Tank ────────────────────────────────────────────────────────────────────

    private static void tankBuckets(GameTestHelper helper) {
        withPlaced(helper, Registration.TANK_ITEM.get(), TankBlockEntity.class, (player, tank) -> {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            helper.useBlock(GameTestKit.CENTRE, player);
            check(helper, tank.getAmount() == 1000 && tank.getStoredFluid().getFluid() == Fluids.WATER,
                    "a water bucket put " + tank.getAmount() + " mB of " + tank.getStoredFluid().getFluid() + " in");
            check(helper, hand(player).is(Items.BUCKET), "emptying a water bucket left " + hand(player) + " in hand");
            helper.useBlock(GameTestKit.CENTRE, player);
            check(helper, tank.getAmount() == 0, "filling a bucket left " + tank.getAmount() + " mB");
            check(helper, hand(player).is(Items.WATER_BUCKET), "filling a bucket gave " + hand(player));
            helper.succeed();
        });
    }

    private static void tankBottles(GameTestHelper helper) {
        withPlaced(helper, Registration.TANK_ITEM.get(), TankBlockEntity.class, (player, tank) -> {
            tank.insert(FluidStack.create(xpJuice(), 1000), 1000, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
            helper.useBlock(GameTestKit.CENTRE, player);
            check(helper, hand(player).is(Items.EXPERIENCE_BOTTLE), "a glass bottle on XP Juice gave " + hand(player));
            check(helper, tank.getAmount() == 750, "filling a bottle left " + tank.getAmount() + " mB, not 750");
            helper.useBlock(GameTestKit.CENTRE, player);
            check(helper, hand(player).is(Items.GLASS_BOTTLE), "a Bottle o' Enchanting on the tank gave " + hand(player));
            check(helper, tank.getAmount() == 1000, "emptying a bottle left " + tank.getAmount() + " mB, not 1000");
            helper.succeed();
        });
    }

    private static void tankFillSlot(GameTestHelper helper) {
        withPlaced(helper, Registration.TANK_ITEM.get(), TankBlockEntity.class, (player, tank) -> {
            tank.transferContainer.setItem(0, new ItemStack(Items.WATER_BUCKET));
            helper.succeedWhen(() -> {
                check(helper, tank.getAmount() == 1000, "tank has " + tank.getAmount() + " mB");
                check(helper, tank.transferContainer.getItem(1).is(Items.BUCKET),
                        "output slot has " + tank.transferContainer.getItem(1));
            });
        });
    }

    private static void tankKeepsContents(GameTestHelper helper) {
        withPlaced(helper, Registration.TANK_ITEM.get(), TankBlockEntity.class, (player, tank) -> {
            tank.insert(FluidStack.create(Fluids.LAVA, 5000), 5000, false);
            ServerLevel level = helper.getLevel();
            BlockPos pos = helper.absolutePos(GameTestKit.CENTRE);
            BlockState state = level.getBlockState(pos);
            List<ItemStack> drops = Block.getDrops(state, level, pos, tank, player, BlockTests.toolFor(state));
            ItemStack dropped = drops.stream().filter(s -> s.is(Registration.TANK_ITEM.get())).findFirst().orElse(ItemStack.EMPTY);
            if (dropped.isEmpty()) {
                helper.fail("the tank dropped " + drops);
                return;
            }
            TankContents contents = ItemComponents.getOrDefault(dropped, Registration.TANK_CONTENTS.get(), TankContents.EMPTY);
            check(helper, contents.amount() == 5000 && contents.storedFluid().getFluid() == Fluids.LAVA,
                    "the dropped tank holds " + contents.amount() + " mB of " + contents.storedFluid().getFluid());
            level.removeBlock(pos, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, dropped);
            helper.placeAt(player, dropped, GameTestKit.CENTRE.below(), Direction.UP);
            if (!(level.getBlockEntity(pos) instanceof TankBlockEntity placed)) {
                helper.fail("placing the dropped tank placed no tank");
                return;
            }
            check(helper, placed.getAmount() == 5000 && placed.getStoredFluid().getFluid() == Fluids.LAVA,
                    "the placed tank holds " + placed.getAmount() + " mB of " + placed.getStoredFluid().getFluid());
            helper.succeed();
        });
    }

    // ── Absorption Hopper ───────────────────────────────────────────────────────

    private static void absorptionHopperItems(GameTestHelper helper) {
        ServerPlayer player = GameTestKit.player(helper);
        helper.setBlock(GameTestKit.CENTRE, Blocks.CHEST);
        BlockPos hopperAt = GameTestKit.CENTRE.above();
        ItemStack hopper = new ItemStack(MFSRegistryHelper.getItem("absorption_hopper"));
        player.setItemInHand(InteractionHand.MAIN_HAND, hopper);
        helper.placeAt(player, hopper, GameTestKit.CENTRE, Direction.UP);
        if (!(helper.getBlockEntity(hopperAt) instanceof AbsorptionHopperBlockEntity be)) {
            helper.fail("no absorption hopper on the chest");
            return;
        }
        be.setPushSides(1 << 1); // down, into the chest
        ServerLevel level = helper.getLevel();
        Vec3 at = Vec3.atCenterOf(helper.absolutePos(hopperAt.above(2)));
        ItemEntity item = new ItemEntity(level, at.x, at.y, at.z, new ItemStack(Items.COBBLESTONE, 10));
        item.setNoPickUpDelay();
        level.addFreshEntity(item);
        helper.succeedWhen(() -> {
            Container chest = (Container) helper.getBlockEntity(GameTestKit.CENTRE);
            int inChest = chest.countItem(Items.COBBLESTONE);
            check(helper, inChest == 10, "chest has " + inChest + " cobblestone, hopper has "
                    + be.getInventory().countItem(Items.COBBLESTONE));
        });
    }

    private static void absorptionHopperXp(GameTestHelper helper) {
        withPlaced(helper, MFSRegistryHelper.getItem("absorption_hopper"), AbsorptionHopperBlockEntity.class, (player, be) -> {
            Vec3 at = Vec3.atCenterOf(helper.absolutePos(GameTestKit.CENTRE.above(2)));
            helper.getLevel().addFreshEntity(new ExperienceOrb(helper.getLevel(), at.x, at.y, at.z, 10));
            helper.succeedWhen(() -> check(helper, be.getTankAmount() == 200 && be.getTankFluid().getFluid() == xpJuice(),
                    "tank has " + be.getTankAmount() + " mB of " + be.getTankFluid().getFluid() + ", wanted 200 mB of XP Juice"));
        });
    }

    // ── Mob Harvester and Fan ───────────────────────────────────────────────────

    private static void harvester(GameTestHelper helper) {
        helper.setBlock(GameTestKit.CENTRE.west(), Blocks.REDSTONE_BLOCK);
        ServerPlayer player = GameTestKit.player(helper);
        BlockState state = BlockTests.place(helper, player, MFSRegistryHelper.getItem("mob_harvester"));
        if (!state.hasProperty(MobHarvesterBlock.POWERED) || !state.getValue(MobHarvesterBlock.POWERED)) {
            helper.fail("the harvester isn't powered next to a block of redstone: " + state);
            return;
        }
        Cow cow = helper.spawn(EntityType.COW, GameTestKit.CENTRE.east());
        cow.setNoAi(true);
        helper.succeedWhen(() -> {
            check(helper, !cow.isAlive(), "the cow is still alive (" + cow.getHealth() + " health)");
            AABB area = new AABB(helper.absolutePos(GameTestKit.CENTRE)).inflate(4);
            boolean beef = !helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(Items.BEEF)).isEmpty();
            check(helper, beef, "the cow dropped no beef");
        });
    }

    private static void fan(GameTestHelper helper) {
        helper.setBlock(GameTestKit.CENTRE.east(), Blocks.REDSTONE_BLOCK);
        ServerPlayer player = GameTestKit.player(helper);
        BlockState state = BlockTests.place(helper, player, MFSRegistryHelper.getItem("fan"));
        if (!state.hasProperty(FanBlock.POWERED) || !state.getValue(FanBlock.POWERED)) {
            helper.fail("the fan isn't powered next to a block of redstone: " + state);
            return;
        }
        Direction facing = state.getValue(FanBlock.FACING);
        if (facing.getAxis() == Direction.Axis.Y) {
            helper.fail("the fan faces " + facing + "; the test expects it to face the player");
            return;
        }
        BlockPos start = GameTestKit.CENTRE.relative(facing);
        ItemEntity item = helper.spawnItem(Items.STICK, start);
        Vec3 from = item.position();
        helper.succeedWhen(() -> {
            double moved = item.position().subtract(from).dot(Vec3.atLowerCornerOf(facing.getNormal()));
            check(helper, moved >= 1.5, "the item moved " + String.format("%.2f", moved) + " blocks " + facing + ", wanted 1.5");
        });
    }

    // ── Brewing ─────────────────────────────────────────────────────────────────

    private static void brew(GameTestHelper helper, Essence essence, Item ingredient, TieredPotionItem.Kind kind) {
        helper.setBlock(GameTestKit.CENTRE, Blocks.BREWING_STAND);
        BrewingStandBlockEntity stand = (BrewingStandBlockEntity) helper.getBlockEntity(GameTestKit.CENTRE);
        stand.setItem(0, new ItemStack(Registration.ESSENCE_BOTTLES.get(essence).get()));
        stand.setItem(3, new ItemStack(ingredient));
        stand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
        helper.succeedWhen(() -> {
            ItemStack out = stand.getItem(0);
            check(helper, out.getItem() instanceof TieredPotionItem p && p.kind() == kind,
                    "the bottle slot has " + out + ", ingredient slot " + stand.getItem(3));
        });
    }

    // ── XP machines ─────────────────────────────────────────────────────────────

    private static void anvil(GameTestHelper helper) {
        withPlaced(helper, MFSRegistryHelper.getItem("anvil"), XpAnvilBlockEntity.class, (player, anvil) -> {
            anvil.insert(FluidStack.create(xpJuice(), 1000), 1000, false);
            ItemStack sword = new ItemStack(Items.IRON_SWORD);
            sword.setDamageValue(200);
            anvil.items().setItem(XpAnvilBlockEntity.INPUT, sword);
            helper.succeedWhen(() -> {
                ItemStack out = anvil.items().getItem(XpAnvilBlockEntity.OUTPUT);
                check(helper, out.is(Items.IRON_SWORD) && !out.isDamaged(), "output slot has " + out);
                check(helper, anvil.getAmount() == 800, "the repair left " + anvil.getAmount() + " mB, not 800");
            });
        });
    }

    private static void disenchanter(GameTestHelper helper) {
        withPlaced(helper, MFSRegistryHelper.getItem("disenchanter"), DisenchanterBlockEntity.class, (player, be) -> {
            be.insert(FluidStack.create(xpJuice(), 1000), 1000, false);
            ItemStack sword = new ItemStack(Items.IRON_SWORD);
            sword.enchant(Enchantments.SHARPNESS, 3);
            sword.enchant(Enchantments.UNBREAKING, 2);
            be.items().setItem(DisenchanterBlockEntity.ITEM, sword);
            be.items().setItem(DisenchanterBlockEntity.BOOK, new ItemStack(Items.BOOK));
            helper.succeedWhen(() -> {
                ItemStack out = be.items().getItem(DisenchanterBlockEntity.OUTPUT);
                check(helper, out.is(Items.ENCHANTED_BOOK), "output slot has " + out);
                int left = EnchantmentHelper.getEnchantments(be.items().getItem(DisenchanterBlockEntity.ITEM)).size();
                check(helper, left == 1, "the sword has " + left + " enchantments left, wanted 1");
                check(helper, be.getAmount() == 750, "it used " + (1000 - be.getAmount()) + " mB, wanted 250");
            });
        });
    }

    private static void upgradeStation(GameTestHelper helper) {
        withPlaced(helper, MFSRegistryHelper.getItem("upgrade_station"), UpgradeStationBlockEntity.class, (player, be) -> {
            SimpleContainer items = field(be, "items");
            GameTestKit.Problems problems = new GameTestKit.Problems();
            int checked = 0;
            for (Recipe<?> r : helper.getLevel().getRecipeManager().getRecipes()) {
                if (!(r instanceof UpgradingRecipe recipe) || recipe.mobHead().isPresent()) continue;
                ItemStack[] item = recipe.item().getItems(), input = recipe.input().getItems();
                if (item.length == 0 || input.length == 0) {
                    problems.add(recipe.getId() + ": an ingredient matches no items");
                    continue;
                }
                checked++;
                items.setItem(UpgradeStationBlockEntity.ITEM_SLOT, item[0].copy());
                items.setItem(UpgradeStationBlockEntity.INPUT_SLOT, input[0].copy());
                ItemStack out = items.getItem(UpgradeStationBlockEntity.OUTPUT_SLOT);
                if (!out.is(recipe.result().getItem())) {
                    problems.add(recipe.getId() + ": " + item[0] + " + " + input[0] + " gave " + out + ", not " + recipe.result());
                }
                items.setItem(UpgradeStationBlockEntity.ITEM_SLOT, ItemStack.EMPTY);
                items.setItem(UpgradeStationBlockEntity.INPUT_SLOT, ItemStack.EMPTY);
            }
            if (checked == 0) problems.add("found no upgrading recipes to try");
            problems.finish(helper);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T field(Object owner, String name) {
        try {
            Field f = owner.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(owner);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("no field " + name + " on " + owner.getClass(), e);
        }
    }
}
