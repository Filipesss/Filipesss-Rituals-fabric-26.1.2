package net.filipes.rituals.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * ModelPart-based gun model for the Pulse Blaster item.
 *
 * Structure:
 *   root
 *     └── gun            (static frame + grip)
 *           ├── barrel   (static front tube)
 *           └── cylinder (ROTATES — this is the spinning drum)
 *
 * The cylinder's pivot sits on the barrel axis so it spins in place.
 * Adjust the cuboid coordinates to match your Blockbench model.
 * Texture atlas: 64×32 px  (see UVs below).
 */
public class PulseBlasterGunModel {

    public static final EntityModelLayer LAYER = new EntityModelLayer(
            Identifier.of("rituals", "pulse_blaster_gun"), "main");

    private static final Identifier TEXTURE =
            Identifier.of("rituals", "textures/item/pulse_blaster.png");

    private final ModelPart gun;
    private final ModelPart cylinder;

    public PulseBlasterGunModel(ModelPart root) {
        this.gun      = root.getChild("gun");
        this.cylinder = this.gun.getChild("cylinder");
    }

    // ── Static model data (called once during model baking) ───────────────────

    public static TexturedModelData getTexturedModelData() {
        ModelData     modelData = new ModelData();
        ModelPartData rootData  = modelData.getRoot();

        /*
         * "gun" — the static body of the blaster.
         *
         * Two cuboids baked into one part:
         *   • Main body:  8 wide × 4 tall × 12 deep  (horizontal barrel housing)
         *   • Grip/handle: 4 wide × 8 tall × 3 deep  (hangs below the body)
         *
         * UV(0, 0)  → body faces  (needs ≈48 × 20 px in your texture)
         * UV(0, 20) → grip faces  (needs ≈14 × 14 px)
         */
        ModelPartData gun = rootData.addChild("gun",
                ModelPartBuilder.create()
                        .uv(0,  0).cuboid(-4f, -2f, -8f,  8, 4, 12)   // main housing
                        .uv(0, 20).cuboid(-2f,  2f, -6f,  4, 8,  3),  // grip
                ModelTransform.NONE);

        /*
         * "barrel" — static front barrel tube.
         *
         * Pivot at (0, 0, 4) places its origin at the front face of the body,
         * then the cuboid extends 8 px forward from there.
         *
         * UV(40, 0) → barrel faces
         */
        gun.addChild("barrel",
                ModelPartBuilder.create()
                        .uv(40, 0).cuboid(-1f, -1f, 0f, 2, 2, 8),
                ModelTransform.origin(0f, 0f, 4f));

        /*
         * "cylinder" — THE ROTATING DRUM.
         *
         * Pivot at (0, 0, 2) in the gun's local space positions the drum's
         * rotation axis right where a revolver cylinder would sit.
         * The cuboid is centred on (0,0,0) in the cylinder's own space so
         * it spins around its own centre when we set cylinder.roll.
         *
         *   Local cuboid:  (-3, -3, -4)  size  (6, 6, 8)
         *   Centre check:  -3+3=0, -3+3=0, -4+4=0  ✓
         *
         * UV(24, 0) → drum faces  (needs ≈28 × 20 px in your texture)
         *
         * NOTE: If your Blockbench model names the cylinder group differently,
         * rename the string "cylinder" here and in the constructor above, and
         * adjust the cuboid geometry to match your exported values.
         */
        gun.addChild("cylinder",
                ModelPartBuilder.create()
                        .uv(24, 0).cuboid(-3f, -3f, -4f, 6, 6, 8),
                ModelTransform.origin(0f, 0f, 2f));

        return TexturedModelData.of(modelData, 64, 32);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    /**
     * Sets the cylinder roll angle then submits each part to the queue.
     *
     * @param cylinderAngle angle in radians (from {@link PulseBlasterCylinderState#getAngle()})
     */
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue,
                       int light, int overlay, float cylinderAngle) {
        // roll = rotation around the Z axis, which is the barrel's longitudinal axis
        cylinder.roll = cylinderAngle;

        // Submit gun body (includes barrel as child, but NOT cylinder — we render it separately)
        // Actually ModelPart.render() renders all children too, so just submit the root
        queue.submitModelPart(
                gun,
                matrices,
                RenderLayers.entityTranslucentEmissive(TEXTURE),
                light,
                overlay,
                null
        );
    }
}