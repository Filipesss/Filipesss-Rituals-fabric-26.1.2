package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.PulseBlasterBeamEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class PulseBlasterBeamRenderer
        extends EntityRenderer<PulseBlasterBeamEntity, PulseBlasterBeamRenderer.BeamRenderState> {

    private static final float LENGTH      = 0.5f;
    private static final float SIDE_LENGTH = LENGTH * (6f / 8f);
    private static final float CENTER_HALF = 0.035f;
    private static final float TOTAL_HALF  = 0.065f;

    private static final int YR = 255, YG = 150, YB = 46,  YA = 210;

    private static final int RR = 255, RG = 50,  RB = 50,   RA = 200;

    public static class BeamRenderState extends EntityRenderState {
        public boolean hasVelocity = false;
        public float yaw   = 0f;
        public float pitch = 0f;
    }

    public PulseBlasterBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override public BeamRenderState createRenderState() { return new BeamRenderState(); }

    @Override
    public void extractRenderState(PulseBlasterBeamEntity entity,
                                   BeamRenderState state,
                                   float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Vec3 vel = entity.getDeltaMovement();
        double lenSq = vel.x * vel.x + vel.y * vel.y + vel.z * vel.z;
        if (lenSq > 0.0001) {
            state.hasVelocity = true;
            state.yaw   = (float) Math.toDegrees(Math.atan2(-vel.x, vel.z));
            state.pitch = (float) Math.toDegrees(
                    Math.atan2(-vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z)));
        } else {
            state.hasVelocity = false;
        }
    }

    @Override
    public void submit(BeamRenderState state, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState camera) {
        if (!state.hasVelocity) return;

        ps.pushPose();

        ps.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        ps.mulPose(Axis.XP.rotationDegrees(state.pitch));
        ps.mulPose(Axis.XP.rotationDegrees(90.0f));

        ps.translate(0f, -LENGTH * 0.5f, 0f);

        snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                (pose, v) -> drawPlane(pose, v));

        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(90f));
        snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                (pose, v) -> drawPlane(pose, v));
        ps.popPose();

        ps.popPose();
    }

    private static void drawPlane(PoseStack.Pose pose, VertexConsumer v) {
        float yTop     = LENGTH * 0.5f;
        float yBot     = -LENGTH * 0.5f;
        float sideTop  = SIDE_LENGTH * 0.5f;
        float sideBot  = -SIDE_LENGTH * 0.5f;

        quad(pose, v,
                -TOTAL_HALF, sideTop, 0,
                -CENTER_HALF, sideTop, 0,
                -CENTER_HALF, sideBot, 0,
                -TOTAL_HALF,  sideBot, 0,
                RR, RG, RB, RA);

        quad(pose, v,
                -CENTER_HALF, yTop, 0,
                CENTER_HALF, yTop, 0,
                CENTER_HALF, yBot, 0,
                -CENTER_HALF, yBot, 0,
                YR, YG, YB, YA);

        quad(pose, v,
                CENTER_HALF, sideTop, 0,
                TOTAL_HALF,  sideTop, 0,
                TOTAL_HALF,  sideBot, 0,
                CENTER_HALF, sideBot, 0,
                RR, RG, RB, RA);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer v,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             int r, int g, int b, int a) {
        bv(pose, v, x0, y0, z0, r, g, b, a);
        bv(pose, v, x1, y1, z1, r, g, b, a);
        bv(pose, v, x2, y2, z2, r, g, b, a);
        bv(pose, v, x3, y3, z3, r, g, b, a);

        bv(pose, v, x3, y3, z3, r, g, b, a);
        bv(pose, v, x2, y2, z2, r, g, b, a);
        bv(pose, v, x1, y1, z1, r, g, b, a);
        bv(pose, v, x0, y0, z0, r, g, b, a);
    }

    private static void bv(PoseStack.Pose pose, VertexConsumer v,
                           float x, float y, float z, int r, int g, int b, int a) {
        v.addVertex(pose, x, y, z).setColor(r, g, b, a);
    }

    @Override protected float getShadowRadius  (BeamRenderState s) { return 0f; }
    @Override protected float getShadowStrength(BeamRenderState s) { return 0f; }

}