package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.filipes.rituals.entity.custom.LightningStrikeEntity;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LightningStrikeEntityRenderer
        extends EntityRenderer<LightningStrikeEntity, LightningStrikeEntityRenderer.StrikeRenderState> {

    private static final RenderPipeline STRIKE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("rituals", "lightning_strike_trail"))
                    .withVertexShader("core/rendertype_lightning")
                    .withFragmentShader("core/rendertype_lightning")
                    .withColorTargetState(new ColorTargetState(
                            new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ONE, DestFactor.ZERO)
                    ))
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .withCull(false)
                    .build()
    );

    private static final RenderType STRIKE_TRAIL = RenderType.create(
            "lightning_strike_trail",
            RenderSetup.builder(STRIKE_PIPELINE)
                    .sortOnUpload()
                    .setOutputTarget(OutputTarget.WEATHER_TARGET)
                    .createRenderSetup()
    );


    public static class StrikeRenderState extends EntityRenderState {
        float posX, posY, posZ;
        float height;
        int   r, g, b;
        float alpha;
        float age;
        long  seed;
    }

    private static final int   BOLT_UPDATE_TICKS = 1;
    private static final int   STRAND_COUNT      = 5;
    private static final float BOTTOM_EXTEND     = 1.2f;
    private static final float BOLT_WIDTH        = 0.13f;
    private static final int   PEAK_ALPHA        = 95;

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


        final int   mainAlpha = (int)(s.alpha * PEAK_ALPHA);
        final float age       = s.age;
        final long  seed      = s.seed;
        final float height    = s.height;

        ps.pushPose();
        ps.translate(s.posX, s.posY, s.posZ);

        for (int strand = 0; strand < STRAND_COUNT; strand++) {
            final long strandSeed = seed ^ ((long)strand * 0x9E3779B97F4A7C15L);

            snc.submitCustomGeometry(ps, STRIKE_TRAIL, (pose, v) ->
                    drawVerticalBolt(pose, v, height, BOLT_WIDTH,
                            s.r, s.g, s.b, mainAlpha, age, strandSeed));
        }

        ps.popPose();
    }


    private static void drawVerticalBolt(PoseStack.Pose pose, VertexConsumer v,
                                         float height, float width,
                                         int r, int g, int bl, int alpha,
                                         float age, long seed) {
        if (alpha < 2 || height < 0.1f) return;

        int   timeSlot = (int)(age / BOLT_UPDATE_TICKS);
        float span     = height + BOTTOM_EXTEND;
        int   knots    = Mth.clamp((int)(span / 3.5f), 3, 12);

        Vec3[] pts = new Vec3[knots + 2];
        pts[0]         = new Vec3(0, -BOTTOM_EXTEND, 0);
        pts[knots + 1] = new Vec3(0,  height,        0);

        for (int i = 1; i <= knots; i++) {
            float t = i / (float)(knots + 1);

            float env    = (float) Math.sin(t * Math.PI * 0.7f);
            float maxOff = Math.min(span * 0.10f, 1.4f);

            float offX = hash(seed + i * 997L  + timeSlot * 7919L) * env * maxOff;
            float offZ = hash(seed + i * 1009L + timeSlot * 6271L) * env * maxOff;

            pts[i] = new Vec3(offX, -BOTTOM_EXTEND + span * t, offZ);
        }

        final float COS30 = 0.866f;
        final float SIN30 = 0.5f;

        for (int i = 0; i < pts.length - 1; i++) {
            Vec3 a   = pts[i];
            Vec3 b   = pts[i + 1];
            Vec3 dir = b.subtract(a);
            if (dir.lengthSqr() < 1e-8) continue;
            dir = dir.normalize();

            float ext = width * 0.25f;
            a = a.subtract(dir.scale(ext));
            b = b.add(dir.scale(ext));

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