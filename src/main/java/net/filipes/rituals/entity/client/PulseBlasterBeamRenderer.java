package net.filipes.rituals.entity.client;

import net.filipes.rituals.entity.custom.PulseBlasterBeamEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class PulseBlasterBeamRenderer
        extends EntityRenderer<PulseBlasterBeamEntity, PulseBlasterBeamRenderState> {

    private static final Identifier TEXTURE =
            Identifier.of("rituals", "textures/entity/pulse_blaster_beam.png");

    // The model's visual centre sits at Y ≈ 1.266 blocks above the entity's
    // feet (bone origin 24px = 1.5 b, cuboid centre −3.75 px = −0.234 b).
    private static final float MODEL_CENTER_Y = 1.266f;

    private final PulseBlasterBeamModel model;

    public PulseBlasterBeamRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new PulseBlasterBeamModel(
                context.getPart(PulseBlasterBeamModel.LAYER)
        );
    }

    @Override
    public PulseBlasterBeamRenderState createRenderState() {
        return new PulseBlasterBeamRenderState();
    }

    @Override
    public void updateRenderState(
            PulseBlasterBeamEntity entity,
            PulseBlasterBeamRenderState state,
            float tickProgress
    ) {
        super.updateRenderState(entity, state, tickProgress);

        net.minecraft.util.math.Vec3d vel = entity.getVelocity();
        double lenSq = vel.x * vel.x + vel.y * vel.y + vel.z * vel.z;

        if (lenSq > 0.0001) {
            state.hasVelocity = true;

            // Standard Minecraft yaw: 0 = south (+Z), 90 = west (−X)
            state.yaw = (float) Math.toDegrees(Math.atan2(-vel.x, vel.z));

            // Positive pitch = nose down (matches MC convention)
            state.pitch = (float) Math.toDegrees(
                    Math.atan2(-vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z))
            );
        } else {
            state.hasVelocity = false;
        }
    }

    @Override
    public void render(
            PulseBlasterBeamRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState
    ) {
        if (!state.hasVelocity) return;

        matrices.push();

        // ── Orientation ──────────────────────────────────────────────────────
        // The model is a vertical "I" (long axis = +Y).
        // Goal: make that long axis align with the velocity vector.
        //
        // Step 1 — yaw: rotate the +Z ("south") reference axis to the
        //   horizontal component of the velocity.  Because the model will face
        //   +Z after step 3, we negate the yaw so that yaw=−90° (east) becomes
        //   a +90° rotation that swings +Z toward +X.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-state.yaw));

        // Step 2 — pitch: tilt up or down along the now-correct horizontal axis.
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(state.pitch));

        // Step 3 — base orientation: tip the upright model forward so its
        //   long axis (+Y) becomes +Z (pointing in the travel direction).
        //   +90° (not −90°) ensures the tip ends up at +Z, not −Z.
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));

        // Step 4 — centering: cancel the model's built-in Y offset so the
        //   visual centre sits exactly on the entity position.
        //   This translate is applied in model-space (before all rotations),
        //   which is why it belongs at the END of the matrix chain.
        //   If the beam appears offset, tweak MODEL_CENTER_Y above.
        matrices.translate(0f, -MODEL_CENTER_Y, 0f);

        queue.submitModelPart(
                this.model.getBone(),
                matrices,
                RenderLayers.entityTranslucentEmissive(TEXTURE),
                state.light,
                OverlayTexture.DEFAULT_UV,
                null
        );

        matrices.pop();
    }
}