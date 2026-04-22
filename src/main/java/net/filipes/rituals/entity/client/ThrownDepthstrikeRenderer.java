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
        int size = entity.trailSize;
        for (int i = 0; i < size; i++) {
            int idx = Math.floorMod(entity.trailHead - 1 - i, ThrownDepthstrikeEntity.TRAIL_LENGTH);
            Vec3 wp = entity.trailPositions[idx];
            if (wp != null) {
                state.trail.add(new Vec3(wp.x - curX, wp.y - curY, wp.z - curZ));
            }
        }

        state.inGround = entity.isThrownInGround();
        state.age      = entity.tickCount - 1 + partialTick;
        state.seed     = entity.getUUID().getMostSignificantBits()
                ^ entity.getUUID().getLeastSignificantBits();

        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                state.itemRenderState,
                state.stack,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                entity.level(),
                entity,
                entity.getId()
        );
    }

    @Override
    public void submit(ThrownDepthstrikeRenderState state, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

        if (state.trail.size() >= 2) {
            drawTrail(state, poseStack, submitNodeCollector);
        }

        if (state.inGround) {
            drawImpact(state, poseStack, submitNodeCollector);
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(state.partialTick, state.yRotO, state.yRot) - 90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(state.partialTick, state.xRotO, state.xRot) + 90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        state.itemRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }


    private static void drawTrail(ThrownDepthstrikeRenderState s, PoseStack ps, SubmitNodeCollector snc) {
        if (s.inGround) return;

        List<Vec3> trail = s.trail;

        int count = Math.min(trail.size(), 5);
        if (count < 2) return;

        for (int i = 0; i < count - 1; i++) {
            Vec3 from = trail.get(i);
            Vec3 to   = trail.get(i + 1);

            float alpha = 1.0f - (i / (float)(count - 1));
            if (alpha < 0.04f) continue;

            Vec3 rawDir = to.subtract(from);
            if (rawDir.lengthSqr() < 1e-6) continue;

            Vec3 dir    = rawDir.normalize();
            Vec3 helper = (Math.abs(dir.y) > 0.95) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 right  = dir.cross(helper).normalize();
            Vec3 up     = right.cross(dir).normalize();

            float len   = (float) rawDir.length();
            float age   = s.age;
            long  seedA = s.seed ^ (i * 0x9E3779B97F4A7C15L);
            long  seedB = seedA  ^ 0xDEADBEEFCAFEBABEL;

            int outerA = (int)(alpha * 185);
            int innerA = (int)(alpha * 155);

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawBolt(pose, v, from, dir, right, up, len, 0.10f,   0, 200, 220, outerA, age, seedA));
            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawBolt(pose, v, from, dir, right, up, len, 0.05f, 130, 255, 245, innerA, age, seedB));
        }
    }

    private static void drawBolt(PoseStack.Pose pose, VertexConsumer v,
                                 Vec3 origin, Vec3 dir, Vec3 right, Vec3 up,
                                 float length, float width,
                                 int r, int g, int b, int al,
                                 float age, long seed) {
        if (length < 0.05f || al < 4) return;

        int timeSlot = (int) age;
        int knots    = Math.max(3, (int)(length / 1.2f));

        Vec3[] pts = new Vec3[knots + 2];
        pts[0]         = origin;
        pts[knots + 1] = origin.add(dir.scale(length));

        for (int i = 1; i <= knots; i++) {
            float t      = i / (float)(knots + 1);
            float env    = (float) Math.sin(t * Math.PI);
            float maxOff = Math.min(length * 0.50f, 1.4f);

            float offR = hash(seed + i * 997L  + timeSlot * 7919L) * env * maxOff;
            float offU = hash(seed + i * 1009L + timeSlot * 6271L) * env * maxOff;

            pts[i] = origin
                    .add(dir.scale(length * t))
                    .add(right.scale(offR))
                    .add(up.scale(offU));
        }

        float half = width * 0.5f;
        float wing = (float)(width * Math.sqrt(3.0) / 2.0);

        Vec3 p0 = up.scale(half);
        Vec3 p1 = up.scale(-half).add(right.scale( wing));
        Vec3 p2 = up.scale(-half).add(right.scale(-wing));

        for (int i = 0; i < pts.length - 1; i++) {
            vSide(pose, v, pts[i], pts[i + 1], p0, p1, r, g, b, al); // right panel
            vSide(pose, v, pts[i], pts[i + 1], p0, p2, r, g, b, al); // left  panel
        }
    }

    private static final int   IMP_NUM_SPARKS = 18;
    private static final float IMP_CYCLE      = 10f;
    private static final int   IMP_NUM_SEGS   = 3;
    private static final float IMP_FADE       = 1.0f;

    private static void drawImpact(ThrownDepthstrikeRenderState s, PoseStack ps, SubmitNodeCollector snc) {
        Vec3 impact = Vec3.ZERO;
        Vec3 normal = new Vec3(0, 1, 0);
        Vec3 axisA  = new Vec3(1, 0, 0);
        Vec3 axisB  = new Vec3(0, 0, 1);

        final float totalAliveTime = IMP_NUM_SEGS + IMP_FADE;

        for (int slot = 0; slot < IMP_NUM_SPARKS; slot++) {
            float offset   = (hash(s.seed + slot * 0xA7B3L) * 0.5f + 0.5f) * IMP_CYCLE;
            float localAge = (s.age + offset) % IMP_CYCLE;
            if (localAge >= totalAliveTime) continue;

            int  cycleNum  = (int)((s.age + offset) / IMP_CYCLE);
            long shapeSeed = rehash(s.seed ^ ((long)slot * 0x9E3779B97F4A7C15L)
                    + (long)cycleNum * 0x6C62272E07BB0142L);

            float angle   = (hash(shapeSeed) * 0.5f + 0.5f) * (float)(2.0 * Math.PI);
            Vec3  boltDir = axisA.scale(Math.cos(angle)).add(axisB.scale(Math.sin(angle)));
            Vec3  perp    = normal.cross(boltDir).normalize();

            float totalLen = 1.0f + (hash(shapeSeed + 1L) * 0.5f + 0.5f) * 2.0f;

            Vec3[] pts = new Vec3[IMP_NUM_SEGS + 1];
            pts[0] = impact;
            for (int k = 1; k <= IMP_NUM_SEGS; k++) {
                float t      = k / (float) IMP_NUM_SEGS;
                float env    = (float) Math.sin(t * Math.PI);
                float maxOff = totalLen * 0.18f;
                float offP   = hash(shapeSeed + k * 997L)  * env * maxOff;
                float offN   = hash(shapeSeed + k * 1009L) * env * maxOff;
                pts[k] = impact
                        .add(boltDir.scale(totalLen * t))
                        .add(perp.scale(offP))
                        .add(normal.scale(offN));
            }

            Vec3 p0 = normal.scale(0.5f);
            Vec3 p1 = normal.scale(-0.5f).add(perp.scale((float)(Math.sqrt(3.0) / 2.0)));
            Vec3 p2 = normal.scale(-0.5f).add(perp.scale((float)(-Math.sqrt(3.0) / 2.0)));

            for (int seg = 0; seg < IMP_NUM_SEGS; seg++) {
                float segRevealAge = localAge - seg;
                if (segRevealAge < 0f) continue;

                float alpha;
                if (segRevealAge < IMP_FADE) {
                    alpha = segRevealAge / IMP_FADE;
                } else if (localAge > totalAliveTime - IMP_FADE) {
                    alpha = (totalAliveTime - localAge) / IMP_FADE;
                } else {
                    alpha = 1.0f;
                }
                alpha = Mth.clamp(alpha, 0f, 1f);

                final int al = (int)(alpha * 210);
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

    private static void vSide(PoseStack.Pose pose, VertexConsumer v,
                              Vec3 a, Vec3 b, Vec3 off0, Vec3 off1,
                              int r, int g, int bl, int al) {
        Vec3 a0 = a.add(off0), a1 = a.add(off1);
        Vec3 b0 = b.add(off0), b1 = b.add(off1);

        bv(pose, v, a0, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, b0, r, g, bl, al);

        bv(pose, v, b0, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, a0, r, g, bl, al);
    }

    private static void crossQuad(PoseStack.Pose pose, VertexConsumer v,
                                  Vec3 a, Vec3 b, Vec3 axis, float w,
                                  int r, int g, int bl, int al) {
        float h  = w * 0.5f;
        Vec3 off = axis.scale(h);
        Vec3 a0  = a.add(off), a1 = a.subtract(off);
        Vec3 b0  = b.add(off), b1 = b.subtract(off);
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