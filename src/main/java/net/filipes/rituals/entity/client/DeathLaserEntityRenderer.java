package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.DeathLaserEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes; // still used by lightning bolts
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class DeathLaserEntityRenderer extends EntityRenderer<DeathLaserEntity, DeathLaserEntityRenderer.LaserRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            "rituals", "textures/entity/death_laser_beam.png");

    private static final float TW          = 256f;
    private static final float TH          = 32f;
    private static final float SPRITE_HALF = 0.75f;
    private static final float BEAM_HALF   = 0.75f;

    public static class LaserRenderState extends EntityRenderState {
        float posX, posY, posZ;
        float colX, colY, colZ;
        float yawDeg, pitchDeg;
        float beamLength;
        int   frame;
        float alpha;
        float age;
        long  seed;
        Direction blockSide;
        boolean clearerView;
    }

    public DeathLaserEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public LaserRenderState createRenderState() { return new LaserRenderState(); }

    @Override
    public void extractRenderState(DeathLaserEntity e, LaserRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        double eX = e.xo + (e.getX() - e.xo) * pt;
        double eY = e.yo + (e.getY() - e.yo) * pt;
        double eZ = e.zo + (e.getZ() - e.zo) * pt;
        s.posX = (float)(eX - s.x);
        s.posY = (float)(eY - s.y);
        s.posZ = (float)(eZ - s.z);

        double cX = e.prevCollidePosX + (e.collidePosX - e.prevCollidePosX) * pt;
        double cY = e.prevCollidePosY + (e.collidePosY - e.prevCollidePosY) * pt;
        double cZ = e.prevCollidePosZ + (e.collidePosZ - e.prevCollidePosZ) * pt;
        s.colX = (float)(cX - s.x);
        s.colY = (float)(cY - s.y);
        s.colZ = (float)(cZ - s.z);

        float yaw   = e.prevRenderYaw   + (e.renderYaw   - e.prevRenderYaw)   * pt;
        float pitch = e.prevRenderPitch + (e.renderPitch - e.prevRenderPitch) * pt;
        s.yawDeg   = yaw   * (180f / Mth.PI);
        s.pitchDeg = pitch * (180f / Mth.PI);

        float dx = s.colX - s.posX, dy = s.colY - s.posY, dz = s.colZ - s.posZ;
        s.beamLength = Mth.sqrt(dx * dx + dy * dy + dz * dz);

        int f = Mth.floor((e.appearTimer - 1f + pt) * 2f);
        s.frame = (f < 0) ? 6 : Math.min(f, 6);
        s.alpha = Mth.clamp(e.appearTimer / 3f, 0f, 1f);

        s.age  = e.tickCount - 1 + pt;
        s.seed = e.getUUID().getMostSignificantBits() ^ e.getUUID().getLeastSignificantBits();

        s.blockSide   = e.blockSide;
        s.clearerView = e.caster instanceof Player
                && Minecraft.getInstance().player == e.caster
                && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    @Override public boolean affectedByCulling(DeathLaserEntity e) { return false; }
    @Override protected float getShadowRadius  (LaserRenderState s) { return 0f; }
    @Override protected float getShadowStrength(LaserRenderState s) { return 0f; }

    @Override
    public void submit(LaserRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

        if (s.alpha < 0.005f) return;

        final RenderType TEX      = RenderTypes.eyes(TEXTURE);
        final int         frame   = s.frame;
        final float       len     = s.beamLength;
        final boolean     fp      = s.clearerView;
        final float       yaw     = s.yawDeg;
        final float       pitch   = s.pitchDeg;
        final float       camXRot = Minecraft.getInstance().gameRenderer.getMainCamera().xRot();
        final Quaternionf camQuat = this.entityRenderDispatcher.camera.rotation();

        ps.pushPose();
        ps.translate(s.posX, s.posY, s.posZ);

        if (!fp) {
            ps.pushPose();
            ps.mulPose(camQuat);
            snc.submitCustomGeometry(ps, TEX, (pose, v) -> drawSprite(pose, v, frame));
            ps.popPose();
        }

        ps.pushPose();
        ps.mulPose(new Quaternionf().rotationX( 90f * Mth.DEG_TO_RAD));
        ps.mulPose(new Quaternionf().rotationZ((yaw - 90f) * Mth.DEG_TO_RAD));
        ps.mulPose(new Quaternionf().rotationX(-pitch * Mth.DEG_TO_RAD));
        {
            ps.pushPose();
            if (!fp) ps.mulPose(new Quaternionf().rotationY((camXRot + 90f) * Mth.DEG_TO_RAD));
            snc.submitCustomGeometry(ps, TEX, (pose, v) -> drawBeam(pose, v, frame, len, fp));
            ps.popPose();

            if (!fp) {
                ps.pushPose();
                ps.mulPose(new Quaternionf().rotationY((-camXRot - 90f) * Mth.DEG_TO_RAD));
                snc.submitCustomGeometry(ps, TEX, (pose, v) -> drawBeam(pose, v, frame, len, false));
                ps.popPose();
            }
        }
        ps.popPose();

        ps.pushPose();
        ps.translate(s.colX - s.posX, s.colY - s.posY, s.colZ - s.posZ);
        {
            ps.pushPose();
            ps.mulPose(camQuat);
            snc.submitCustomGeometry(ps, TEX, (pose, v) -> drawSprite(pose, v, frame));
            ps.popPose();

            if (s.blockSide != null) {
                ps.pushPose();
                Quaternionf sq = s.blockSide.getRotation();
                sq.mul(new Quaternionf().rotationX(90f * Mth.DEG_TO_RAD));
                ps.mulPose(sq);
                ps.translate(0.0, 0.0, -0.01);
                snc.submitCustomGeometry(ps, TEX, (pose, v) -> drawSprite(pose, v, frame));
                ps.popPose();
            }
        }
        ps.popPose();

        ps.popPose();

        if (s.age > 20f && len > 0.1f) {
            drawLightningBolts(s, ps, snc);
            drawImpactBolts(s, ps, snc);
        }
    }

    private static void drawSprite(PoseStack.Pose pose, VertexConsumer v, int frame) {
        float u0 = (16f * frame) / TW, u1 = u0 + 16f / TW;
        float v0 = 0f, v1 = 16f / TH;
        float r  = SPRITE_HALF;
        vert(pose, v, -r, -r, 0, u0, v0);
        vert(pose, v, -r,  r, 0, u0, v1);
        vert(pose, v,  r,  r, 0, u1, v1);
        vert(pose, v,  r, -r, 0, u1, v0);
        vert(pose, v,  r, -r, 0, u1, v0);
        vert(pose, v,  r,  r, 0, u1, v1);
        vert(pose, v, -r,  r, 0, u0, v1);
        vert(pose, v, -r, -r, 0, u0, v0);
    }

    private static void drawBeam(PoseStack.Pose pose, VertexConsumer v,
                                 int frame, float length, boolean fp) {
        float u0  = 0f, u1  = 20f / TW;
        float vv0 = 16f / TH + (frame / TH), vv1 = vv0 + 1f / TH;
        float off = fp ? -1f : 0f;
        vert(pose, v, -BEAM_HALF, off,    0, u0, vv0);
        vert(pose, v, -BEAM_HALF, length, 0, u0, vv1);
        vert(pose, v,  BEAM_HALF, length, 0, u1, vv1);
        vert(pose, v,  BEAM_HALF, off,    0, u1, vv0);
        vert(pose, v,  BEAM_HALF, off,    0, u1, vv0);
        vert(pose, v,  BEAM_HALF, length, 0, u1, vv1);
        vert(pose, v, -BEAM_HALF, length, 0, u0, vv1);
        vert(pose, v, -BEAM_HALF, off,    0, u0, vv0);
    }

    private static void vert(PoseStack.Pose pose, VertexConsumer v,
                             float x, float y, float z, float u, float vc) {
        v.addVertex(pose, x, y, z)
                .setColor(1f, 1f, 1f, 1f)
                .setUv(u, vc)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(0f, 0f, 1f);
    }

    private static final int   BOLT_PAIRS    = 6;
    private static final int   UPDATE_TICKS  = 1;

    private static void drawLightningBolts(LaserRenderState s, PoseStack ps, SubmitNodeCollector snc) {
        Vec3 from   = new Vec3(s.posX, s.posY, s.posZ);
        Vec3 to     = new Vec3(s.colX, s.colY, s.colZ);
        Vec3 rawDir = to.subtract(from);
        if (rawDir.lengthSqr() < 1e-6) return;

        final Vec3 dir    = rawDir.normalize();
        final Vec3 helper = (Math.abs(dir.y) > 0.95) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        final Vec3 right0 = dir.cross(helper).normalize();
        final Vec3 up0    = right0.cross(dir).normalize();

        final float age  = s.age;
        final float len  = s.beamLength;
        final long  seed = s.seed;

        for (int p = 0; p < BOLT_PAIRS; p++) {
            double angle  = p * (Math.PI / BOLT_PAIRS);
            double cosA   = Math.cos(angle), sinA = Math.sin(angle);
            Vec3   right  = right0.scale(cosA).add(up0.scale(sinA));
            Vec3   up     = right0.scale(-sinA).add(up0.scale(cosA));

            long seedA = seed ^ (p * 0x9E3779B97F4A7C15L);
            long seedB = seedA ^ 0xDEADBEEFCAFEBABEL;
            long seedC = seedA ^ 0x1A2B3C4D5E6F7A8BL;

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawBolt(pose, v, from, dir, right, up, len,
                            0.10f, 255, 26, 0, 230, age, seedA));
            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawBolt(pose, v, from, dir, right, up, len,
                            0.07f, 249, 194, 43, 179, age, seedB));
            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) ->
                    drawBolt(pose, v, from, dir, right, up, len,
                            0.06f, 200, 0, 0, 200, age, seedC));
        }
    }

    private static void drawBolt(PoseStack.Pose pose, VertexConsumer v,
                                 Vec3 origin, Vec3 dir, Vec3 right, Vec3 up,
                                 float length, float width,
                                 int r, int g, int b, int al,
                                 float age, long seed) {
        if (length < 0.1f) return;

        int timeSlot = (int)(age / UPDATE_TICKS);
        int knots    = Math.max(1, (int)(length / 4.0f));

        Vec3[] pts = new Vec3[knots + 2];
        pts[0]         = origin;
        pts[knots + 1] = origin.add(dir.scale(length));

        for (int i = 1; i <= knots; i++) {
            float t      = i / (float)(knots + 1);
            float env    = (float) Math.sin(t * Math.PI);
            float maxOff = Math.min(length * 0.22f, 0.9f);

            float offR = hash(seed + i * 997L  + timeSlot * 7919L) * env * maxOff;
            float offU = hash(seed + i * 1009L + timeSlot * 6271L) * env * maxOff;

            pts[i] = origin
                    .add(dir.scale(length * t))
                    .add(right.scale(offR))
                    .add(up.scale(offU));
        }

        for (int i = 0; i < pts.length - 1; i++) {
            crossQuad(pose, v, pts[i], pts[i + 1], right, width, r, g, b, al);
            crossQuad(pose, v, pts[i], pts[i + 1], up,    width, r, g, b, al);
        }
    }


    // ── Impact bolts ─────────────────────────────────────────────────────────

    private static final int   IMP_NUM_SPARKS = 20;
    private static final float IMP_CYCLE      = 10f;   // ticks per fire cycle
    private static final int   IMP_NUM_SEGS   = 3;     // segments per spark
    private static final float IMP_FADE       = 1.0f;  // ticks to fade each segment in/out

    private static void drawImpactBolts(LaserRenderState s, PoseStack ps, SubmitNodeCollector snc) {
        Vec3 impact = new Vec3(s.colX, s.colY, s.colZ);

        Vec3 normal;
        if (s.blockSide != null) {
            normal = new Vec3(s.blockSide.getStepX(),
                    s.blockSide.getStepY(),
                    s.blockSide.getStepZ());
        } else {
            Vec3 beamDir = impact.subtract(new Vec3(s.posX, s.posY, s.posZ));
            normal = beamDir.lengthSqr() > 1e-6 ? beamDir.normalize() : new Vec3(0, 1, 0);
        }

        Vec3 helper = (Math.abs(normal.y) > 0.95) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 axisA  = normal.cross(helper).normalize();
        Vec3 axisB  = normal.cross(axisA).normalize();

        // Alive window: one tick per segment to reveal + one fade-out tick
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

            // Longer sparks, much less lateral wiggle
            float totalLen = 1.5f + (hash(shapeSeed + 1L) * 0.5f + 0.5f) * 2.0f;

            Vec3[] pts = new Vec3[IMP_NUM_SEGS + 1];
            pts[0] = impact;
            for (int k = 1; k <= IMP_NUM_SEGS; k++) {
                float t      = k / (float) IMP_NUM_SEGS;
                float env    = (float) Math.sin(t * Math.PI);
                float maxOff = totalLen * 0.12f;   // subtle deviation only
                float offP   = hash(shapeSeed + k * 997L)  * env * maxOff;
                float offN   = hash(shapeSeed + k * 1009L) * env * maxOff;
                pts[k] = impact
                        .add(boltDir.scale(totalLen * t))
                        .add(perp.scale(offP))
                        .add(normal.scale(offN));
            }


            final double sqrt3over2 = Math.sqrt(3.0) / 2.0;
            final Vec3 tubeA = perp;
            final Vec3 tubeB = perp.scale(-0.5).add(normal.scale(sqrt3over2));

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

                final int al = (int)(alpha * 230);
                if (al < 4) continue;

                final Vec3 a = pts[seg];
                final Vec3 b = pts[seg + 1];
                final Vec3 tA = tubeA, tB = tubeB;

                snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) -> {
                    crossQuad(pose, v, a, b, tA, 0.09f, 255, 80, 0, al);
                    crossQuad(pose, v, a, b, tB, 0.09f, 255, 80, 0, al);
                });
            }
        }
    }

    private static long rehash(long seed) {
        seed ^= (seed >>> 33);
        seed *= 0xFF51AFD7ED558CCDL;
        seed ^= (seed >>> 33);
        seed *= 0xC4CEB9FE1A85EC53L;
        seed ^= (seed >>> 33);
        return seed;
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
}