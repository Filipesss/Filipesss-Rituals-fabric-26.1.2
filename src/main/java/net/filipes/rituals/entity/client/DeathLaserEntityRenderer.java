package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.DeathLaserEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class DeathLaserEntityRenderer extends EntityRenderer<DeathLaserEntity, DeathLaserEntityRenderer.DeathLaserRenderState> {

    public static class DeathLaserRenderState extends EntityRenderState {
        public Vec3 originLocal = Vec3.ZERO;
        public Vec3 direction = new Vec3(0, 0, 1);
        public float maxDistance = 0f;
        public float tipProgress = 0f;
        public float tailProgress = 0f;
        public float beamWidth = 0.22f;
        public int color = 0xFFFFFF44;
        public float hitDistance = -1f;
        public long seed = 0L;
        public float age = 0f;
    }

    private static final int SIDE_SPARK_COUNT = 90;

    private static final int SIDE_SPARK_SEGMENTS_MIN = 4;
    private static final int SIDE_SPARK_SEGMENTS_MAX = 7;

    private static final float SIDE_SPARK_SPEED_MIN = 1.0f;
    private static final float SIDE_SPARK_SPEED_MAX = 3.0f;

    private static final float SIDE_SPARK_LENGTH_MIN = 1.0f;
    private static final float SIDE_SPARK_LENGTH_MAX = 3.2f;

    private static final float SIDE_SPARK_WIDTH_MIN = 0.026f;
    private static final float SIDE_SPARK_WIDTH_MAX = 0.060f;

    private static final float SIDE_SPARK_OFFSET_MIN = 0.20f;
    private static final float SIDE_SPARK_OFFSET_MAX = 0.75f;

    private static final float SIDE_SPARK_FADE_DIST = 1.1f;
    private static final float SIDE_SPARK_WIGGLE = 0.035f;

    private static final int HIT_LINE_COUNT = 24;
    private static final float HIT_LINE_APPEAR_SPEED = 5.5f;
    private static final float HIT_LINE_MAX_LEN = 2.8f;
    private static final float HIT_LINE_BASE_WIDTH = 0.030f;
    private static final float HIT_LINE_FADE_DIST = 1.0f;

    public DeathLaserEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public DeathLaserRenderState createRenderState() {
        return new DeathLaserRenderState();
    }

    @Override
    public void extractRenderState(DeathLaserEntity entity, DeathLaserRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        Vec3 renderCenter = new Vec3(state.x, state.y, state.z);
        state.originLocal = entity.getLaserOrigin().subtract(renderCenter);

        Vec3 dir = entity.getLaserDirection();
        state.direction = dir.lengthSqr() < 1e-6 ? new Vec3(0, 0, 1) : dir.normalize();
        state.maxDistance = entity.getMaxDistance();
        state.beamWidth = entity.getBeamWidth();
        state.color = entity.getBeamColor();
        state.hitDistance = entity.getHitDistance();
        state.seed = entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits();
        state.age = entity.tickCount - 1 + partialTicks;

        float dist = entity.getMaxDistance();
        float speed = entity.getBeamSpeed();
        int grow = (int) Math.ceil(dist / speed);
        int hold = entity.getHoldTicks();

        state.tipProgress = Math.min(dist, Math.max(0f, state.age * speed));
        float shrinkStart = grow + hold;
        state.tailProgress = Math.max(0f, (state.age - shrinkStart) * speed);
    }

    @Override
    public boolean affectedByCulling(DeathLaserEntity e) {
        return false;
    }

    @Override
    protected float getShadowRadius(DeathLaserRenderState s) {
        return 0f;
    }

    @Override
    protected float getShadowStrength(DeathLaserRenderState s) {
        return 0f;
    }
    private static void drawWigglyBeam(
            PoseStack.Pose pose,
            VertexConsumer verts,
            Vec3 start,
            Vec3 dir,
            Vec3 right,
            Vec3 up,
            float length,
            float width,
            int color,
            float age,
            float phase,
            int segments,
            float wiggle
    ) {
        Vec3 prev = start;

        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float falloff = 1.0f - (t * 0.15f);

            float wobbleR = (float) Math.sin(age * 2.4f + phase + t * 5.4f) * wiggle * falloff;
            float wobbleU = (float) Math.cos(age * 2.1f + phase * 1.37f + t * 4.8f) * wiggle * falloff;

            Vec3 point = start
                    .add(dir.scale(length * t))
                    .add(right.scale(wobbleR))
                    .add(up.scale(wobbleU));

            drawPrismAutoOrient(pose, verts, prev, point, width, color);
            prev = point;
        }
    }

    @Override
    public void submit(DeathLaserRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {

        float beamVisible = state.tipProgress - state.tailProgress;
        boolean hitActive = state.hitDistance >= 0
                && state.tipProgress >= state.hitDistance
                && state.tailProgress < state.hitDistance + HIT_LINE_MAX_LEN;

        if (beamVisible <= 0.001f && !hitActive) {
            return;
        }

        final Vec3 O = state.originLocal;
        final Vec3 DIR = state.direction;
        final float W = state.beamWidth;
        final float age = state.age;

        Vec3 helper = Math.abs(DIR.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 rt = DIR.cross(helper);
        if (rt.lengthSqr() < 1e-6) {
            helper = new Vec3(0, 0, 1);
            rt = DIR.cross(helper);
        }
        final Vec3 RIGHT = rt.normalize();
        final Vec3 UP = RIGHT.cross(DIR).normalize();

        final RandomSource rng = RandomSource.create(state.seed);

        final float pulse = 0.78f + 0.22f * (float) Math.sin(age * 10.5f + (state.seed & 1023L) * 0.01f);
        final float jitter = 0.012f + 0.010f * (float) Math.sin(age * 17.0f + 2.0f);

        final float[] sparkBase = new float[SIDE_SPARK_COUNT];
        final float[] sparkSpeed = new float[SIDE_SPARK_COUNT];
        final float[] sparkLength = new float[SIDE_SPARK_COUNT];
        final float[] sparkWidth = new float[SIDE_SPARK_COUNT];
        final float[] sparkPhase = new float[SIDE_SPARK_COUNT];
        final float[] sparkOffset = new float[SIDE_SPARK_COUNT];
        final int[] sparkSegments = new int[SIDE_SPARK_COUNT];
        final float[] sparkDirMix = new float[SIDE_SPARK_COUNT];

        for (int i = 0; i < SIDE_SPARK_COUNT; i++) {
            sparkBase[i] = rng.nextFloat() * Math.max(0.0001f, state.maxDistance);
            sparkSpeed[i] = SIDE_SPARK_SPEED_MIN + rng.nextFloat() * (SIDE_SPARK_SPEED_MAX - SIDE_SPARK_SPEED_MIN);
            sparkLength[i] = SIDE_SPARK_LENGTH_MIN + rng.nextFloat() * (SIDE_SPARK_LENGTH_MAX - SIDE_SPARK_LENGTH_MIN);
            sparkWidth[i] = SIDE_SPARK_WIDTH_MIN + rng.nextFloat() * (SIDE_SPARK_WIDTH_MAX - SIDE_SPARK_WIDTH_MIN);
            sparkPhase[i] = rng.nextFloat() * ((float) Math.PI * 2.0f);
            sparkOffset[i] = SIDE_SPARK_OFFSET_MIN + rng.nextFloat() * (SIDE_SPARK_OFFSET_MAX - SIDE_SPARK_OFFSET_MIN);
            sparkSegments[i] = SIDE_SPARK_SEGMENTS_MIN + rng.nextInt(SIDE_SPARK_SEGMENTS_MAX - SIDE_SPARK_SEGMENTS_MIN + 1);
            sparkDirMix[i] = 0.65f + rng.nextFloat() * 0.25f;
        }

        final float[] hAngle = new float[HIT_LINE_COUNT];
        final float[] hPhi = new float[HIT_LINE_COUNT];
        final float[] hLen = new float[HIT_LINE_COUNT];
        final float[] hWidth = new float[HIT_LINE_COUNT];
        final float[] hColorT = new float[HIT_LINE_COUNT];

        if (hitActive) {
            RandomSource hr = RandomSource.create(state.seed ^ 0xDEADBEEFCAFEL);
            for (int i = 0; i < HIT_LINE_COUNT; i++) {
                hAngle[i] = (float) (hr.nextDouble() * Math.PI * 2.0);
                hPhi[i] = (float) ((hr.nextDouble() - 0.5) * Math.PI * 0.85);
                hLen[i] = HIT_LINE_MAX_LEN * (0.45f + (float) (hr.nextDouble() * 0.55f));
                hWidth[i] = HIT_LINE_BASE_WIDTH * (0.6f + (float) (hr.nextDouble() * 0.85f));
                hColorT[i] = (float) hr.nextDouble();
            }
        }

        final boolean doHit = hitActive;
        final float beamVis = beamVisible;

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, vertices) -> {
            if (beamVis > 0.001f) {
                Vec3 tail = O.add(DIR.scale(state.tailProgress));
                Vec3 tip = O.add(DIR.scale(state.tipProgress));

                drawCataclysmLikeBeam(
                        pose,
                        vertices,
                        tail,
                        tip,
                        RIGHT,
                        UP,
                        W,
                        state.color,
                        age,
                        pulse,
                        jitter
                );
            }

            for (int i = 0; i < SIDE_SPARK_COUNT; i++) {
                float travel = sparkBase[i] + age * sparkSpeed[i];

                if (state.maxDistance > 0.001f) {
                    travel %= state.maxDistance;
                }

                if (travel < state.tailProgress || travel > state.tipProgress) {
                    continue;
                }

                float tailGap = travel - state.tailProgress;
                float alphaScale = tailGap < SIDE_SPARK_FADE_DIST
                        ? Math.max(0f, tailGap / SIDE_SPARK_FADE_DIST)
                        : 1.0f;

                if (alphaScale < 0.02f) {
                    continue;
                }

                Vec3 attachPt = O.add(DIR.scale(travel));

                float sideShift = sparkOffset[i];
                float drift = (float) Math.sin(age * 0.9f + sparkPhase[i]) * (sideShift * 0.08f);
                float wobbleR = (float) Math.sin(age * 1.25f + sparkPhase[i]) * (sideShift * 0.10f);
                float wobbleU = (float) Math.cos(age * 1.10f + sparkPhase[i] * 1.37f) * (sideShift * 0.10f);

                Vec3 startPt = attachPt
                        .add(RIGHT.scale(sideShift + drift))
                        .add(UP.scale(wobbleU));

                Vec3 miniDir = DIR
                        .add(RIGHT.scale(wobbleR * sparkDirMix[i]))
                        .add(UP.scale((wobbleU * 0.45f) * sparkDirMix[i]))
                        .normalize();

                int outer = withAlpha(0xFF220000, (int) (255 * alphaScale));
                int core = withAlpha(0xFFFF2A2A, (int) (255 * alphaScale));

                drawWigglyBeam(
                        pose,
                        vertices,
                        startPt,
                        miniDir,
                        RIGHT,
                        UP,
                        sparkLength[i],
                        sparkWidth[i] * 1.9f,
                        outer,
                        age,
                        sparkPhase[i],
                        sparkSegments[i],
                        SIDE_SPARK_WIGGLE * 0.65f
                );

                drawWigglyBeam(
                        pose,
                        vertices,
                        startPt,
                        miniDir,
                        RIGHT,
                        UP,
                        sparkLength[i] * 0.98f,
                        sparkWidth[i],
                        core,
                        age,
                        sparkPhase[i],
                        sparkSegments[i],
                        SIDE_SPARK_WIGGLE * 0.35f
                );
            }

            if (doHit) {
                float hitAge = state.tipProgress - state.hitDistance;
                Vec3 hitPt = O.add(DIR.scale(state.hitDistance));

                float tailGap = state.hitDistance - state.tailProgress;
                float hitFade = tailGap < HIT_LINE_FADE_DIST
                        ? Math.max(0f, tailGap / HIT_LINE_FADE_DIST)
                        : 1.0f;

                for (int i = 0; i < HIT_LINE_COUNT; i++) {
                    float lineLen = Math.min(hLen[i], hitAge * HIT_LINE_APPEAR_SPEED);
                    if (lineLen < 0.02f) {
                        continue;
                    }

                    double cosP = Math.cos(hPhi[i]);
                    Vec3 lineDir = RIGHT.scale(Math.cos(hAngle[i]) * cosP)
                            .add(UP.scale(Math.sin(hAngle[i]) * cosP))
                            .add(DIR.scale(Math.sin(hPhi[i])));
                    lineDir = lineDir.normalize();

                    Vec3 endPt = hitPt.add(lineDir.scale(lineLen));
                    int hc = blendColor(0xFF7A1A1A, 0xFFFF3A3A, hColorT[i]);
                    hc = withAlpha(hc, (int) (230 * hitFade));
                    drawPrismAutoOrient(pose, vertices, hitPt, endPt, hWidth[i], hc);
                }

                int flare = withAlpha(0xFFFFD0A0, (int) (180 * hitFade));
                drawPrism(pose, vertices, hitPt, hitPt.add(RIGHT.scale(0.18)).add(UP.scale(0.18)), RIGHT, UP, W * 0.9f, flare);
                drawPrism(pose, vertices, hitPt, hitPt.add(RIGHT.scale(-0.18)).add(UP.scale(0.18)), RIGHT, UP, W * 0.9f, flare);
                drawPrism(pose, vertices, hitPt, hitPt.add(RIGHT.scale(0.18)).add(UP.scale(-0.18)), RIGHT, UP, W * 0.9f, flare);
                drawPrism(pose, vertices, hitPt, hitPt.add(RIGHT.scale(-0.18)).add(UP.scale(-0.18)), RIGHT, UP, W * 0.9f, flare);
            }
        });
    }

    private static void drawCataclysmLikeBeam(
            PoseStack.Pose pose,
            VertexConsumer verts,
            Vec3 tail,
            Vec3 tip,
            Vec3 right,
            Vec3 up,
            float baseWidth,
            int baseColor,
            float age,
            float pulse,
            float jitter
    ) {
        Vec3 axis = tip.subtract(tail);
        float len = (float) axis.length();
        if (len <= 1.0e-5f) {
            return;
        }

        Vec3 dir = axis.normalize();

        int haloColor = withAlpha(blendColor(baseColor, 0xFF2A0000, 0.72f), 40);
        int shellColor = withAlpha(blendColor(baseColor, 0xFFFF5A1F, 0.50f), 95);
        int coreColor = withAlpha(blendColor(baseColor, 0xFFFFB36B, 0.22f), 170);
        int whiteCore = withAlpha(tintWhite(baseColor, 0.72f), 235);

        int segments = Math.max(6, (int) (len / 0.55f));
        Vec3 prev = tail;

        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float taper = 1.0f - (0.62f * t);
            float w = baseWidth * taper;

            float waveA = (float) Math.sin(age * 8.0f + t * 18.0f) * jitter;
            float waveB = (float) Math.cos(age * 9.5f + t * 14.0f) * jitter;

            Vec3 point = tail
                    .add(dir.scale(len * t))
                    .add(right.scale(waveA))
                    .add(up.scale(waveB));

            drawPrismAutoOrient(pose, verts, prev, point, w * 3.8f * pulse, haloColor);
            drawPrismAutoOrient(pose, verts, prev, point, w * 2.1f * pulse, shellColor);
            drawPrismAutoOrient(pose, verts, prev, point, w * 1.05f * pulse, coreColor);
            drawPrismAutoOrient(pose, verts, prev, point, w * 0.48f, whiteCore);

            prev = point;
        }
    }

    private static void drawPrism(PoseStack.Pose pose, VertexConsumer verts,
                                  Vec3 a, Vec3 b, Vec3 right, Vec3 up, float width, int color) {
        if (a.subtract(b).lengthSqr() < 1e-8) {
            return;
        }

        float hw = width * 0.5f;
        Vec3 r = right.scale(hw), u = up.scale(hw);

        Vec3 a1 = a.add(r).add(u), a2 = a.subtract(r).add(u);
        Vec3 a3 = a.subtract(r).subtract(u), a4 = a.add(r).subtract(u);

        Vec3 b1 = b.add(r).add(u), b2 = b.subtract(r).add(u);
        Vec3 b3 = b.subtract(r).subtract(u), b4 = b.add(r).subtract(u);

        drawQuad(pose, verts, a1, a2, b2, b1, color);
        drawQuad(pose, verts, a2, a3, b3, b2, color);
        drawQuad(pose, verts, a3, a4, b4, b3, color);
        drawQuad(pose, verts, a4, a1, b1, b4, color);
    }

    private static void drawPrismAutoOrient(PoseStack.Pose pose, VertexConsumer verts,
                                            Vec3 a, Vec3 b, float width, int color) {
        Vec3 seg = b.subtract(a);
        if (seg.lengthSqr() < 1e-8) {
            return;
        }

        seg = seg.normalize();
        Vec3 helper = Math.abs(seg.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 rt = seg.cross(helper);
        if (rt.lengthSqr() < 1e-6) {
            helper = new Vec3(0, 0, 1);
            rt = seg.cross(helper);
        }

        Vec3 right = rt.normalize();
        Vec3 up = right.cross(seg).normalize();
        drawPrism(pose, verts, a, b, right, up, width, color);
    }

    private static void drawQuad(PoseStack.Pose pose, VertexConsumer verts,
                                 Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color) {
        int al = (color >>> 24) & 0xFF;
        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int bl = color & 0xFF;

        verts.addVertex(pose, (float) a.x, (float) a.y, (float) a.z).setColor(r, g, bl, al);
        verts.addVertex(pose, (float) b.x, (float) b.y, (float) b.z).setColor(r, g, bl, al);
        verts.addVertex(pose, (float) c.x, (float) c.y, (float) c.z).setColor(r, g, bl, al);
        verts.addVertex(pose, (float) d.x, (float) d.y, (float) d.z).setColor(r, g, bl, al);
    }

    private static int withAlpha(int color, int alpha) {
        return (clamp(alpha) << 24) | (color & 0x00FFFFFF);
    }

    private static int blendColor(int a, int b, float t) {
        int ar = (a >>> 16) & 0xFF, ag = (a >>> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >>> 16) & 0xFF, bg = (b >>> 8) & 0xFF, bb = b & 0xFF;
        return (lerp(ar, br, t) << 16) | (lerp(ag, bg, t) << 8) | lerp(ab, bb, t);
    }

    private static int tintWhite(int color, float amount) {
        int r = (color >>> 16) & 0xFF, g = (color >>> 8) & 0xFF, b = color & 0xFF;
        return (lerp(r, 255, amount) << 16) | (lerp(g, 255, amount) << 8) | lerp(b, 255, amount);
    }

    private static int lerp(int a, int b, float t) {
        return clamp(Math.round(a + (b - a) * t));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}