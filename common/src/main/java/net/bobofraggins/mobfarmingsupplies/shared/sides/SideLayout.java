package net.bobofraggins.mobfarmingsupplies.shared.sides;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Which world side each button of a side-configuration grid shows. The grid has six cells:
 * <pre>
 *   .      TOP    .
 *   LEFT   FRONT  RIGHT
 *   .      BOTTOM BACK
 * </pre>
 * A block placed by a player records an orientation ({@link FrontAndTop}): BACK is the block it was
 * placed against, FRONT the side opposite, and LEFT / RIGHT / TOP / BOTTOM are as the placer saw
 * them — up and down on a wall; away from and toward the placer on a floor or ceiling.
 *
 * <p>Blocks placed before orientations existed (or by anything other than a player) have none and
 * keep the fixed {@link #LEGACY} layout every side grid used before, so existing installations
 * look the same as before.
 * Side settings themselves are always stored by world direction; this only changes the grid.
 */
public final class SideLayout {

    public static final int TOP = 0, LEFT = 1, FRONT = 2, RIGHT = 3, BOTTOM = 4, BACK = 5;
    public static final int CELLS = 6;

    /** {column, row} of each cell in the 3×3 grid. */
    public static final int[][] CELL_POS = {{1, 0}, {0, 1}, {1, 1}, {2, 1}, {1, 2}, {2, 2}};

    /** Lang key suffix of each cell ({@code gui.mobfarmingsupplies.side_grid.<name>}). */
    public static final String[] CELL_NAMES = {"top", "left", "front", "right", "bottom", "back"};

    /** The fixed grid all four blocks used before orientations existed. */
    public static final Direction[] LEGACY =
            {Direction.UP, Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.DOWN, Direction.NORTH};

    private static final Codec<FrontAndTop> CODEC = StringRepresentable.fromEnum(FrontAndTop::values);
    private static final String NBT_KEY = "SideOrientation";

    private SideLayout() {}

    /** The world side shown in each cell for a block with {@code orientation}. */
    public static Direction[] of(FrontAndTop orientation) {
        Direction front = orientation.front();
        Direction top = orientation.top();
        // Placed on a wall the placer faced the back, so their right is the front turned left;
        // on a floor or ceiling, their right is the way they faced turned right.
        Direction right = front.getAxis().isHorizontal() ? front.getCounterClockWise() : top.getClockWise();
        Direction[] cells = new Direction[CELLS];
        cells[TOP] = top;
        cells[LEFT] = right.getOpposite();
        cells[FRONT] = front;
        cells[RIGHT] = right;
        cells[BOTTOM] = top.getOpposite();
        cells[BACK] = front.getOpposite();
        return cells;
    }

    /** The grid for a block: its orientation's, or the legacy layout if it has none. */
    public static Direction[] ofNullable(@Nullable FrontAndTop orientation) {
        return orientation == null ? LEGACY : of(orientation);
    }

    /** The orientation of a block a player is placing. */
    public static FrontAndTop fromPlacement(BlockPlaceContext ctx) {
        Direction front = ctx.getClickedFace();
        Direction top = front.getAxis().isHorizontal() ? Direction.UP : ctx.getHorizontalDirection();
        return FrontAndTop.fromFrontAndTop(front, top);
    }

    public static void save(ValueOutput output, @Nullable FrontAndTop orientation) {
        if (orientation != null) output.store(NBT_KEY, CODEC, orientation);
    }

    @Nullable
    public static FrontAndTop load(ValueInput input) {
        return input.read(NBT_KEY, CODEC).orElse(null);
    }
}
