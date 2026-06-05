package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.MultiBurstSparkEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MultiBurstSparkEntityRenderer
        extends EntityRenderer<MultiBurstSparkEntity, MultiBurstSparkEntityRenderer.State> {

    public static class State extends EntityRenderState {
        final List<List<Vec3>> trails = new ArrayList<>();
        int   r, g, b, alpha;
        float width;
        int   windowSize;
        float jitter;
        long  seed;
    }

    public MultiBurstSparkEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(MultiBurstSparkEntity e, State s, float pt) {
        super.extractRenderState(e, s, pt);

        s.trails.clear();
        if (e.clientTrails != null) {
            for (List<Vec3> t : e.clientTrails)
                s.trails.add(new ArrayList<>(t));
        }

        int col      = e.getTrailColor();
        s.r          = (col >> 16) & 0xFF;
        s.g          = (col >> 8)  & 0xFF;
        s.b          =  col        & 0xFF;
        s.alpha      = e.getTrailAlpha();
        s.width      = e.getTrailWidth();
        s.windowSize = e.getWindowSize();
        s.jitter     = e.getTrailJitter();
        s.seed       = e.getUUID().getMostSignificantBits();
    }

    @Override public boolean affectedByCulling(MultiBurstSparkEntity e) { return false; }
    @Override protected float getShadowRadius  (State s) { return 0f; }
    @Override protected float getShadowStrength(State s) { return 0f; }

    @Override
    public void submit(State s, PoseStack ps, SubmitNodeCollector snc, CameraRenderState cam) {
        if (s.trails.isEmpty()) return;

        final double camX  = s.x, camY = s.y, camZ = s.z;

        final float  COS30 = (float)(Math.sqrt(3.0) / 2.0);
        final float  SIN30 = 0.5f;
        final int    r = s.r, g = s.g, b = s.b, al = s.alpha;
        final float  w = s.width;

        ps.pushPose();

        for (int ti = 0; ti < s.trails.size(); ti++) {
            List<Vec3> trail = s.trails.get(ti);
            int total = trail.size();
            if (total < 2) continue;

            int winEnd   = total - 2;
            int winStart = Math.max(0, winEnd - s.windowSize + 1);

            Vec3[] pts = new Vec3[total];
            for (int i = 0; i < total; i++) {
                Vec3 p = trail.get(i);
                pts[i] = new Vec3(p.x - camX, p.y - camY, p.z - camZ);
            }

            Vec3 overall = pts[winEnd + 1].subtract(pts[winStart]);
            if (overall.lengthSqr() < 1e-8) overall = new Vec3(0, 1, 0);
            overall = overall.normalize();

            Vec3 oHelper   = (Math.abs(overall.y) > 0.9) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 oRight    = overall.cross(oHelper).normalize();
            Vec3 oUp       = overall.cross(oRight).normalize();
            Vec3 oBisector = oRight.add(oUp).normalize();
            Vec3 oPerp     = oRight.subtract(oUp).normalize();
            final Vec3 wingA = oBisector.scale(COS30).add(oPerp.scale(SIN30));
            final Vec3 wingB = oBisector.scale(COS30).subtract(oPerp.scale(SIN30));

            // Optional jitter — burstJitter is 0 by default
            if (s.jitter > 1e-4f) {
                pts = pts.clone();
                long trailSeed = s.seed ^ ((long) ti * 0x9E3779B97F4A7C15L);
                for (int i = winStart + 1; i <= winEnd && i < total - 1; i++) {
                    Vec3 prev = pts[i - 1], next = pts[i + 1];
                    Vec3 dir  = next.subtract(prev);
                    if (dir.lengthSqr() < 1e-8) continue;
                    dir = dir.normalize();
                    Vec3 helper = (Math.abs(dir.y) > 0.9) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
                    Vec3 right  = dir.cross(helper).normalize();
                    Vec3 up     = right.cross(dir).normalize();
                    Vec3 wp     = trail.get(i);
                    long posSeed = hashPos(wp) ^ trailSeed;
                    float offR   = hash(posSeed + 997L)  * s.jitter;
                    float offU   = hash(posSeed + 1009L) * s.jitter;
                    pts[i] = pts[i].add(right.scale(offR)).add(up.scale(offU));
                }
            }

            final Vec3[] finalPts = pts;
            final Vec3   fWA = wingA, fWB = wingB;

            for (int i = winStart; i <= winEnd; i++) {
                Vec3 ra = finalPts[i], rb = finalPts[i + 1];
                if (rb.subtract(ra).lengthSqr() < 1e-8) continue;

                final Vec3 pA = ra, pB = rb;

                snc.submitCustomGeometry(ps, SparkEntityRenderer.SPARK_TRAIL, (pose, v) -> {
                    vQuad(pose, v, pA, pB, fWA, fWA, w, r, g, b, al);
                    vQuad(pose, v, pA, pB, fWB, fWB, w, r, g, b, al);
                });
            }
        }

        ps.popPose();
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
        long ix = (long)(p.x * 100), iy = (long)(p.y * 100), iz = (long)(p.z * 100);
        long h  = ix * 0x9E3779B97F4A7C15L ^ iy * 0x6C62272E07BB0142L ^ iz * 0xBF58476D1CE4E5B9L;
        h ^= h >>> 30; h *= 0xBF58476D1CE4E5B9L; h ^= h >>> 27;
        return h;
    }

    private static float hash(long seed) {
        seed ^= (seed >>> 30); seed *= 0xBF58476D1CE4E5B9L;
        seed ^= (seed >>> 27); seed *= 0x94D049BB133111EBL;
        seed ^= (seed >>> 31);
        return (seed & Long.MAX_VALUE) / (float) Long.MAX_VALUE * 2f - 1f;
    }
}