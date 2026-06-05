package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.SolarBladeChargeTracker;
import net.filipes.rituals.entity.custom.SolarStarEntity;
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
import net.minecraft.world.entity.Entity;

public class SolarStarEntityRenderer
        extends EntityRenderer<SolarStarEntity, SolarStarEntityRenderer.StarRenderState> {

    public static class StarRenderState extends EntityRenderState {
        float ownerOffsetX, ownerOffsetY, ownerOffsetZ;
        boolean hasOwner;
        float rotation;
        float scale;
        float flare;
        int   alpha;
    }

    private static final Identifier STAR_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/misc/solar_star.png");
    private static final RenderType RENDER_TYPE  = RenderTypes.eyes(STAR_TEXTURE);
    private static final float RADIUS            = 1.3f;
    private static final int   EXPAND_TICKS      = 12;

    public SolarStarEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public StarRenderState createRenderState() { return new StarRenderState(); }

    @Override
    public void extractRenderState(SolarStarEntity e, StarRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        if (e.ownerEntity != null) {
            Entity owner = e.ownerEntity;

            double ownerX = owner.xo + (owner.getX() - owner.xo) * pt;
            double ownerY = owner.yo + (owner.getY() - owner.yo) * pt;
            double ownerZ = owner.zo + (owner.getZ() - owner.zo) * pt;

            double entityX = e.xo + (e.getX() - e.xo) * pt;
            double entityY = e.yo + (e.getY() - e.yo) * pt;
            double entityZ = e.zo + (e.getZ() - e.zo) * pt;

            s.ownerOffsetX = (float)(ownerX - entityX);
            s.ownerOffsetY = (float)(ownerY - entityY);
            s.ownerOffsetZ = (float)(ownerZ - entityZ);
            s.hasOwner = true;
        } else {
            s.hasOwner = false;
        }

        float tick = e.tickCount + pt;
        s.rotation = tick * 3.5f;
        s.flare    = e.getFlare();

        float pulse    = 0.75f + 0.25f * (float) Math.sin(tick * 0.12f);
        int baseAlpha  = (int)(pulse * 220);

        int remaining  = Math.max(0, SolarBladeChargeTracker.DURATION_TICKS - e.tickCount);
        float scaleFactor;
        if (tick < EXPAND_TICKS) {
            float t = tick / EXPAND_TICKS;
            scaleFactor = 1f - (float) Math.pow(1f - t, 3f);
        } else if (remaining < EXPAND_TICKS) {
            float t = remaining / (float) EXPAND_TICKS;
            scaleFactor = 1f - (float) Math.pow(1f - t, 3f);
        } else {
            scaleFactor = 1f;
        }

        s.scale = scaleFactor + s.flare * 0.45f;
        float normalAlpha = baseAlpha * scaleFactor;
        float flareBoost  = s.flare * 255f;
        s.alpha = (int) Math.min(255f, normalAlpha + flareBoost);
    }

    @Override public boolean affectedByCulling(SolarStarEntity e) { return false; }
    @Override protected float getShadowRadius  (StarRenderState s) { return 0f; }
    @Override protected float getShadowStrength(StarRenderState s) { return 0f; }

    @Override
    public void submit(StarRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {
        if (!s.hasOwner) return;

        float h = RADIUS * s.scale;

        ps.pushPose();
        ps.translate(s.ownerOffsetX, s.ownerOffsetY + 0.08f, s.ownerOffsetZ);
        ps.mulPose(Axis.YP.rotationDegrees(s.rotation));

        snc.submitCustomGeometry(ps, RENDER_TYPE, (pose, v) -> {
            vertex(pose, v, -h, 0, -h, 0f, 0f, s.alpha);
            vertex(pose, v,  h, 0, -h, 1f, 0f, s.alpha);
            vertex(pose, v,  h, 0,  h, 1f, 1f, s.alpha);
            vertex(pose, v, -h, 0,  h, 0f, 1f, s.alpha);

            vertex(pose, v, -h, 0,  h, 0f, 1f, s.alpha);
            vertex(pose, v,  h, 0,  h, 1f, 1f, s.alpha);
            vertex(pose, v,  h, 0, -h, 1f, 0f, s.alpha);
            vertex(pose, v, -h, 0, -h, 0f, 0f, s.alpha);
        });

        ps.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer v,
                               float x, float y, float z,
                               float u, float vv, int alpha) {
        v.addVertex(pose, x, y, z)
                .setColor(255, 200, 80, alpha)
                .setUv(u, vv)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0f, 1f, 0f);
    }
}