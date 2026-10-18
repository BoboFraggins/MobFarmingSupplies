package net.bobofraggins.mobfarmingsupplies.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Set;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A soft light-grey glow drawn behind items that are mostly black, so they show up in slots and
 * JEI: the glass blocks, the Tank, the inhibitors, a few machines and the darker mob heads. GUI
 * only (see {@code GuiGraphicsBackdropMixin}); in hand and in the world the items are as usual.
 */
public final class GuiBackdrop {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mobfarmingsupplies", "textures/item/gui_backdrop.png");
    /** Mob Heads dark enough to need it. */
    private static final Set<EntityType<?>> DARK_HEADS = Set.of(EntityType.CAT, EntityType.ENDERMAN, EntityType.BAT,
            EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.MAGMA_CUBE);

    private GuiBackdrop() {}

    /** Whether {@code stack} is drawn on a backdrop in GUIs. */
    public static boolean wants(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Registration.MOB_HEAD_ITEM.get()) return DARK_HEADS.contains(stack.get(Registration.MOB_HEAD_TYPE.get()));
        return item == Registration.WITHER_PROOF_GLASS_ITEM.get() || item == Registration.MOB_EXCLUSION_GLASS_ITEM.get()
                || item == Registration.TANK_ITEM.get() || item == Registration.ENDER_INHIBITOR_ITEM.get()
                || item == Registration.WITHER_INHIBITOR_ITEM.get() || item == Registration.CLONE_O_MATIC_ITEM.get()
                || item == Registration.MAGIC_HAT_ITEM.get() || item == Registration.MOB_HARVESTER_ITEM.get()
                || item == Registration.EXPERIENCE_SYRINGE.get();
    }

    /** Whether {@code stack}'s GUI icon is drawn bigger than its slot and has to be clipped to it. */
    public static boolean clipped(ItemStack stack) {
        return stack.getItem() == Registration.GIANT_SKELETON_ORNAMENT_ITEM.get();
    }

    /** Draws the backdrop over the 16x16 slot at ({@code x}, {@code y}), behind where the item goes. */
    public static void draw(GuiGraphics graphics, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(TEXTURE, x, y, 16, 16, 0f, 0f, 64, 64, 64, 64);
        RenderSystem.disableBlend();
    }
}
