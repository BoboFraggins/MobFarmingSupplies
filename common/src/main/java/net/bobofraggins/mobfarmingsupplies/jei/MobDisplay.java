package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSamplePackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Mobs drawn on the JEI guide pages: made once per type for the current level, and drawn into a
 * box on the page, scaled to fit and turning to follow the mouse.
 */
final class MobDisplay {

    /** Pixels per block at most, so small mobs aren't drawn huge. */
    private static final int MAX_SCALE = 30;

    private record Key(EntityType<?> type, boolean baby) {}

    private final Map<Key, Optional<LivingEntity>> mobs = new HashMap<>();
    @Nullable private ClientLevel mobsLevel;

    /** A {@code type} mob to draw (a baby if {@code baby}), or null if it can't be made here. */
    @Nullable
    LivingEntity get(EntityType<?> type, boolean baby) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        if (level != mobsLevel) {
            mobs.clear();
            mobsLevel = level;
        }
        return mobs.computeIfAbsent(new Key(type, baby), key -> {
            try {
                Optional<LivingEntity> mob = Optional.ofNullable(key.type().create(level))
                        .filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast);
                mob.ifPresent(m -> DnaSamplePackItem.applyBabyState(m, key.baby()));
                return mob;
            } catch (RuntimeException e) {
                return Optional.empty(); // some modded mobs can't be made outside the world
            }
        }).orElse(null);
    }

    /**
     * Draws {@code mob} in the box from ({@code x0}, {@code y0}) to ({@code x1}, {@code y1}) of the
     * page, turned towards the mouse, as {@code InventoryScreen.renderEntityInInventoryFollowsMouse}
     * does but without its scissor: 1.21.1 places the scissor in screen space, ignoring the page's
     * offset, which clipped the mob away. The mob is scaled to fit the box, so it stays inside.
     */
    static void draw(GuiGraphics graphics, LivingEntity mob, int x0, int y0, int x1, int y1,
                     double mouseX, double mouseY) {
        float size = Math.max(mob.getBbWidth(), mob.getBbHeight());
        float scale = Math.min(MAX_SCALE, (y1 - y0 - 4) / Math.max(size, 0.1f));
        float centreX = (x0 + x1) / 2f;
        float centreY = (y0 + y1) / 2f;
        float yaw = (float) Math.atan((centreX - mouseX) / 40.0);
        float pitch = (float) Math.atan((centreY - mouseY) / 40.0);
        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf().rotateX(pitch * 20.0f * Mth.DEG_TO_RAD);
        pose.mul(camera);

        float bodyRot = mob.yBodyRot, yRot = mob.getYRot(), xRot = mob.getXRot();
        float headRotO = mob.yHeadRotO, headRot = mob.yHeadRot;
        mob.yBodyRot = 180.0f + yaw * 20.0f;
        mob.setYRot(180.0f + yaw * 40.0f);
        mob.setXRot(-pitch * 20.0f);
        mob.yHeadRot = mob.getYRot();
        mob.yHeadRotO = mob.getYRot();
        float mobScale = mob.getScale();
        Vector3f offset = new Vector3f(0.0f, mob.getBbHeight() / 2.0f + 0.0625f * mobScale, 0.0f);
        InventoryScreen.renderEntityInInventory(graphics, centreX, centreY, scale / mobScale, offset, pose, camera, mob);
        mob.yBodyRot = bodyRot;
        mob.setYRot(yRot);
        mob.setXRot(xRot);
        mob.yHeadRotO = headRotO;
        mob.yHeadRot = headRot;
    }
}
