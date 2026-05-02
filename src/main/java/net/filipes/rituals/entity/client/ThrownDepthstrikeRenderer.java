package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.ThrownDepthstrikeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ThrownDepthstrikeRenderer extends EntityRenderer<ThrownDepthstrikeEntity, ThrownDepthstrikeRenderState> {

    // ── Hex line constants ─────────────────────────────────────────────────────
    // Must match ThrownDepthstrikeEntity.HEX_LINE_LEN and HEX_REVEAL_TICK
    private static final float HEX_LINE_LEN  = 2.5f;
    private static final float HEX_REVEAL    = 12f;   // ticks to grow to full length
    private static final float HEX_HOLD      = 4f;    // ticks fully visible before fade
    private static final float HEX_FADE      = 8f;    // ticks to fade out
    private static final float HEX_TOTAL     = HEX_REVEAL + HEX_HOLD + HEX_FADE;

    // 3 bolts per direction — flat + two slightly elevated for depth
    // { horiz angle offset (deg), elevation (deg), length scale, alpha scale }
    private static final float[][] SUB_BOLTS = {
            {  0f,  0f, 1.00f, 1.00f },
            { 10f, 18f, 0.90f, 0.70f },
            {-10f, 14f, 0.85f, 0.60f },
    };

    // ── Old impact quad constants (start after HEX_REVEAL) ────────────────────
    private static final int   IMP_NUM_SPARKS = 18;
    private static final float IMP_CYCLE      = 10f;
    private static final int   IMP_NUM_SEGS   = 3;
    private static final float IMP_FADE       = 1.0f;

    // ─────────────────────────────────────────────────────────────────────────

    public ThrownDepthstrikeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ThrownDepthstrikeRenderState createRenderState() {
        return new ThrownDepthstrikeRenderState();
    }

    @Override
    public void extractRenderState(ThrownDepthstrikeEntity entity, ThrownDepthstrikeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.partialTick = partialTick;
        state.stack = entity.getPickupItemStackOrigin();
        state.yRot  = entity.getYRot();
        state.xRot  = entity.getXRot();
        state.yRotO = entity.yRotO;
        state.xRotO = entity.xRotO;

        state.trail.clear();
        double curX = entity.getX(), curY = entity.getY(), curZ = entity.getZ();
        for (int i = 0; i < entity.trailSize; i++) {
            int idx = Math.floorMod(entity.trailHead - 1 - i, ThrownDepthstrikeEntity.TRAIL_LENGTH);
            Vec3 wp = entity.trailPositions[idx];
            if (wp != null) state.trail.add(new Vec3(wp.x - curX, wp.y - curY, wp.z - curZ));
        }

        state.inGround   = entity.isThrownInGround();
        state.age        = entity.tickCount - 1 + partialTick;
        state.seed       = entity.getUUID().getMostSignificantBits()
                ^ entity.getUUID().getLeastSignificantBits();
        state.landingAge = entity.landingTick >= 0 ? (float)(entity.landingTick - 1) : -1f;

        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                state.itemRenderState, state.stack,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                entity.level(), entity, entity.getId());
    }

    @Override
    public void submit(ThrownDepthstrikeRenderState state, PoseStack poseStack,
                       SubmitNodeCollector snc, CameraRenderState camera) {

        if (state.trail.size() >= 2) drawTrail(state, poseStack, snc);
        if (state.inGround)          drawHexLines(state, poseStack, snc);
        if (state.inGround)          drawImpactQuads(state, poseStack, snc);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(state.partialTick, state.yRotO, state.yRot) - 90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(state.partialTick, state.xRotO, state.xRot) + 90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        state.itemRenderState.submit(poseStack, snc, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        super.submit(state, poseStack, snc, camera);
    }

    // ── Flying trail ──────────────────────────────────────────────────────────

    private static void drawTrail(ThrownDepthstrikeRenderState s, PoseStack ps, SubmitNodeCollector snc) {
        if (s.inGround) return;
        List<Vec3> trail = s.trail;
        int count = Math.min(trail.size(), 5);
        if (count < 2) return;

        for (int i = 0; i < count - 1; i++) {
            Vec3 from   = trail.get(i);
            Vec3 to     = trail.get(i + 1);
            float alpha = 1.0f - (i / (float)(count - 1));
            if (alpha < 0.04f) continue;

            Vec3 rawDir = to.subtract(from);
            if (rawDir.lengthSqr() < 1e-6) continue;
            Vec3 dir    = rawDir.normalize();
            Vec3 helper = (Math.abs(dir.y) > 0.95) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 right  = dir.cross(helper).normalize();
            Vec3 up     = right.cross(dir).normalize();

            float len   = (float) rawDir.length();
            long  seedA = s.seed ^ (i * 0x9E3779B97F4A7C15L);
            long  seedB = seedA  ^ 0xDEADBEEFCAFEBABEL;
            int   outerA = (int)(alpha * 185), innerA = (int)(alpha * 155);

            // Trail uses full jaggedness (1f) and is always fully revealed (revealFraction=1f)
            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawBolt(pose, v, from, dir, right, up, len, 0.10f,   0, 200, 220, outerA, s.age, seedA, 1f, 1f));
            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawBolt(pose, v, from, dir, right, up, len, 0.05f, 130, 255, 245, innerA, s.age, seedB, 1f, 1f));
        }
    }

    // ── 6 hex lines growing from center ──────────────────────────────────────

    private static void drawHexLines(ThrownDepthstrikeRenderState s, PoseStack ps, SubmitNodeCollector snc) {
        if (s.landingAge < 0) return;
        float impactAge = s.age - s.landingAge;
        if (impactAge < 0 || impactAge > HEX_TOTAL) return;

        // Smooth 0→1 reveal over HEX_REVEAL ticks
        float revealFraction = Mth.clamp(impactAge / HEX_REVEAL, 0f, 1f);

        // Alpha: full while growing + hold, then fade
        float alpha;
        if (impactAge <= HEX_REVEAL + HEX_HOLD) {
            alpha = 1.0f;
        } else {
            alpha = 1.0f - (impactAge - HEX_REVEAL - HEX_HOLD) / HEX_FADE;
        }
        alpha = Mth.clamp(alpha, 0f, 1f);
        if (alpha < 0.01f) return;

        // Shape frozen during reveal so the line doesn't wiggle while growing,
        // then flickers once fully extended for a live-lightning feel.
        float shapeTick = revealFraction < 1f ? s.landingAge : impactAge;

        for (int d = 0; d < 6; d++) {
            double baseAngle = Math.PI / 3.0 * d;

            for (int sb = 0; sb < SUB_BOLTS.length; sb++) {
                double hAngle    = baseAngle + Math.toRadians(SUB_BOLTS[sb][0]);
                double elevAngle = Math.toRadians(SUB_BOLTS[sb][1]);
                float  lenScale  = SUB_BOLTS[sb][2];
                float  aScale    = SUB_BOLTS[sb][3];

                double cosElev = Math.cos(elevAngle);
                Vec3 dir = new Vec3(
                        Math.cos(hAngle) * cosElev,
                        Math.sin(elevAngle),
                        Math.sin(hAngle) * cosElev
                ).normalize();

                Vec3 worldUp = new Vec3(0, 1, 0);
                Vec3 right   = dir.cross(worldUp);
                if (right.lengthSqr() < 1e-6) right = new Vec3(1, 0, 0);
                right = right.normalize();
                Vec3 up = right.cross(dir).normalize();

                float len    = HEX_LINE_LEN * lenScale;
                int   outerA = (int)(alpha * aScale * 185);
                int   innerA = (int)(alpha * aScale * 155);
                long  seedA  = s.seed ^ ((long)d * 0x9E3779B97F4A7C15L) ^ ((long)sb * 0x6C62272E07BB0142L);
                long  seedB  = seedA  ^ 0xDEADBEEFCAFEBABEL;

                final Vec3  fDir = dir, fRight = right, fUp = up;
                final float fLen = len, fShape = shapeTick, fReveal = revealFraction;
                final long  fSA = seedA, fSB = seedB;
                final int   fOA = outerA, fIA = innerA;

                // Very low jaggedness (0.06f) so lines appear nearly straight
                snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                        drawBolt(pose, v, Vec3.ZERO, fDir, fRight, fUp,
                                fLen, 0.10f,   0, 200, 220, fOA, fShape, fSA, fReveal, 0.06f));
                snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                        drawBolt(pose, v, Vec3.ZERO, fDir, fRight, fUp,
                                fLen, 0.05f, 130, 255, 245, fIA, fShape, fSB, fReveal, 0.06f));
            }
        }
    }

    // ── Old impact quads (start after HEX_REVEAL) ────────────────────────────

    private static void drawImpactQuads(ThrownDepthstrikeRenderState s, PoseStack ps, SubmitNodeCollector snc) {
        if (s.landingAge < 0) return;
        float impactAge = s.age - s.landingAge;
        // Don't start until the hex lines have fully extended
        if (impactAge < HEX_REVEAL) return;

        // Age relative to when quads started
        float age = impactAge - HEX_REVEAL;

        Vec3 normal = new Vec3(0, 1, 0);
        Vec3 axisA  = new Vec3(1, 0, 0);
        Vec3 axisB  = new Vec3(0, 0, 1);
        float totalAliveTime = IMP_NUM_SEGS + IMP_FADE;

        for (int slot = 0; slot < IMP_NUM_SPARKS; slot++) {
            float offset   = (hash(s.seed + slot * 0xA7B3L) * 0.5f + 0.5f) * IMP_CYCLE;
            float localAge = (age + offset) % IMP_CYCLE;
            if (localAge >= totalAliveTime) continue;

            int  cycleNum  = (int)((age + offset) / IMP_CYCLE);
            long shapeSeed = rehash(s.seed ^ ((long)slot * 0x9E3779B97F4A7C15L)
                    + (long)cycleNum * 0x6C62272E07BB0142L);

            float angle   = (hash(shapeSeed) * 0.5f + 0.5f) * (float)(2.0 * Math.PI);
            Vec3  boltDir = axisA.scale(Math.cos(angle)).add(axisB.scale(Math.sin(angle)));
            Vec3  perp    = normal.cross(boltDir).normalize();
            float totalLen = 1.0f + (hash(shapeSeed + 1L) * 0.5f + 0.5f) * 2.0f;

            Vec3[] pts = new Vec3[IMP_NUM_SEGS + 1];
            pts[0] = Vec3.ZERO;
            for (int k = 1; k <= IMP_NUM_SEGS; k++) {
                float t      = k / (float) IMP_NUM_SEGS;
                float env    = (float) Math.sin(t * Math.PI);
                float maxOff = totalLen * 0.18f;
                float offP   = hash(shapeSeed + k * 997L)  * env * maxOff;
                float offN   = hash(shapeSeed + k * 1009L) * env * maxOff;
                pts[k] = Vec3.ZERO.add(boltDir.scale(totalLen * t))
                        .add(perp.scale(offP))
                        .add(normal.scale(offN));
            }

            Vec3 p0 = normal.scale(0.5f);
            Vec3 p1 = normal.scale(-0.5f).add(perp.scale((float)(Math.sqrt(3.0) / 2.0)));
            Vec3 p2 = normal.scale(-0.5f).add(perp.scale((float)(-Math.sqrt(3.0) / 2.0)));

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
                segAlpha = Mth.clamp(segAlpha, 0f, 1f);

                final int al = (int)(segAlpha * 210);
                if (al < 4) continue;

                final Vec3 a = pts[seg], b = pts[seg + 1];
                final Vec3 s0 = p0, s1 = p1, s2 = p2;

                snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, vx) -> {
                    vSide(pose, vx, a, b, s0.scale(0.08f), s1.scale(0.08f),  20, 255, 190, al);
                    vSide(pose, vx, a, b, s0.scale(0.08f), s2.scale(0.08f),  20, 255, 190, al);
                });
                snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, vx) -> {
                    vSide(pose, vx, a, b, s0.scale(0.03f), s1.scale(0.03f), 160, 255, 240, al);
                    vSide(pose, vx, a, b, s0.scale(0.03f), s2.scale(0.03f), 160, 255, 240, al);
                });
            }
        }
    }

    // ── Shared bolt geometry ──────────────────────────────────────────────────

    /**
     * @param revealFraction 0→1 smooth segment-by-segment tip reveal
     * @param jaggedFactor   0=straight line, 1=full zigzag; use 0.06 for hex lines, 1.0 for trail
     */
    private static void drawBolt(PoseStack.Pose pose, VertexConsumer v,
                                 Vec3 origin, Vec3 dir, Vec3 right, Vec3 up,
                                 float length, float width,
                                 int r, int g, int b, int al,
                                 float age, long seed,
                                 float revealFraction, float jaggedFactor) {
        if (length < 0.05f || al < 4 || revealFraction <= 0f) return;

        int timeSlot = (int) age;
        int knots    = Math.max(3, (int)(length / 1.2f));
        int total    = knots + 2;

        Vec3[] pts = new Vec3[total];
        pts[0]         = origin;
        pts[total - 1] = origin.add(dir.scale(length));

        for (int i = 1; i <= knots; i++) {
            float t      = i / (float)(knots + 1);
            float env    = (float) Math.sin(t * Math.PI);
            float maxOff = Math.min(length * 0.50f, 1.4f) * jaggedFactor;
            float offR   = hash(seed + i * 997L  + timeSlot * 7919L) * env * maxOff;
            float offU   = hash(seed + i * 1009L + timeSlot * 6271L) * env * maxOff;
            pts[i] = origin
                    .add(dir.scale(length * t))
                    .add(right.scale(offR))
                    .add(up.scale(offU));
        }

        float half = width * 0.5f;
        float wing = (float)(width * Math.sqrt(3.0) / 2.0);
        Vec3  p0   = up.scale(half);
        Vec3  p1   = up.scale(-half).add(right.scale( wing));
        Vec3  p2   = up.scale(-half).add(right.scale(-wing));

        // Smooth tip: reveal segment by segment, interpolating the last partial one
        float segCount = revealFraction * (total - 1);
        int   fullSegs = (int) segCount;
        float partialT = segCount - fullSegs;

        for (int i = 0; i < fullSegs && i < total - 1; i++) {
            vSide(pose, v, pts[i], pts[i + 1], p0, p1, r, g, b, al);
            vSide(pose, v, pts[i], pts[i + 1], p0, p2, r, g, b, al);
        }

        if (fullSegs < total - 1 && partialT > 0.001f) {
            Vec3 from = pts[fullSegs];
            Vec3 to   = pts[fullSegs + 1];
            Vec3 tip  = new Vec3(
                    from.x + (to.x - from.x) * partialT,
                    from.y + (to.y - from.y) * partialT,
                    from.z + (to.z - from.z) * partialT
            );
            vSide(pose, v, from, tip, p0, p1, r, g, b, al);
            vSide(pose, v, from, tip, p0, p2, r, g, b, al);
        }
    }

    private static void vSide(PoseStack.Pose pose, VertexConsumer v,
                              Vec3 a, Vec3 b, Vec3 off0, Vec3 off1,
                              int r, int g, int bl, int al) {
        Vec3 a0 = a.add(off0), a1 = a.add(off1);
        Vec3 b0 = b.add(off0), b1 = b.add(off1);
        bv(pose, v, a0, r, g, bl, al); bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al); bv(pose, v, b0, r, g, bl, al);
        bv(pose, v, b0, r, g, bl, al); bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al); bv(pose, v, a0, r, g, bl, al);
    }

    private static void bv(PoseStack.Pose pose, VertexConsumer v,
                           Vec3 p, int r, int g, int b, int a) {
        v.addVertex(pose, (float)p.x, (float)p.y, (float)p.z).setColor(r, g, b, a);
    }

    private static float hash(long seed) {
        seed ^= (seed >>> 30); seed *= 0xBF58476D1CE4E5B9L;
        seed ^= (seed >>> 27); seed *= 0x94D049BB133111EBL;
        seed ^= (seed >>> 31);
        return (seed & Long.MAX_VALUE) / (float) Long.MAX_VALUE * 2f - 1f;
    }

    private static long rehash(long seed) {
        seed ^= (seed >>> 33); seed *= 0xFF51AFD7ED558CCDL;
        seed ^= (seed >>> 33); seed *= 0xC4CEB9FE1A85EC53L;
        seed ^= (seed >>> 33);
        return seed;
    }
}