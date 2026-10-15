package net.bobofraggins.mobfarmingsupplies.jei;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.bobofraggins.mobfarmingsupplies.dna.DnaSamplePackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

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
                Optional<LivingEntity> mob = Optional.ofNullable(key.type().create(level, EntitySpawnReason.LOAD))
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
     * page. The mob is drawn in screen space, outside the page's own offset, so the box and the
     * mouse are moved there first.
     */
    static void draw(GuiGraphicsExtractor graphics, LivingEntity mob, int x0, int y0, int x1, int y1,
                     double mouseX, double mouseY) {
        float size = Math.max(mob.getBbWidth(), mob.getBbHeight());
        float scale = Math.min(MAX_SCALE, (y1 - y0 - 4) / Math.max(size, 0.1f));
        Vector2f topLeft = graphics.pose().transformPosition(x0, y0, new Vector2f());
        Vector2f bottomRight = graphics.pose().transformPosition(x1, y1, new Vector2f());
        Vector2f mouse = graphics.pose().transformPosition((float) mouseX, (float) mouseY, new Vector2f());
        float guiScale = (bottomRight.x - topLeft.x) / (x1 - x0);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                Math.round(topLeft.x), Math.round(topLeft.y), Math.round(bottomRight.x), Math.round(bottomRight.y),
                Math.round(scale * guiScale), 0.0625f, mouse.x, mouse.y, mob);
    }
}
