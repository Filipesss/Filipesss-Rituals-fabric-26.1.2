package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.SolarMarkEntity;
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

public class SolarMarkEntityRenderer
        extends EntityRenderer<SolarMarkEntity, SolarMarkEntityRenderer.SolarRenderState> {

    public static class SolarRenderState extends EntityRenderState {
        float relX, relY, relZ;
        int   frame;
        float cameraYaw;
        float cameraPitch;
        float scale = 1.0f;
    }

    private static final RenderType[] RENDER_TYPES = new RenderType[SolarMarkEntity.FRAME_COUNT];
    static {
        for (int i = 0; i < SolarMarkEntity.FRAME_COUNT; i++) {
            Identifier tex = Identifier.fromNamespaceAndPath(
                    "rituals", "textures/particle/solar_mark_" + i + ".png");
            RENDER_TYPES[i] = RenderTypes.eyes(tex);
        }
    }

    private static final float[] FRAME_SCALES = { 1.0f, 1.25f, 1.5f, 1.25f, 1.0f };

    public SolarMarkEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public SolarRenderState createRenderState() { return new SolarRenderState(); }

    @Override
    public void extractRenderState(SolarMarkEntity e, SolarRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        double eX = e.xo + (e.getX() - e.xo) * pt;
        double eY = e.yo + (e.getY() - e.yo) * pt;
        double eZ = e.zo + (e.getZ() - e.zo) * pt;
        s.relX = (float)(eX - s.x);
        s.relY = (float)(eY - s.y);
        s.relZ = (float)(eZ - s.z);
        s.frame = e.getCurrentFrame();

        s.cameraYaw = e.level().getLevelData() != null
                ? net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().yRot()
                : 0f;
        s.cameraPitch = e.level().getLevelData() != null
                ? net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().xRot()
                : 0f;
        s.scale = e.getEntityScale();
    }

    @Override public boolean affectedByCulling(SolarMarkEntity e) { return false; }
    @Override protected float getShadowRadius  (SolarRenderState s) { return 0f; }
    @Override protected float getShadowStrength(SolarRenderState s) { return 0f; }

    @Override
    public void submit(SolarRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

        final float half = SolarMarkEntity.QUAD_SIZE * 0.5f;
        final RenderType rt = RENDER_TYPES[s.frame];
        float frameScale = s.scale * FRAME_SCALES[s.frame];

        ps.pushPose();
        ps.translate(s.relX, s.relY, s.relZ);
        ps.mulPose(Axis.YP.rotationDegrees(-s.cameraYaw));
        ps.mulPose(Axis.XP.rotationDegrees(s.cameraPitch));
        ps.translate(0.0f, 0.0f, -0.8f);
        if (frameScale != 1.0f) ps.scale(frameScale, frameScale, frameScale);

        snc.submitCustomGeometry(ps, rt, (pose, v) -> {
            tv(pose, v, -half, -half, 0f, 1f, 1f);
            tv(pose, v, half,  -half, 0f, 0f, 1f);
            tv(pose, v, half,   half, 0f, 0f, 0f);
            tv(pose, v, -half,  half, 0f, 1f, 0f);

            tv(pose, v, -half,  half, 0f, 1f, 0f);
            tv(pose, v, half,   half, 0f, 0f, 0f);
            tv(pose, v, half,  -half, 0f, 0f, 1f);
            tv(pose, v, -half, -half, 0f, 1f, 1f);
        });

        ps.popPose();
    }

    private static void tv(PoseStack.Pose pose, VertexConsumer v,
                           float x, float y, float z, float u, float vv) {
        v.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, vv)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0f, 0f, 1f);
    }
}