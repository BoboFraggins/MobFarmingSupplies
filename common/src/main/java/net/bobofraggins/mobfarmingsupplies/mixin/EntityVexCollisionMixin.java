package net.bobofraggins.mobfarmingsupplies.mixin;

import java.util.ArrayList;
import java.util.List;
import net.bobofraggins.mobfarmingsupplies.register.MFSTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Stops vexes at blocks in {@link MFSTags.Blocks#BLOCKS_VEXES}. A Vex moves with {@code noPhysics}
 * set, so it skips block collision entirely; this clips its movement against just those blocks.
 */
@Mixin(Entity.class)
public abstract class EntityVexCollisionMixin {

    @Shadow public boolean noPhysics;

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 mobfarmingsupplies$stopVexAtGlass(Vec3 movement, MoverType type) {
        Entity self = (Entity) (Object) this;
        if (!noPhysics || !(self instanceof Vex) || movement.lengthSqr() == 0) return movement;
        AABB box = self.getBoundingBox();
        AABB swept = box.expandTowards(movement);
        List<VoxelShape> shapes = new ArrayList<>();
        CollisionContext context = CollisionContext.of(self);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(swept.minX, swept.minY, swept.minZ),
                BlockPos.containing(swept.maxX, swept.maxY, swept.maxZ))) {
            BlockState state = self.level().getBlockState(pos);
            if (state.is(MFSTags.Blocks.BLOCKS_VEXES)) {
                shapes.add(state.getCollisionShape(self.level(), pos, context).move(pos.getX(), pos.getY(), pos.getZ()));
            }
        }
        if (shapes.isEmpty()) return movement;
        // Same axis order as vanilla collision: Y first, then the larger horizontal axis.
        double x = movement.x, y = movement.y, z = movement.z;
        y = Shapes.collide(Direction.Axis.Y, box, shapes, y);
        box = box.move(0, y, 0);
        if (Math.abs(x) < Math.abs(z)) {
            z = Shapes.collide(Direction.Axis.Z, box, shapes, z);
            box = box.move(0, 0, z);
            x = Shapes.collide(Direction.Axis.X, box, shapes, x);
        } else {
            x = Shapes.collide(Direction.Axis.X, box, shapes, x);
            box = box.move(x, 0, 0);
            z = Shapes.collide(Direction.Axis.Z, box, shapes, z);
        }
        return new Vec3(x, y, z);
    }
}
