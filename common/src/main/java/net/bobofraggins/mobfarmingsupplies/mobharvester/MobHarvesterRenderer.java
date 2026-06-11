package net.bobofraggins.mobfarmingsupplies.mobharvester;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bobofraggins.mobfarmingsupplies.MobFarmingSuppliesCommon;
import net.bobofraggins.mobfarmingsupplies.client.model.ExtraBlockModels;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import java.util.HashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Block-entity renderer for the Mob Harvester.
 *
 * <h3>Static body</h3>
 * <p>Neck, shoulders, chest, waist, legs, and base are rendered by vanilla's static
 * block renderer from {@code block/mob_harvester.json}.
 *
 * <h3>Arms (extra block models)</h3>
 * <p>{@code block/mob_harvester_left_arm} and {@code block/mob_harvester_right_arm} are
 * rendered here.  When powered they swing using the player-attack formula:
 * <pre>swingAngle = sin(√t × π) × MAX_SWING</pre>
 * Left arm leads; right arm is offset half a cycle for an alternating strike.
 *
 * <h3>Head</h3>
 * <ul>
 *   <li><b>Unpowered</b> — Carved Pumpkin (vanilla item), facing forward, no tilt.</li>
 *   <li><b>Powered</b> — Custom head model ({@code block/mob_harvester_head}) using the
 *       {@code mob_harvester_head_face/side/top} textures; 10° forward tilt, slowly turning
 *       to look at adjacent squares.  Picks a new target every {@value #LOOK_SEGMENT_TICKS}
 *       ticks, spends the first {@value #LOOK_TRANSITION_FRAC_PCT}% smoothly rotating
 *       and the rest staring.</li>
 * </ul>
 */
public class MobHarvesterRenderer
        implements BlockEntityRenderer<MobHarvesterBlockEntity, MobHarvesterRenderer.HarvesterState> {

    // ── Arm/head extra block model ids ──────────────────────────────────────────────

    public static final Identifier LEFT_ARM_FRONT_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/mob_harvester_left_arm_front");
    public static final Identifier LEFT_ARM_SIDE_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/mob_harvester_left_arm_side");
    public static final Identifier LEFT_ARM_REAR_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/mob_harvester_left_arm_rear");
    public static final Identifier RIGHT_ARM_FRONT_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/mob_harvester_right_arm_front");
    public static final Identifier RIGHT_ARM_SIDE_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/mob_harvester_right_arm_side");
    public static final Identifier RIGHT_ARM_REAR_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/mob_harvester_right_arm_rear");
    public static final Identifier HEAD_MODEL_ID =
            Identifier.fromNamespaceAndPath(MobFarmingSuppliesCommon.MODID, "block/mob_harvester_head");

    // ── Weapons ───────────────────────────────────────────────────────────────────

    // ItemStacks cannot be created in static initializers — item data components may
    // not be bound yet when the renderer class is first loaded during resource reload.
    static final int WEAPON_COUNT = 6;

    private List<ItemStack> weapons;
    private ItemStack carvedPumpkin;

    private void ensureItems() {
        if (weapons == null) {
            weapons = List.of(
                    new ItemStack(Items.IRON_SWORD),
                    new ItemStack(Items.IRON_AXE),
                    new ItemStack(Items.IRON_PICKAXE),
                    new ItemStack(Items.IRON_SHOVEL),
                    new ItemStack(Items.IRON_HOE),
                    new ItemStack(Items.LIGHTNING_ROD)
            );
            carvedPumpkin = new ItemStack(Items.CARVED_PUMPKIN);
        }
    }

    // ── Arm shoulder pivots (block space, 0–1) ────────────────────────────────────

    private static final float[] LEFT_ORIGIN  = { 3f / 16f, 23f / 16f, 8f / 16f };
    private static final float[] RIGHT_ORIGIN = { 13f / 16f, 23f / 16f, 8f / 16f };

    // ── Blade-tip offsets from shoulder pivot (block space) ───────────────────────
    //
    // Each blade hangs 12 px below its shoulder and is baked at ±45°, which
    // displaces its tip 8.485 px (= 12 × sin 45°) diagonally.

    private static final float D = 8.485f / 16f;

    /** Blade-tip offsets {dx, dy, dz} from the respective arm origin. Indices 0–2
     *  are left-arm blades, 3–5 are right-arm blades. */
    private static final float[][] BLADE_OFFSET = {
            {  0f, -D, -D },   // 0  left-front
            { -D,  -D,  0f },  // 1  left-side
            {  0f, -D,  D  },  // 2  left-rear
            {  0f, -D, -D },   // 3  right-front
            {  D,  -D,  0f },  // 4  right-side
            {  0f, -D,  D  },  // 5  right-rear
    };

    /** Per-blade Y-rotation (radians) to orient each item outward along its blade. */
    private static final float[] BLADE_YAW = {
             0f,                       // 0  left-front  → north
             (float)(Math.PI / 2),     // 1  left-side   → west
             (float) Math.PI,          // 2  left-rear   → south
             0f,                       // 3  right-front → north
            -(float)(Math.PI / 2),     // 4  right-side  → east
             (float) Math.PI,          // 5  right-rear  → south
    };

    // ── Arm-swing animation ───────────────────────────────────────────────────────

    /** Peak arm-swing angle (radians). 60° = dramatic but readable strike arc. */
    private static final float MAX_SWING_RAD = (float)(Math.PI / 3);

    private static final float TWO_PI = (float)(Math.PI * 2);

    /** Swing period for the left arm (ticks per cycle). */
    private static final float LEFT_ARM_PERIOD  = 14f;
    /** Swing period for the right arm (ticks per cycle). */
    private static final float RIGHT_ARM_PERIOD = 18f;

    /** Phase offset for the left arm (radians). */
    private static final float LEFT_ARM_PHASE  = 0f;
    /** Phase offset for the right arm — offset by π so the arms alternate. */
    private static final float RIGHT_ARM_PHASE = (float) Math.PI;

    private static final float ITEM_SCALE = 0.5f;

    // ── Head geometry ─────────────────────────────────────────────────────────────

    /**
     * Head pivot in block space — sits atop the neck (neck top at y = 30 px).
     * Translate to this point, apply rotations, then render the item centered here.
     */
    private static final float HEAD_X = 0.5f;
    private static final float HEAD_Y = 30f / 16f;   // neck top
    private static final float HEAD_Z = 0.5f;

    /**
     * Scale for the pumpkin head.  FIXED context renders a block item at roughly
     * 0.5 units; 0.55 makes it sit naturally on the narrow neck.
     */
    private static final float HEAD_SCALE = 0.55f;

    /**
     * Base yaw offset (radians) so the pumpkin face points south (+z, forward)
     * when headYawDeg = 0.  May need tweaking in-game depending on how the
     * FIXED display context orients the pumpkin texture.
     */
    private static final float HEAD_BASE_YAW_RAD = (float) Math.PI;

    /** Downward tilt (degrees) applied when the harvester is powered. */
    private static final float HEAD_POWERED_PITCH_DEG = 10f;
    /** Maximum downward pitch (degrees) the head will ever aim. */
    private static final float MAX_PITCH_DOWN_DEG = 45f;

    // ── Head look-around animation ────────────────────────────────────────────────

    /** Ticks to smoothly rotate from one look target to the next. */
    private static final int    LOOK_TRANSITION_TICKS = 72;
    /** Minimum ticks to stare at a target before picking a new one. */
    private static final int    STARE_MIN_TICKS        = 60;
    /** Maximum ticks to stare at a target before picking a new one. */
    private static final int    STARE_MAX_TICKS        = 160;

    /** Radius (blocks) within which entities are considered look targets. */
    private static final double LOOK_RANGE = 8.0;

    /**
     * Candidate look directions (degrees, where 0 = south / forward).
     * Biased toward the four cardinal faces so the head stares at adjacent squares.
     */
    private static final float[] LOOK_TARGETS_DEG = {
             0f,   // south  (forward)
            90f,   // west
           180f,   // north  (behind)
           -90f,   // east
            45f,   // south-west
           135f,   // north-west
          -135f,   // north-east
           -45f,   // south-east
    };

    // ── Scratch quaternions (submit is single-threaded) ───────────────────────────

    private static final Quaternionf SWING_QUAT      = new Quaternionf();
    private static final Quaternionf YAW_QUAT        = new Quaternionf();
    private static final Quaternionf HEAD_YAW_QUAT   = new Quaternionf();
    private static final Quaternionf HEAD_PITCH_QUAT = new Quaternionf();
    private static final Quaternionf FACING_QUAT     = new Quaternionf();

    // ── Persistent per-block look state ──────────────────────────────────────────
    //
    // HarvesterState can have multiple live instances per block (one per render pass),
    // so it cannot hold state that must survive across frames. This map, keyed by
    // block position, is the single source of truth for the look animation.

    private static final class LookState {
        long  turnStartTick  = -1L; // -1 = not yet started (face forward)
        long  stareUntilTick = 0L;
        float prevYawDeg     = 0f;
        float prevPitchDeg   = 0f;
        float targetYawDeg   = 0f;
        float targetPitchDeg = 0f;
        boolean lookingAtPlayer = false;
        boolean turning         = false;
        // Output fields — written here, copied into HarvesterState for submit()
        float headYawDeg  = 0f;
        float headPitchDeg = 0f;
    }

    private final HashMap<BlockPos, LookState> lookStates = new HashMap<>();

    // ── Constructor ───────────────────────────────────────────────────────────────

    private final ItemModelResolver itemModelResolver;

    public MobHarvesterRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    // ── Render state ──────────────────────────────────────────────────────────────

    public static class HarvesterState extends BlockEntityRenderState {
        /** Arm swing angles in radians — [0] = left, [1] = right; both 0 when unpowered. */
        final float[] bladeSwingRad = new float[2];
        /** Weapon item states, one per blade. */
        final ItemStackRenderState[] weapons = new ItemStackRenderState[WEAPON_COUNT];
        /** Head item state — carved pumpkin item when unpowered; unused when powered. */
        final ItemStackRenderState headState = new ItemStackRenderState();
        /** True when the block has redstone power; selects head model vs item. */
        boolean powered;
        /** Current head look direction in degrees (0 = south / forward). */
        float headYawDeg;
        /** Downward pitch in degrees (0 unpowered, {@value HEAD_POWERED_PITCH_DEG} powered). */
        float headPitchDeg;

        /** Block-facing offset in degrees (0=south, 90=west, 180=north, −90=east). */
        float facingOffsetDeg = 0f;

        public HarvesterState() {
            for (int i = 0; i < weapons.length; i++) weapons[i] = new ItemStackRenderState();
        }
    }

    @Override
    public HarvesterState createRenderState() {
        return new HarvesterState();
    }

    // ── Two-phase rendering ───────────────────────────────────────────────────────

    @Override
    public void extractRenderState(
            MobHarvesterBlockEntity be,
            HarvesterState state,
            float partialTick,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {

        ensureItems();
        BlockEntityRenderState.extractBase(be, state, crumbling);

        Level level   = be.getLevel();
        BlockState blockState = be.getBlockState();
        boolean powered = blockState.getValue(MobHarvesterBlock.POWERED);
        state.facingOffsetDeg = facingOffsetDeg(blockState.getValue(MobHarvesterBlock.FACING));

        // ── Arms ─────────────────────────────────────────────────────────────────
        if (powered && level != null) {
            float time = level.getGameTime() + partialTick;
            state.bladeSwingRad[0] = Mth.sin(time * TWO_PI / LEFT_ARM_PERIOD  + LEFT_ARM_PHASE)  * MAX_SWING_RAD;
            state.bladeSwingRad[1] = Mth.sin(time * TWO_PI / RIGHT_ARM_PERIOD + RIGHT_ARM_PHASE) * MAX_SWING_RAD;
        } else {
            state.bladeSwingRad[0] = 0f;
            state.bladeSwingRad[1] = 0f;
        }

        // ── Blade weapons ─────────────────────────────────────────────────────────
        for (int i = 0; i < weapons.size(); i++) {
            state.weapons[i].clear();
            itemModelResolver.updateForTopItem(
                    state.weapons[i],
                    weapons.get(i),
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    level,
                    null,
                    i);
        }

        // ── Head ──────────────────────────────────────────────────────────────────
        state.powered = powered;
        state.headState.clear();
        if (!powered) {
            // Unpowered: vanilla Carved Pumpkin item, static, no tilt.
            itemModelResolver.updateForTopItem(
                    state.headState,
                    carvedPumpkin,
                    ItemDisplayContext.FIXED,
                    level,
                    null,
                    0);
        }

        BlockPos pos = be.getBlockPos();
        LookState ls = lookStates.computeIfAbsent(pos, p -> new LookState());

        if (powered && level != null) {
            long gameTime = level.getGameTime();

            // ── State machine: pick a new target when first powered or stare expires ──
            if (ls.turnStartTick < 0 || gameTime >= ls.stareUntilTick) {
                ls.prevYawDeg    = ls.headYawDeg;
                ls.prevPitchDeg  = ls.headPitchDeg;
                ls.turnStartTick = gameTime;
                ls.turning       = true;
                pickNewTarget(ls, gameTime, level, pos, state.facingOffsetDeg);
            }

            // ── Interpolate toward target over LOOK_TRANSITION_TICKS ─────────────────
            float segT = Math.min(1f,
                    (gameTime - ls.turnStartTick + partialTick) / LOOK_TRANSITION_TICKS);

            if (segT < 1f) {
                float smooth = segT * segT * (3f - 2f * segT);
                ls.headYawDeg   = lerpAngleDeg(ls.prevYawDeg,   ls.targetYawDeg,   smooth);
                ls.headPitchDeg = lerpAngleDeg(ls.prevPitchDeg, ls.targetPitchDeg, smooth);
            } else {
                ls.headYawDeg   = ls.targetYawDeg;
                ls.headPitchDeg = ls.targetPitchDeg;
                if (ls.turning) {
                    ls.turning = false;
                }
            }

            state.headYawDeg   = ls.headYawDeg;
            state.headPitchDeg = ls.headPitchDeg;
        } else {
            ls.headYawDeg    = 0f;
            ls.headPitchDeg  = 0f;
            ls.turnStartTick = -1L;
            ls.turning       = false;
            state.headYawDeg   = 0f;
            state.headPitchDeg = 0f;
        }
    }

    @Override
    public void submit(
            HarvesterState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState) {

        // Rotate all BER geometry around the block's XZ centre to match its facing.
        // The static block model is already rotated by the blockstate JSON y-rotation.
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(FACING_QUAT.rotationY(state.facingOffsetDeg * Mth.DEG_TO_RAD));
        poseStack.translate(-0.5, 0, -0.5);

        // ── Arms (six independent blades) ────────────────────────────────────────
        float lSwing = state.bladeSwingRad[0];
        float rSwing = state.bladeSwingRad[1];

        // Front/rear: X-axis swing; side: Z-axis swing (up/down in its own plane).
        submitBlade(poseStack, collector, state,
                ExtraBlockModels.get(LEFT_ARM_FRONT_MODEL_ID),
                LEFT_ORIGIN,  lSwing, false);
        submitBlade(poseStack, collector, state,
                ExtraBlockModels.get(LEFT_ARM_SIDE_MODEL_ID),
                LEFT_ORIGIN,  lSwing, true);
        submitBlade(poseStack, collector, state,
                ExtraBlockModels.get(LEFT_ARM_REAR_MODEL_ID),
                LEFT_ORIGIN,  lSwing, false);
        submitBlade(poseStack, collector, state,
                ExtraBlockModels.get(RIGHT_ARM_FRONT_MODEL_ID),
                RIGHT_ORIGIN, rSwing, false);
        submitBlade(poseStack, collector, state,
                ExtraBlockModels.get(RIGHT_ARM_SIDE_MODEL_ID),
                RIGHT_ORIGIN, rSwing, true);
        submitBlade(poseStack, collector, state,
                ExtraBlockModels.get(RIGHT_ARM_REAR_MODEL_ID),
                RIGHT_ORIGIN, rSwing, false);

        // ── Blade weapons ─────────────────────────────────────────────────────────
        for (int i = 0; i < BLADE_OFFSET.length; i++) {
            if (state.weapons[i].isEmpty()) continue;

            float[] origin   = (i < 3) ? LEFT_ORIGIN  : RIGHT_ORIGIN;
            float   swingRad = (i < 3) ? state.bladeSwingRad[0] : state.bladeSwingRad[1];
            float[] off      = BLADE_OFFSET[i];

            float cos = Mth.cos(swingRad);
            float sin = Mth.sin(swingRad);
            float wx, wy, wz;

            // Side blades (1 = left-side, 4 = right-side) swing on Z; others swing on X.
            if (i == 1 || i == 4) {
                // Z-axis rotation: x' = x·cos − y·sin, y' = x·sin + y·cos, z' = z
                wx = origin[0] + (off[0] * cos - off[1] * sin);
                wy = origin[1] + (off[0] * sin + off[1] * cos);
                wz = origin[2] + off[2];
            } else {
                // X-axis rotation: x' = x, y' = y·cos − z·sin, z' = y·sin + z·cos
                wx = origin[0] + off[0];
                wy = origin[1] + (off[1] * cos - off[2] * sin);
                wz = origin[2] + (off[1] * sin + off[2] * cos);
            }

            poseStack.pushPose();
            poseStack.translate(wx, wy, wz);
            poseStack.mulPose(YAW_QUAT.rotationY(BLADE_YAW[i]));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            state.weapons[i].submit(
                    poseStack, collector,
                    state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // ── Head ──────────────────────────────────────────────────────────────────
        {
            float yawRad   = state.headYawDeg   * Mth.DEG_TO_RAD;
            float pitchRad = state.headPitchDeg * Mth.DEG_TO_RAD;

            poseStack.pushPose();
            poseStack.translate(HEAD_X, HEAD_Y, HEAD_Z);

            // Yaw first (Y axis), then pitch (X axis), to match player look order.
            // Powered head model faces south by default → no base offset needed.
            // Unpowered pumpkin in FIXED context faces north → needs PI flip.
            float baseYaw = state.powered ? 0f : HEAD_BASE_YAW_RAD;
            poseStack.mulPose(HEAD_YAW_QUAT.rotationY(baseYaw - yawRad));
            if (pitchRad != 0f) {
                poseStack.mulPose(HEAD_PITCH_QUAT.rotationX(pitchRad));
            }

            poseStack.scale(HEAD_SCALE, HEAD_SCALE, HEAD_SCALE);

            if (state.powered) {
                // Powered: render custom block model with mob_harvester_head_* textures.
                BlockStateModelPart headModel = ExtraBlockModels.get(HEAD_MODEL_ID);
                if (headModel != null) {
                    // Block model occupies [0,1]³; center it at the pivot.
                    poseStack.translate(-0.5, -0.5, -0.5);
                    collector.submitBlockModel(
                            poseStack,
                            Sheets.cutoutBlockSheet(),
                            List.of(headModel),
                            new int[0],
                            state.lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            0);
                }
            } else if (!state.headState.isEmpty()) {
                // Unpowered: vanilla Carved Pumpkin item.
                // ItemDisplayContext.FIXED applies a built-in 0.5 scale; compensate so the
                // pumpkin matches the powered head model which renders at full block scale.
                poseStack.scale(2f, 2f, 2f);
                state.headState.submit(
                        poseStack, collector,
                        state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }

            poseStack.popPose();
        }

        poseStack.popPose(); // facing rotation
    }

    // ── Helpers — blade ───────────────────────────────────────────────────────────

    /**
     * Submits a single blade model, rotating it around its shoulder pivot.
     * Front/rear blades swing on the X axis; side blades swing on the Z axis.
     *
     * @param swingOnZ true → Z-axis rotation (side blade up/down), false → X-axis (front/rear)
     */
    private static void submitBlade(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            HarvesterState state,
            BlockStateModelPart model,
            float[] origin,
            float swingRad,
            boolean swingOnZ) {

        if (model == null) return;

        poseStack.pushPose();
        poseStack.translate( origin[0],  origin[1],  origin[2]);
        if (swingOnZ) {
            poseStack.mulPose(SWING_QUAT.rotationZ(swingRad));
        } else {
            poseStack.mulPose(SWING_QUAT.rotationX(swingRad));
        }
        poseStack.translate(-origin[0], -origin[1], -origin[2]);

        collector.submitBlockModel(
                poseStack,
                Sheets.cutoutBlockSheet(),
                List.of(model),
                new int[0],
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0);

        poseStack.popPose();
    }

    // ── Helpers — head look-around ────────────────────────────────────────────────

    /** Returns how many degrees the block's facing is offset from SOUTH (0=south, 90=west, …). */
    private static float facingOffsetDeg(Direction facing) {
        return switch (facing) {
            case WEST  ->  90f;
            case NORTH -> 180f;
            case EAST  -> -90f;
            default    ->   0f; // SOUTH
        };
    }

    /**
     * Picks a new look target and stare duration, writing results into {@code state}.
     * If a living entity is within {@value #LOOK_RANGE} blocks the head tracks it;
     * otherwise it picks a random direction from {@link #LOOK_TARGETS_DEG}.
     */
    private static void pickNewTarget(LookState ls, long gameTime, Level level,
                                      BlockPos pos, float facingOffsetDeg) {
        double cx = pos.getX() + 0.5, cy = pos.getY() + HEAD_Y, cz = pos.getZ() + 0.5;

        List<LivingEntity> nearby = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(cx - LOOK_RANGE, cy - LOOK_RANGE, cz - LOOK_RANGE,
                         cx + LOOK_RANGE, cy + LOOK_RANGE, cz + LOOK_RANGE));

        long h = gameTime * 7919L
                ^ (long) pos.getX() * 374761393L
                ^ (long) pos.getZ() * 668265263L;
        h ^= h >>> 17;
        h ^= h >>> 31;

        if (!nearby.isEmpty()) {
            LivingEntity target = nearby.get((int) Math.floorMod(h, nearby.size()));
            double dx    = target.getX() - cx;
            double dy    = target.getEyeY() - cy;
            double dz    = target.getZ() - cz;
            double hDist = Math.sqrt(dx * dx + dz * dz);
            float worldYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            ls.targetYawDeg   = worldYaw - facingOffsetDeg;
            ls.targetPitchDeg = Math.min(MAX_PITCH_DOWN_DEG, (float) -Math.toDegrees(
                    Math.atan2(dy, Math.max(hDist, 0.01))));
            ls.lookingAtPlayer = (target instanceof Player);
        } else {
            int idx = (int)(Math.abs(h) % LOOK_TARGETS_DEG.length);
            ls.targetYawDeg   = LOOK_TARGETS_DEG[idx] - facingOffsetDeg;
            ls.targetPitchDeg = HEAD_POWERED_PITCH_DEG;
            ls.lookingAtPlayer = false;
        }

        long h2 = h ^ 0xDEADBEEFL;
        h2 ^= h2 >>> 13;
        int stare = STARE_MIN_TICKS + (int)(Math.abs(h2) % (STARE_MAX_TICKS - STARE_MIN_TICKS));
        if (!ls.lookingAtPlayer) stare /= 2;
        ls.stareUntilTick = gameTime + LOOK_TRANSITION_TICKS + stare;
    }

    /**
     * Linearly interpolates between two angles (degrees), always taking the
     * shortest arc so the head never spins the long way around.
     */
    private static float lerpAngleDeg(float from, float to, float t) {
        float diff = ((to - from + 540f) % 360f) - 180f;
        return from + diff * t;
    }

}
