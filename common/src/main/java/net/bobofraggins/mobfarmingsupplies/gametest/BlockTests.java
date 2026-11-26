package net.bobofraggins.mobfarmingsupplies.gametest;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * For every block of the mod that has an item: a player places it from the item, it ticks for a
 * second, and then:
 * <ul>
 *   <li>it is still there;
 *   <li>its block entity's saved data comes back the same after a save and load, and its client
 *       sync data can be made;
 *   <li>broken with the right tool, it drops its item;
 *   <li>pick-block gives an item;
 *   <li>breaking it leaves none of its pieces behind (two-block machines).
 * </ul>
 */
final class BlockTests {

    private BlockTests() {}

    static List<TestFunction> all() {
        List<TestFunction> tests = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!GameTestKit.isOurs(id) || block.asItem() == Items.AIR) continue;
            tests.add(GameTestKit.test("blocks", id.getPath(), 100, h -> run(h, block)));
        }
        return tests;
    }

    /**
     * Places {@code item} at {@link GameTestKit#CENTRE} as {@code player} would, clicking the top of
     * the floor; returns the placed state. Tries again sneaking if that placed nothing (the Picnic
     * Basket places only then), and leaves the player standing.
     */
    static BlockState place(GameTestHelper helper, ServerPlayer player, Item item) {
        BlockPos at = helper.absolutePos(GameTestKit.CENTRE);
        for (boolean sneaking : new boolean[] {false, true}) {
            player.setShiftKeyDown(sneaking);
            ItemStack stack = new ItemStack(item);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            helper.placeAt(player, stack, GameTestKit.CENTRE.below(), Direction.UP);
            if (!helper.getLevel().getBlockState(at).isAir()) break;
        }
        player.setShiftKeyDown(false);
        return helper.getLevel().getBlockState(at);
    }

    private static void run(GameTestHelper helper, Block block) {
        ServerPlayer player = GameTestKit.player(helper);
        BlockState placed = place(helper, player, block.asItem());
        if (placed.isAir()) {
            GameTestKit.remove(helper, player);
            helper.fail("placing " + block.asItem() + " placed nothing");
            return;
        }
        helper.runAfterDelay(20, () -> {
            try {
                check(helper, player, block);
            } finally {
                GameTestKit.remove(helper, player);
            }
        });
    }

    private static void check(GameTestHelper helper, ServerPlayer player, Block block) {
        GameTestKit.Problems problems = new GameTestKit.Problems();
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(GameTestKit.CENTRE);
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            helper.fail("the block disappeared within a second of being placed");
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            CompoundTag saved = be.saveWithFullMetadata();
            BlockEntity loaded = BlockEntity.loadStatic(pos, state, saved);
            if (loaded == null) {
                problems.add("its block entity could not be loaded from its own save data");
            } else {
                CompoundTag again = loaded.saveWithFullMetadata();
                if (!saved.equals(again)) problems.add("save data changed after a save and load:\n    before " + saved + "\n    after  " + again);
            }
            try {
                be.getUpdateTag();
                be.getUpdatePacket();
            } catch (RuntimeException e) {
                problems.add("making its client sync data threw " + e);
            }
        }

        ItemStack tool = toolFor(state);
        List<ItemStack> drops = Block.getDrops(state, level, pos, be, player, tool);
        Item expected = block.asItem();
        Item placedAs = state.getBlock().asItem();
        if (drops.stream().noneMatch(d -> d.is(expected) || d.is(placedAs))) {
            problems.add("broken with " + (tool.isEmpty() ? "a bare hand" : tool.getItem()) + " it drops "
                    + drops + " instead of " + expected);
        }

        ItemStack picked = state.getBlock().getCloneItemStack(level, pos, state);
        if (picked.isEmpty()) problems.add("pick-block gives nothing");

        level.destroyBlock(pos, false, player);
        if (!level.getBlockState(pos).isAir()) problems.add("it is still there after being broken");
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 2, 1))) {
            BlockState left = level.getBlockState(near);
            if (GameTestKit.isOurs(BuiltInRegistries.BLOCK.getKey(left.getBlock()))) {
                problems.add("breaking it left " + left + " behind at " + near.subtract(pos));
            }
        }
        problems.finish(helper);
    }

    static ItemStack toolFor(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return new ItemStack(Items.NETHERITE_PICKAXE);
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) return new ItemStack(Items.NETHERITE_AXE);
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return new ItemStack(Items.NETHERITE_SHOVEL);
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) return new ItemStack(Items.NETHERITE_HOE);
        return state.requiresCorrectToolForDrops() ? new ItemStack(Items.NETHERITE_PICKAXE) : ItemStack.EMPTY;
    }
}
