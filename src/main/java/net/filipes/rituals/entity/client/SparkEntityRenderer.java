package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SparkEntityRenderer<T extends SparkEntity> extends EntityRenderer<T, SparkEntityRenderer.SparkRenderState> {

    public static class SparkRenderState extends EntityRenderState {
        final List<Vec3> trail = new ArrayList<>();
        int   r, g, b;
        float width;
        int   peakAlpha;
        int   windowOffset;
        int   windowSize;
        float jitter;
        long  seed;
        int trailAmount;
        float trailSpacing;
        float trailGapChance;
        float trailRotation;
    }

    public SparkEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public SparkRenderState createRenderState() { return new SparkRenderState(); }

    @Override
    public void extractRenderState(T e, SparkRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        double eX = e.xo + (e.getX() - e.xo) * pt;
        double eY = e.yo + (e.getY() - e.yo) * pt;
        double eZ = e.zo + (e.getZ() - e.zo) * pt;

        s.trail.clear();
        s.trail.addAll(e.trailPositions);
        s.trail.add(new Vec3(eX, eY, eZ));

        s.r            = e.trailR;
        s.g            = e.trailG;
        s.b            = e.trailB;
        s.width        = e.trailWidth;
        s.peakAlpha    = e.trailAlpha;
        s.windowOffset = e.trailWindowOffset;
        s.windowSize   = e.windowSize;
        s.jitter       = e.trailJitter;
        s.trailAmount = e.trailAmount;
        s.trailSpacing = e.trailSpacing;
        s.trailGapChance = e.trailGapChance;
        s.trailRotation = e.trailRotation;
        s.seed         = e.getUUID().getMostSignificantBits();
    }

    @Override public boolean affectedByCulling(SparkEntity e) { return false; }
    @Override protected float getShadowRadius  (SparkRenderState s) { return 0f; }
    @Override protected float getShadowStrength(SparkRenderState s) { return 0f; }

    @Override
    public void submit(SparkRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

        List<Vec3> trail = s.trail;
        if (trail.size() < 2) return;

        int numSeg  = trail.size() - 1;
        int winEnd  = Math.min(s.windowOffset, numSeg - 1);
        int winStart= Math.max(0, winEnd - s.windowSize + 1);
        if (winStart > winEnd) return;

        final double camX = s.x, camY = s.y, camZ = s.z;
        final int    total = trail.size();

        Vec3[] basePts = new Vec3[total];
        for (int i = 0; i < total; i++) {
            Vec3 p = trail.get(i);
            basePts[i] = new Vec3(p.x - camX, p.y - camY, p.z - camZ);
        }

        final int   r  = s.r, g = s.g, b = s.b;
        final float w  = s.width;
        final int   al = s.peakAlpha;

        final float COS30 = (float)(Math.sqrt(3.0) / 2.0);
        final float SIN30 = 0.5f;

        Vec3 overall = basePts[winEnd + 1].subtract(basePts[winStart]);
        if (overall.lengthSqr() < 1e-8) overall = new Vec3(0, 1, 0);
        overall = overall.normalize();

        Vec3 oHelper   = (Math.abs(overall.y) > 0.9) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 oRight    = overall.cross(oHelper).normalize();
        Vec3 oUp       = overall.cross(oRight).normalize();
        Vec3 oBisector = oRight.add(oUp).normalize();
        Vec3 oPerp     = oRight.subtract(oUp).normalize();
        final Vec3 baseWingA = oBisector.scale(COS30).add(oPerp.scale(SIN30));
        final Vec3 baseWingB = oBisector.scale(COS30).subtract(oPerp.scale(SIN30));

        final int    trailCount = Math.max(1, s.trailAmount);
        final double angleStep  = (2.0 * Math.PI) / trailCount;
        final Vec3   axis       = overall;

        ps.pushPose();

        for (int t = 0; t < trailCount; t++) {

            Vec3 wingA = rotateAround(baseWingA, axis, angleStep * t);
            Vec3 wingB = rotateAround(baseWingB, axis, angleStep * t);
            Vec3 outward = rotateAround(oBisector, axis, angleStep * t);
            Vec3[] pts = basePts.clone();

            long trailSeed = s.seed ^ (long) t * 0x9E3779B97F4A7C15L;

            for (int i = winStart + 1; i < winEnd + 1 && i < total - 1; i++) {
                Vec3 prev = pts[i - 1];
                Vec3 next = pts[i + 1];

                Vec3 jitterOffset = Vec3.ZERO;

                if (s.jitter > 1e-4f) {
                    Vec3 dir = next.subtract(prev);
                    if (dir.lengthSqr() >= 1e-8) {
                        dir = dir.normalize();
                        Vec3 helper = (Math.abs(dir.y) > 0.9) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
                        Vec3 right  = dir.cross(helper).normalize();
                        Vec3 up     = right.cross(dir).normalize();

                        Vec3  wp      = trail.get(i);
                        long  posSeed = hashPos(wp) ^ trailSeed;
                        float offR    = hash(posSeed + 997L)  * s.jitter;
                        float offU    = hash(posSeed + 1009L) * s.jitter;
                        jitterOffset  = right.scale(offR).add(up.scale(offU));
                    }
                }

                Vec3 spacingOffset = (trailCount > 1) ? outward.scale(s.trailSpacing) : Vec3.ZERO;

                pts[i] = basePts[i].add(jitterOffset).add(spacingOffset);
            }

            if (s.trailSpacing > 1e-4f) {
                Vec3 sp = outward.scale(s.trailSpacing);
                pts[winStart]    = basePts[winStart].add(sp);
                pts[winEnd + 1]  = basePts[winEnd + 1].add(sp);
            }

            for (int i = winStart; i <= winEnd; i++) {

                if (s.trailGapChance > 1e-4f) {
                    long gapSeed = trailSeed ^ ((long) i * 0x6C62272E07BB0142L);
                    float roll = (hash(gapSeed + 31337L) + 1f) * 0.5f;
                    if (roll < s.trailGapChance) continue;
                }

                Vec3 ra = pts[i];
                Vec3 rb = pts[i + 1];
                if (rb.subtract(ra).lengthSqr() < 1e-8) continue;

                double angleStart = s.trailRotation * (i - winStart);
                double angleEnd   = s.trailRotation * (i - winStart + 1);
                Vec3 segWingA_start = (Math.abs(angleStart) > 1e-9) ? rotateAround(wingA, axis, angleStart) : wingA;
                Vec3 segWingB_start = (Math.abs(angleStart) > 1e-9) ? rotateAround(wingB, axis, angleStart) : wingB;
                Vec3 segWingA_end   = rotateAround(wingA, axis, angleEnd);
                Vec3 segWingB_end   = rotateAround(wingB, axis, angleEnd);

                final Vec3  pA = ra, pB = rb;
                final Vec3  pWA0 = segWingA_start, pWA1 = segWingA_end;
                final Vec3  pWB0 = segWingB_start, pWB1 = segWingB_end;
                final int   fr = r, fg = g, fb = b, fal = al;
                final float fw = w;

                snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) -> {
                    vQuad(pose, v, pA, pB, pWA0, pWA1, fw, fr, fg, fb, fal);
                    vQuad(pose, v, pA, pB, pWB0, pWB1, fw, fr, fg, fb, fal);
                });
            }
        }

        ps.popPose();
    }

    private static Vec3 rotateAround(Vec3 v, Vec3 axis, double theta) {
        if (Math.abs(theta) < 1e-9) return v;
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        return v.scale(cos)
                .add(axis.cross(v).scale(sin))
                .add(axis.scale(axis.dot(v) * (1.0 - cos)));
    }


    private static void vQuad(PoseStack.Pose pose, VertexConsumer v,
                              Vec3 a, Vec3 b, Vec3 wingA, Vec3 wingB, float w,
                              int r, int g, int bl, int al) {
        Vec3 a0 = a, a1 = a.add(wingA.scale(w));
        Vec3 b0 = b, b1 = b.add(wingB.scale(w));
        bv(pose, v, a0, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, b0, r, g, bl, al);
        bv(pose, v, b0, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, a0, r, g, bl, al);
    }

    private static void bv(PoseStack.Pose pose, VertexConsumer v,
                           Vec3 p, int r, int g, int b, int a) {
        v.addVertex(pose, (float)p.x, (float)p.y, (float)p.z).setColor(r, g, b, a);
    }

    private static long hashPos(Vec3 p) {
        long ix = (long)(p.x * 100);
        long iy = (long)(p.y * 100);
        long iz = (long)(p.z * 100);
        long h  = ix * 0x9E3779B97F4A7C15L
                ^ iy * 0x6C62272E07BB0142L
                ^ iz * 0xBF58476D1CE4E5B9L;
        h ^= h >>> 30;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27;
        return h;
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