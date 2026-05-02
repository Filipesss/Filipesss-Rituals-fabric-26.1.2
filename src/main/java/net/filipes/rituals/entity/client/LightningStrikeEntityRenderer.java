package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.LightningStrikeEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LightningStrikeEntityRenderer
        extends EntityRenderer<LightningStrikeEntity, LightningStrikeEntityRenderer.StrikeRenderState> {

    // ── Render state ──────────────────────────────────────────────────────────

    public static class StrikeRenderState extends EntityRenderState {

        float posX, posY, posZ;
        float height;

        int glowR, glowG, glowB;

        int coreR, coreG, coreB;

        float alpha;
        float age;
        long  seed;
    }

    private static final int   BOLT_UPDATE_TICKS = 2;
    private static final int   STRAND_COUNT = 5;
    private static final int   IMP_NUM_SPARKS = 20;
    private static final float IMP_CYCLE      = 10f;   // ticks per full spark cycle
    private static final int   IMP_NUM_SEGS   = 3;     // segments per individual spark
    private static final float IMP_FADE       = 1.0f;  // fade in / fade out ticks

    public LightningStrikeEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public StrikeRenderState createRenderState() { return new StrikeRenderState(); }

    @Override
    public void extractRenderState(LightningStrikeEntity e, StrikeRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        double eX = e.xo + (e.getX() - e.xo) * pt;
        double eY = e.yo + (e.getY() - e.yo) * pt;
        double eZ = e.zo + (e.getZ() - e.zo) * pt;
        s.posX = (float)(eX - s.x);
        s.posY = (float)(eY - s.y);
        s.posZ = (float)(eZ - s.z);

        s.height = e.getStrikeHeight();

        s.glowR = e.getGlowR(); s.glowG = e.getGlowG(); s.glowB = e.getGlowB();
        s.coreR = e.getCoreR(); s.coreG = e.getCoreG(); s.coreB = e.getCoreB();

        float appear = e.prevAppearTimer + (e.appearTimer - e.prevAppearTimer) * pt;
        s.alpha = Mth.clamp(appear / (float) LightningStrikeEntity.APPEAR_TICKS, 0f, 1f);

        s.age  = e.tickCount - 1 + pt;
        s.seed = e.getUUID().getMostSignificantBits()
                ^ e.getUUID().getLeastSignificantBits();
    }

    @Override public boolean affectedByCulling(LightningStrikeEntity e) { return false; }
    @Override protected float getShadowRadius  (StrikeRenderState s)    { return 0f; }
    @Override protected float getShadowStrength(StrikeRenderState s)    { return 0f; }


    @Override
    public void submit(StrikeRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

        if (s.alpha < 0.005f) return;

        final int   alphaFull = (int)(s.alpha * 255);
        final float age       = s.age;
        final long  seed      = s.seed;
        final float height    = s.height;

        final Vec3 axisX = new Vec3(1, 0, 0);
        final Vec3 axisZ = new Vec3(0, 0, 1);

        ps.pushPose();
        ps.translate(s.posX, s.posY, s.posZ);

        final int glowHalo = (int)(alphaFull * 0.22f);
        final int glowMid  = (int)(alphaFull * 0.45f);
        final int coreAl   = (int)(alphaFull * 0.90f);

        for (int strand = 0; strand < STRAND_COUNT; strand++) {

            final long strandSeed = seed ^ ((long)strand * 0x9E3779B97F4A7C15L);

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawVerticalBolt(pose, v, height, axisX, axisZ,
                            0.22f,
                            s.glowR, s.glowG, s.glowB,
                            glowHalo,
                            age, strandSeed));

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawVerticalBolt(pose, v, height, axisX, axisZ,
                            0.11f,
                            s.glowR, s.glowG, s.glowB,
                            glowMid,
                            age, strandSeed));

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawVerticalBolt(pose, v, height, axisX, axisZ,
                            0.045f,
                            s.coreR, s.coreG, s.coreB,
                            coreAl,
                            age, strandSeed));
        }

        if (age > 2f) {
            drawImpactBolts(s, ps, snc);
        }

        ps.popPose();
    }

    private static void drawVerticalBolt(PoseStack.Pose pose, VertexConsumer v,
                                         float height,
                                         Vec3 axisA, Vec3 axisB,
                                         float width,
                                         int r, int g, int b, int alpha,
                                         float age, long seed) {
        if (alpha < 4 || height < 0.1f) return;

        int timeSlot = (int)(age / BOLT_UPDATE_TICKS);

        int knots = Mth.clamp((int)(height / 1.5f), 4, 24);

        Vec3[] pts = new Vec3[knots + 2];
        pts[0]         = new Vec3(0, 0,      0);
        pts[knots + 1] = new Vec3(0, height, 0);

        for (int i = 1; i <= knots; i++) {
            float t   = i / (float)(knots + 1);
            float env    = (float) Math.sin(t * Math.PI);
            float maxOff = Math.min(height * 0.18f, 1.5f);

            float offA = hash(seed + i * 997L  + timeSlot * 7919L) * env * maxOff;
            float offB = hash(seed + i * 1009L + timeSlot * 6271L) * env * maxOff;

            pts[i] = new Vec3(offA, height * t, offB);
        }

        for (int i = 0; i < pts.length - 1; i++) {
            crossQuad(pose, v, pts[i], pts[i + 1], axisA, width, r, g, b, alpha);
            crossQuad(pose, v, pts[i], pts[i + 1], axisB, width, r, g, b, alpha);
        }
    }

    private static void drawImpactBolts(StrikeRenderState s,
                                        PoseStack ps, SubmitNodeCollector snc) {

        final Vec3 impact = Vec3.ZERO;

        final Vec3 normal = new Vec3(0, 1, 0);
        final Vec3 axisA  = new Vec3(1, 0, 0);
        final Vec3 axisB  = new Vec3(0, 0, 1);

        final int   glowR = s.glowR, glowG = s.glowG, glowB = s.glowB;
        final int   coreR = s.coreR, coreG = s.coreG, coreB = s.coreB;
        final float masterAlpha = s.alpha;

        final float totalAliveTime = IMP_NUM_SEGS + IMP_FADE;

        for (int slot = 0; slot < IMP_NUM_SPARKS; slot++) {

            float offset   = (hash(s.seed + slot * 0xA7B3L) * 0.5f + 0.5f) * IMP_CYCLE;
            float localAge = (s.age + offset) % IMP_CYCLE;

            if (localAge >= totalAliveTime) continue;

            int  cycleNum  = (int)((s.age + offset) / IMP_CYCLE);
            long shapeSeed = rehash(s.seed ^ ((long)slot * 0x9E3779B97F4A7C15L)
                    + (long)cycleNum * 0x6C62272E07BB0142L);

            float angle  = (hash(shapeSeed) * 0.5f + 0.5f) * (float)(2.0 * Math.PI);
            Vec3 boltDir = axisA.scale(Math.cos(angle)).add(axisB.scale(Math.sin(angle)));
            Vec3 perp    = normal.cross(boltDir).normalize();

            float totalLen = 1.4f + (hash(shapeSeed + 1L) * 0.5f + 0.5f) * 2.0f;

            Vec3[] pts = new Vec3[IMP_NUM_SEGS + 1];
            pts[0] = impact;
            for (int k = 1; k <= IMP_NUM_SEGS; k++) {
                float t      = k / (float) IMP_NUM_SEGS;
                float env    = (float) Math.sin(t * Math.PI);
                float maxOff = totalLen * 0.12f;
                float offP   = hash(shapeSeed + k * 997L)  * env * maxOff;
                float offN   = hash(shapeSeed + k * 1009L) * env * maxOff;
                pts[k] = impact
                        .add(boltDir.scale(totalLen * t))
                        .add(perp.scale(offP))
                        .add(normal.scale(offN));
            }

            final double sqrt3over2 = Math.sqrt(3.0) / 2.0;
            final Vec3   tubeA      = perp;
            final Vec3   tubeB      = perp.scale(-0.5).add(normal.scale(sqrt3over2));

            for (int seg = 0; seg < IMP_NUM_SEGS; seg++) {

                float segRevealAge = localAge - seg;
                if (segRevealAge < 0f) continue;

                float segAlpha;
                if (segRevealAge < IMP_FADE) {
                    segAlpha = segRevealAge / IMP_FADE;
                } else if (localAge > totalAliveTime - IMP_FADE) {
                    segAlpha = (totalAliveTime - localAge) / IMP_FADE;
                } else {
                    segAlpha = 1.0f;
                }
                segAlpha = Mth.clamp(segAlpha * masterAlpha, 0f, 1f);

                final int al = (int)(segAlpha * 220);
                if (al < 4) continue;

                final Vec3 ptA = pts[seg];
                final Vec3 ptB = pts[seg + 1];
                final Vec3 tA  = tubeA;
                final Vec3 tB  = tubeB;

                snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) -> {
                    crossQuad(pose, v, ptA, ptB, tA, 0.12f, glowR, glowG, glowB, (int)(al * 0.6f));
                    crossQuad(pose, v, ptA, ptB, tB, 0.12f, glowR, glowG, glowB, (int)(al * 0.6f));
                });
                snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) -> {
                    crossQuad(pose, v, ptA, ptB, tA, 0.05f, coreR, coreG, coreB, al);
                    crossQuad(pose, v, ptA, ptB, tB, 0.05f, coreR, coreG, coreB, al);
                });
            }
        }
    }

    private static void crossQuad(PoseStack.Pose pose, VertexConsumer v,
                                  Vec3 a, Vec3 b, Vec3 axis, float w,
                                  int r, int g, int bl, int al) {
        float h   = w * 0.5f;
        Vec3  off = axis.scale(h);
        Vec3  a0  = a.add(off),      a1 = a.subtract(off);
        Vec3  b0  = b.add(off),      b1 = b.subtract(off);
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
        v.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, b, a);
    }

    private static float hash(long seed) {
        seed ^= (seed >>> 30);
        seed *= 0xBF58476D1CE4E5B9L;
        seed ^= (seed >>> 27);
        seed *= 0x94D049BB133111EBL;
        seed ^= (seed >>> 31);
        return (seed & Long.MAX_VALUE) / (float) Long.MAX_VALUE * 2f - 1f;
    }

    private static long rehash(long seed) {
        seed ^= (seed >>> 33);
        seed *= 0xFF51AFD7ED558CCDL;
        seed ^= (seed >>> 33);
        seed *= 0xC4CEB9FE1A85EC53L;
        seed ^= (seed >>> 33);
        return seed;
    }
}