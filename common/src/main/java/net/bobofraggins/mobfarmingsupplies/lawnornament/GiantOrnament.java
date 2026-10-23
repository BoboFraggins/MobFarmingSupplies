package net.bobofraggins.mobfarmingsupplies.lawnornament;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The giant lawn ornaments: each is a life-size figure drawn {@link GiantOrnamentRenderer#SCALE}
 * times over on its own block. What a figure looks like (model and texture) is client-side, in
 * {@link GiantOrnamentRenderer#figure}. Adding one means a constant here, a block and item in
 * {@code Registration} (with {@code registerGiantOrnament}), and its model in the renderer.
 */
public enum GiantOrnament implements StringRepresentable {
    SKELETON("skeleton", 1.74f),
    HEROBRINE("herobrine", 1.62f),
    /** Wears its owner's skin ({@link GiantOrnamentBlockEntity#owner}), or Steve's or Alex's without one. */
    PLAYER("player", 1.62f);

    public static final Codec<GiantOrnament> CODEC = StringRepresentable.fromEnum(GiantOrnament::values);

    private final String name;
    private final float eyeHeight;

    GiantOrnament(String name, float eyeHeight) {
        this.name = name;
        this.eyeHeight = eyeHeight;
    }

    /** The block and item id, e.g. {@code giant_skeleton_lawn_ornament}. */
    public String id() {
        return "giant_" + name + "_lawn_ornament";
    }

    /** How high the figure's eyes are at life size, in blocks (an entity's eye height). */
    public float eyeHeight() {
        return eyeHeight;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
