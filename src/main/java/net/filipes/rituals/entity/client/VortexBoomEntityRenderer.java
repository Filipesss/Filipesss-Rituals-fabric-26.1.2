package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.VortexBoomEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;

public class VortexBoomEntityRenderer
        extends EntityRenderer<VortexBoomEntity, VortexBoomEntityRenderer.BeamRenderState> {

    public static class BeamRenderState extends EntityRenderState {
        float relX, relY, relZ;
        float beamYaw, beamPitch;
        float beamLength;
        int   frame;
        float cameraYaw;
    }

    private static final RenderType[] RENDER_TYPES = new RenderType[VortexBoomEntity.FRAME_COUNT];
    static {
        for (int i = 0; i < VortexBoomEntity.FRAME_COUNT; i++) {
            Identifier tex = Identifier.fromNamespaceAndPath(
                    "rituals", "textures/particle/vortex_boom_" + i + ".png");
            RENDER_TYPES[i] = RenderTypes.entityTranslucentEmissive(tex);
        }
    }

    public VortexBoomEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public BeamRenderState createRenderState() { return new BeamRenderState(); }

    @Override
    public void extractRenderState(VortexBoomEntity e, BeamRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        double eX = e.xo + (e.getX() - e.xo) * pt;
        double eY = e.yo + (e.getY() - e.yo) * pt;
        double eZ = e.zo + (e.getZ() - e.zo) * pt;

        s.relX = (float)(eX - s.x);
        s.relY = (float)(eY - s.y);
        s.relZ = (float)(eZ - s.z);

        s.beamYaw   = e.getBeamYaw();
        s.beamPitch = e.getBeamPitch();
        s.beamLength = e.getBeamLength();
        s.frame     = e.getCurrentFrame();
        s.cameraYaw = Minecraft.getInstance().gameRenderer.mainCamera().yRot();
    }

    @Override public boolean affectedByCulling(VortexBoomEntity e) { return false; }
    @Override protected float getShadowRadius  (BeamRenderState s) { return 0f; }
    @Override protected float getShadowStrength(BeamRenderState s) { return 0f; }

    @Override
    public void submit(BeamRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

        final float half = VortexBoomEntity.QUAD_SIZE * 0.5f;
        final RenderType rt = RENDER_TYPES[s.frame];

        float pitchRad = s.beamPitch * (float)(Math.PI / 180.0f);
        float yawRad   = -s.beamYaw  * (float)(Math.PI / 180.0f);
        float cosYaw   = Mth.cos(yawRad);
        float sinYaw   = Mth.sin(yawRad);
        float cosPitch = Mth.cos(pitchRad);
        float sinPitch = Mth.sin(pitchRad);

        final float dx = sinYaw  * cosPitch;
        final float dy = -sinPitch;
        final float dz = cosYaw  * cosPitch;

        int steps = Math.max(1, (int)(s.beamLength / VortexBoomEntity.STEP_SIZE) + 1);

        for (int i = 0; i < steps; i++) {
            float dist = i * VortexBoomEntity.STEP_SIZE;

            ps.pushPose();
            ps.translate(
                    s.relX + dx * dist,
                    s.relY + dy * dist,
                    s.relZ + dz * dist
            );
            ps.mulPose(Axis.YP.rotationDegrees(-s.cameraYaw));

            snc.submitCustomGeometry(ps, rt, (pose, v) -> {
                tv(pose, v, -half, -half, 0f, 0f, 1f);
                tv(pose, v,  half, -half, 0f, 1f, 1f);
                tv(pose, v,  half,  half, 0f, 1f, 0f);
                tv(pose, v, -half,  half, 0f, 0f, 0f);

                tv(pose, v, -half,  half, 0f, 0f, 0f);
                tv(pose, v,  half,  half, 0f, 1f, 0f);
                tv(pose, v,  half, -half, 0f, 1f, 1f);
                tv(pose, v, -half, -half, 0f, 0f, 1f);
            });

            ps.popPose();
        }
    }

    private static void tv(PoseStack.Pose pose, VertexConsumer v,
                           float x, float y, float z, float u, float vv) {
        v.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, vv)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0f, 1f, 0f);
    }
}