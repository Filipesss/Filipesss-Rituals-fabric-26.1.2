package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.TemporalSlowZoneGroundEntity;
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

import java.util.ArrayList;
import java.util.List;

public class TemporalSlowZoneGroundEntityRenderer
        extends EntityRenderer<TemporalSlowZoneGroundEntity, TemporalSlowZoneGroundEntityRenderer.ZoneRenderState> {

    public static class ZoneRenderState extends EntityRenderState {
        float radius;
        int frameIndex;
        int alpha;
    }

    private static final int[] ANGLES = {30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330, 360};
    private static final int   LOOPS  = 4;
    private static final List<RenderType> FRAMES = buildFrames();
    private static final int   TICKS_PER_FRAME = 2;

    private static List<RenderType> buildFrames() {
        List<RenderType> frames = new ArrayList<>();
        frames.add(frameType("tz_0_0"));
        for (int loop = 0; loop < LOOPS; loop++) {
            for (int angle : ANGLES) {
                frames.add(frameType("tz_" + angle + "_" + loop));
            }
        }
        return frames;
    }

    private static RenderType frameType(String name) {

        return RenderTypes.eyes(Identifier.fromNamespaceAndPath("rituals", "textures/entity/" + name + ".png"));
    }

    public TemporalSlowZoneGroundEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public ZoneRenderState createRenderState() { return new ZoneRenderState(); }

    @Override
    public void extractRenderState(TemporalSlowZoneGroundEntity e, ZoneRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        s.radius = e.getRadius();

        int frame = e.tickCount / TICKS_PER_FRAME;
        s.frameIndex = Math.min(frame, FRAMES.size() - 1);

        int fadeTicks = 10;
        int remaining = e.getDurationTicks() - e.tickCount;
        float fadeOut = remaining < fadeTicks ? remaining / (float) fadeTicks : 1f;
        float fadeIn  = e.tickCount < fadeTicks ? e.tickCount / (float) fadeTicks : 1f;
        s.alpha = (int) (255 * Math.min(fadeOut, fadeIn));
    }

    @Override public boolean affectedByCulling(TemporalSlowZoneGroundEntity e) { return false; }
    @Override protected float getShadowRadius  (ZoneRenderState s) { return 0f; }
    @Override protected float getShadowStrength(ZoneRenderState s) { return 0f; }

    @Override
    public void submit(ZoneRenderState s, PoseStack ps, SubmitNodeCollector snc, CameraRenderState cam) {
        float h = s.radius;
        RenderType type = FRAMES.get(s.frameIndex);

        ps.pushPose();
        ps.translate(0, 0.02f, 0);

        snc.submitCustomGeometry(ps, type, (pose, v) -> {

            vertex(pose, v, -h, 0, -h, 0f, 1f, s.alpha);
            vertex(pose, v,  h, 0, -h, 1f, 1f, s.alpha);
            vertex(pose, v,  h, 0,  h, 1f, 0f, s.alpha);
            vertex(pose, v, -h, 0,  h, 0f, 0f, s.alpha);

            vertex(pose, v, -h, 0,  h, 0f, 0f, s.alpha);
            vertex(pose, v,  h, 0,  h, 1f, 0f, s.alpha);
            vertex(pose, v,  h, 0, -h, 1f, 1f, s.alpha);
            vertex(pose, v, -h, 0, -h, 0f, 1f, s.alpha);
        });

        ps.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer v,
                               float x, float y, float z, float u, float vv, int alpha) {
        v.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, vv)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0f, 1f, 0f);
    }
}