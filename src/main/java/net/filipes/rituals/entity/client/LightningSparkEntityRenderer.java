package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.LightningSparkEntity;
import net.filipes.rituals.entity.custom.LightningTrailEntity;
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

public class LightningSparkEntityRenderer
        extends EntityRenderer<LightningSparkEntity, LightningSparkEntityRenderer.TrailRenderState> {

    public static class TrailRenderState extends EntityRenderState {
        float relX, relY, relZ;
        float offX, offY, offZ;
        int   frame;
        float cameraYaw;
        boolean flipped;
        float scale = 1.0f;
    }


    private static final RenderType[] RENDER_TYPES = new RenderType[LightningSparkEntity.FRAME_COUNT];
    static {
        for (int i = 0; i < LightningSparkEntity.FRAME_COUNT; i++) {
            Identifier tex = Identifier.fromNamespaceAndPath(
                    "rituals", "textures/particle/lightning_spark_" + i + ".png");
            RENDER_TYPES[i] = RenderTypes.entityTranslucentEmissive(tex);
        }
    }

    public LightningSparkEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public TrailRenderState createRenderState() { return new TrailRenderState(); }

    @Override
    public void extractRenderState(LightningSparkEntity e, TrailRenderState s, float pt) {
        super.extractRenderState(e, s, pt);
        double eX = e.xo + (e.getX() - e.xo) * pt;
        double eY = e.yo + (e.getY() - e.yo) * pt;
        double eZ = e.zo + (e.getZ() - e.zo) * pt;
        s.offX = e.getRenderOffsetX();
        s.offY = e.getRenderOffsetY();
        s.offZ = e.getRenderOffsetZ();
        s.flipped = e.isRenderFlipped();
        s.relX = (float)(eX - s.x);
        s.relY = (float)(eY - s.y);
        s.relZ = (float)(eZ - s.z);
        s.frame      = e.getCurrentFrame();
        s.cameraYaw  = e.level().getLevelData() != null
                ? net.minecraft.client.Minecraft.getInstance().gameRenderer.mainCamera().yRot()
                : 0f;
        s.scale = e.getEntityScale();
    }

    @Override public boolean affectedByCulling(LightningSparkEntity e) { return false; }
    @Override protected float getShadowRadius  (TrailRenderState s)    { return 0f; }
    @Override protected float getShadowStrength(TrailRenderState s)    { return 0f; }

    @Override
    public void submit(TrailRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

        final float half = LightningSparkEntity.QUAD_SIZE * 0.5f;
        boolean flip = s.flipped;
        final RenderType rt = RENDER_TYPES[s.frame];

        ps.pushPose();
        ps.translate(
                s.relX + s.offX,
                s.relY + s.offY,
                s.relZ + s.offZ
        );
        ps.mulPose(Axis.YP.rotationDegrees(-s.cameraYaw));
        if (s.scale != 1.0f) ps.scale(s.scale, s.scale, s.scale);

        snc.submitCustomGeometry(ps, rt, (pose, v) -> {

            if (!flip) {
                tv(pose, v, -half, -half, 0f, 0f, 1f);
                tv(pose, v,  half, -half, 0f, 1f, 1f);
                tv(pose, v,  half,  half, 0f, 1f, 0f);
                tv(pose, v, -half,  half, 0f, 0f, 0f);

                tv(pose, v, -half,  half, 0f, 0f, 0f);
                tv(pose, v,  half,  half, 0f, 1f, 0f);
                tv(pose, v,  half, -half, 0f, 1f, 1f);
                tv(pose, v, -half, -half, 0f, 0f, 1f);
            } else {
                tv(pose, v, -half, -half, 0f, 1f, 1f);
                tv(pose, v,  half, -half, 0f, 0f, 1f);
                tv(pose, v,  half,  half, 0f, 0f, 0f);
                tv(pose, v, -half,  half, 0f, 1f, 0f);

                tv(pose, v, -half,  half, 0f, 1f, 0f);
                tv(pose, v,  half,  half, 0f, 0f, 0f);
                tv(pose, v,  half, -half, 0f, 0f, 1f);
                tv(pose, v, -half, -half, 0f, 1f, 1f);
            }
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
                .setNormal(pose, 0f, 1f, 0f);
    }
}