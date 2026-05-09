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

    public static class StrikeRenderState extends EntityRenderState {
        float posX, posY, posZ;
        float height;
        int   r, g, b;
        float alpha;
        float age;
        long  seed;
    }

    /** Shape re-randomises every tick for a rapid flicker. */
    private static final int   BOLT_UPDATE_TICKS = 1;
    private static final int   STRAND_COUNT      = 20;
    /** How far below the entity origin the bolt extends (pushes it into the ground). */
    private static final float BOTTOM_EXTEND     = 2f;

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
        s.r = e.getR(); s.g = e.getG(); s.b = e.getB();

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
        final int   mainAlpha = (int)(alphaFull * 0.85f);
        final float age       = s.age;
        final long  seed      = s.seed;
        final float height    = s.height;

        ps.pushPose();
        ps.translate(s.posX, s.posY, s.posZ);

        for (int strand = 0; strand < STRAND_COUNT; strand++) {
            final long strandSeed = seed ^ ((long)strand * 0x9E3779B97F4A7C15L);

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawVerticalBolt(pose, v, height, 0.07f,
                            s.r, s.g, s.b, mainAlpha, age, strandSeed));
        }

        ps.popPose();
    }

    // ── Bolt geometry ─────────────────────────────────────────────────────────

    private static void drawVerticalBolt(PoseStack.Pose pose, VertexConsumer v,
                                         float height, float width,
                                         int r, int g, int bl, int alpha,
                                         float age, long seed) {
        if (alpha < 4 || height < 0.1f) return;

        // Changes shape every tick (BOLT_UPDATE_TICKS = 1)
        int timeSlot = (int)(age / BOLT_UPDATE_TICKS);
        int knots    = Mth.clamp((int)(height / 1.5f), 4, 24);

        Vec3[] pts = new Vec3[knots + 2];
        // Bottom extends BOTTOM_EXTEND blocks below the entity origin
        pts[0]         = new Vec3(0, -BOTTOM_EXTEND, 0);
        pts[knots + 1] = new Vec3(0,  height,        0);

        float totalSpan = height + BOTTOM_EXTEND;

        for (int i = 1; i <= knots; i++) {
            float t   = i / (float)(knots + 1);
            float env = (float) Math.sin(t * Math.PI);
            // Wider spread than before
            float maxOff = Math.min(totalSpan * 0.14f, 2.0f);

            float offX = hash(seed + i * 997L  + timeSlot * 7919L) * env * maxOff;
            float offZ = hash(seed + i * 1009L + timeSlot * 6271L) * env * maxOff;
            // Y interpolated across the full span (bottom → top)
            pts[i] = new Vec3(offX, -BOTTOM_EXTEND + totalSpan * t, offZ);
        }

        final float COS30 = 0.866f;
        final float SIN30 = 0.5f;

        for (int i = 0; i < pts.length - 1; i++) {
            Vec3 a   = pts[i];
            Vec3 b   = pts[i + 1];
            Vec3 dir = b.subtract(a);
            if (dir.lengthSqr() < 1e-8) continue;
            dir = dir.normalize();

            Vec3 helper   = (Math.abs(dir.y) > 0.9) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 right    = dir.cross(helper).normalize();
            Vec3 up       = right.cross(dir).normalize();
            Vec3 bisector = right.add(up).normalize();
            Vec3 perp     = right.subtract(up).normalize();
            Vec3 wingA    = bisector.scale(COS30).add(perp.scale(SIN30));
            Vec3 wingB    = bisector.scale(COS30).subtract(perp.scale(SIN30));

            vQuad(pose, v, a, b, wingA, width, r, g, bl, alpha);
            vQuad(pose, v, a, b, wingB, width, r, g, bl, alpha);
        }
    }


    private static void vQuad(PoseStack.Pose pose, VertexConsumer v,
                              Vec3 a, Vec3 b, Vec3 wing, float w,
                              int r, int g, int bl, int al) {
        Vec3 a0 = a, a1 = a.add(wing.scale(w));
        Vec3 b0 = b, b1 = b.add(wing.scale(w));
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
}