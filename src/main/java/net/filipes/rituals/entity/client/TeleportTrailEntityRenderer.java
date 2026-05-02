package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.TeleportTrailEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class TeleportTrailEntityRenderer extends EntityRenderer<TeleportTrailEntity, TeleportTrailEntityRenderer.TeleportTrailRenderState> {

    private static final float BOLT_SEG_FADE = 1.0f;
    private static final float BOLT_SEG_STEP  = 0.35f;

    public static class TeleportTrailRenderState extends EntityRenderState {
        public float endRelX, endRelY, endRelZ;
        public float alpha;
        public float age;
        public long  seed;
    }

    public TeleportTrailEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override public TeleportTrailRenderState createRenderState() { return new TeleportTrailRenderState(); }

    @Override
    public void extractRenderState(TeleportTrailEntity e, TeleportTrailRenderState s, float pt) {
        super.extractRenderState(e, s, pt);
        s.endRelX = e.getEndX() - (float) s.x;
        s.endRelY = e.getEndY() - (float) s.y;
        s.endRelZ = e.getEndZ() - (float) s.z;
        s.alpha   = e.getAlpha(pt);
        s.age     = e.tickCount - 1 + pt;
        s.seed    = e.getId() * 0x9E3779B97F4A7C15L;
    }

    @Override public boolean affectedByCulling(TeleportTrailEntity e) { return false; }
    @Override protected float getShadowRadius(TeleportTrailRenderState s) { return 0f; }
    @Override protected float getShadowStrength(TeleportTrailRenderState s) { return 0f; }

    @Override
    public void submit(TeleportTrailRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {
        if (s.alpha < 0.01f) return;

        float HEIGHT = 1.0f;
        Vec3 from = new Vec3(0,        HEIGHT, 0);
        Vec3 to   = new Vec3(s.endRelX, HEIGHT + s.endRelY, s.endRelZ);

        Vec3 raw = to.subtract(from);
        if (raw.lengthSqr() < 1e-6) return;

        Vec3  dir    = raw.normalize();
        float len    = (float) raw.length();
        Vec3  helper = Math.abs(dir.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3  right  = dir.cross(helper).normalize();
        Vec3  up     = right.cross(dir).normalize();

        int baseAlpha = (int)(s.alpha * 255);

        long animSeed = s.seed ^ ((long)(s.age / 3f) * 0x6C62272E07BB0142L);

        snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                (pose, v) -> drawBolt(pose, v, from, dir, right, up, len,
                        0.05f, 255, 255, 255, baseAlpha, animSeed, s.age));

        snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                (pose, v) -> drawBolt(pose, v, from, dir, right, up, len,
                        0.13f, 80, 210, 255, (int)(baseAlpha * 0.85f), animSeed ^ 0xAAAA_AAAAL, s.age));

        snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                (pose, v) -> drawBolt(pose, v, from, dir, right, up, len,
                        0.26f, 20, 90, 255, (int)(baseAlpha * 0.50f), animSeed ^ 0xBBBB_BBBBL, s.age));

        snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                (pose, v) -> drawBolt(pose, v, from, dir, right, up, len,
                        0.50f, 5, 20, 160, (int)(baseAlpha * 0.22f), animSeed ^ 0xCCCC_CCCCL, s.age));

        long sparkAnimSeed = s.seed ^ ((long)(s.age / 1.5f) * 0x6C62272E07BB0142L);
        for (int i = 0; i < 6; i++) {
            long  bSeed     = sparkAnimSeed ^ (i * 0x6C62272E07BB0142L);
            float tOffset   = hash(bSeed)       * 0.5f + 0.5f;
            float sparkLen  = 0.6f + (hash(bSeed + 1L) * 0.5f + 0.5f) * 2.2f;
            float angle     = hash(bSeed + 2L)  * (float) Math.PI;
            Vec3  sparkDir  = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vec3  sparkOrig = from.add(dir.scale(len * tOffset));
            long  sf        = bSeed;

            snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                    (pose, v) -> drawBolt(pose, v, sparkOrig, sparkDir, right, up, sparkLen,
                            0.04f, 140, 230, 255, (int)(baseAlpha * 0.75f), sf, s.age));
        }

        drawHalo(snc, ps, from, s.alpha, s.age, s.seed);
        drawHalo(snc, ps, to,   s.alpha, s.age, s.seed ^ 0x1234_5678L);
    }

    private static void drawBolt(PoseStack.Pose pose, VertexConsumer v,
                                 Vec3 origin, Vec3 dir, Vec3 right, Vec3 up,
                                 float length, float width,
                                 int r, int g, int b, int al, long seed, float age) {
        if (length < 0.05f || al < 4) return;

        int knots = Math.max(2, (int)(length / 0.9f));

        Vec3[] pts = new Vec3[knots + 2];
        pts[0]         = origin;
        pts[knots + 1] = origin.add(dir.scale(length));

        for (int i = 1; i <= knots; i++) {
            float t      = i / (float)(knots + 1);
            float env    = (float) Math.sin(t * Math.PI);
            float maxOff = Math.min(length * 0.14f, 0.45f);
            float offR   = hash(seed + i * 997L)  * env * maxOff;
            float offU   = hash(seed + i * 1009L) * env * maxOff;
            pts[i] = origin.add(dir.scale(length * t))
                    .add(right.scale(offR))
                    .add(up.scale(offU));
        }

        int totalSegs = pts.length - 1;
        float totalAliveTime = totalSegs * BOLT_SEG_STEP + BOLT_SEG_FADE;

        for (int i = 0; i < totalSegs; i++) {
            float segAge = age - (i * BOLT_SEG_STEP);

            if (segAge < 0f) continue;
            if (segAge > totalAliveTime) continue;

            float alpha;
            if (segAge < BOLT_SEG_FADE) {
                alpha = segAge / BOLT_SEG_FADE;
            } else if (segAge > totalAliveTime - BOLT_SEG_FADE) {
                alpha = (totalAliveTime - segAge) / BOLT_SEG_FADE;
            } else {
                alpha = 1.0f;
            }

            alpha = net.minecraft.util.Mth.clamp(alpha, 0f, 1f);
            int segAlpha = (int)(al * alpha);
            if (segAlpha < 4) continue;

            crossQuad(pose, v, pts[i], pts[i + 1], right, width, r, g, b, segAlpha);
            crossQuad(pose, v, pts[i], pts[i + 1], up,    width, r, g, b, segAlpha);
        }
    }

    private static void drawHalo(SubmitNodeCollector snc, PoseStack ps,
                                 Vec3 center, float alpha, float age, long seed) {
        int al = (int)(alpha * 180);
        if (al < 4) return;

        drawHaloRing(snc, ps, center, 0.55f, al,     180, 230, 255, age, seed);
        drawHaloRing(snc, ps, center, 0.80f, al / 2, 60,  140, 255, age, seed ^ 0xDEAD);
    }

    private static void drawHaloRing(SubmitNodeCollector snc, PoseStack ps,
                                     Vec3 center, float baseRadius, int al,
                                     int r, int g, int b, float age, long seed) {
        snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) -> {
            float radius   = baseRadius + (float) Math.sin(age * 1.3f + seed * 0.001f) * 0.08f;
            int   segments = 20;
            Vec3  ringR    = new Vec3(1, 0, 0);
            Vec3  ringFwd  = new Vec3(0, 0, 1);

            for (int i = 0; i < segments; i++) {
                float a1 = (float)(i       / (double) segments * Math.PI * 2);
                float a2 = (float)((i + 1) / (double) segments * Math.PI * 2);
                Vec3 p1 = center.add(ringR.scale(Math.cos(a1) * radius)).add(ringFwd.scale(Math.sin(a1) * radius));
                Vec3 p2 = center.add(ringR.scale(Math.cos(a2) * radius)).add(ringFwd.scale(Math.sin(a2) * radius));
                vShape(pose, v, p1, p2, ringR, new Vec3(0, 1, 0), 0.03f, r, g, b, al);
            }
        });
    }

    private static void crossQuad(PoseStack.Pose pose, VertexConsumer v,
                                  Vec3 a, Vec3 b, Vec3 axis, float w,
                                  int r, int g, int bl, int al) {
        float h   = w * 0.5f;
        Vec3  off = axis.scale(h);
        Vec3  a0  = a.add(off), a1 = a.subtract(off);
        Vec3  b0  = b.add(off), b1 = b.subtract(off);
        bv(pose, v, a0, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, b0, r, g, bl, al);
        bv(pose, v, b0, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, a0, r, g, bl, al);
    }

    private static void vShape(PoseStack.Pose pose, VertexConsumer v,
                               Vec3 a, Vec3 b, Vec3 right, Vec3 up, float w,
                               int r, int g, int bl, int al) {
        Vec3 lw = right.scale(-w).add(up.scale(w));
        Vec3 rw = right.scale( w).add(up.scale(w));

        bv(pose, v, a,          r, g, bl, al);
        bv(pose, v, a.add(lw),  r, g, bl, al);
        bv(pose, v, b.add(lw),  r, g, bl, al);
        bv(pose, v, b,          r, g, bl, al);

        bv(pose, v, b,          r, g, bl, al);
        bv(pose, v, b.add(lw),  r, g, bl, al);
        bv(pose, v, a.add(lw),  r, g, bl, al);
        bv(pose, v, a,          r, g, bl, al);

        bv(pose, v, a,          r, g, bl, al);
        bv(pose, v, a.add(rw),  r, g, bl, al);
        bv(pose, v, b.add(rw),  r, g, bl, al);
        bv(pose, v, b,          r, g, bl, al);

        bv(pose, v, b,          r, g, bl, al);
        bv(pose, v, b.add(rw),  r, g, bl, al);
        bv(pose, v, a.add(rw),  r, g, bl, al);
        bv(pose, v, a,          r, g, bl, al);
    }

    private static void bv(PoseStack.Pose pose, VertexConsumer v,
                           Vec3 p, int r, int g, int b, int a) {
        v.addVertex(pose, (float) p.x, (float) p.y, (float) p.z).setColor(r, g, b, a);
    }

    private static float hash(long seed) {
        seed ^= (seed >>> 30);
        seed *= 0xBF58476D1CE4E5B9L;
        seed ^= (seed >>> 27);
        seed *= 0x94D049BB133111EBL;
        seed ^= (seed >>> 31);
        return (seed & Long.MAX_VALUE) / (float) Long.MAX_VALUE * 2f - 1f;
    }
}