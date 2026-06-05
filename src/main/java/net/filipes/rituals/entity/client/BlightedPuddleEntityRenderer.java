package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.BlightedPuddleEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

import java.util.Random;

public class BlightedPuddleEntityRenderer
        extends EntityRenderer<BlightedPuddleEntity, BlightedPuddleEntityRenderer.PuddleRenderState> {

    private static final int FRAME_COUNT     = 6;
    private static final int TICKS_PER_FRAME = 6;
    private static final int LAYER_COUNT     = 2;

    private static final RenderType[] RENDER_TYPES = new RenderType[FRAME_COUNT];

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            RENDER_TYPES[i] = RenderTypes.eyes(
                    Identifier.fromNamespaceAndPath("rituals", "textures/entity/blighted_puddle_" + i + ".png")
            );
        }
    }

    public static class PuddleRenderState extends EntityRenderState {
        float alpha;
        float scale;
        // all per-layer data derived from entity-ID seed — consistent across frames
        int[]   frame       = new int[LAYER_COUNT];
        float[] radius      = new float[LAYER_COUNT];
        float[] rotation    = new float[LAYER_COUNT];
        float[] alphaFactor = new float[LAYER_COUNT];
        float[] offsetX = new float[LAYER_COUNT];
        float[] offsetZ = new float[LAYER_COUNT];
        int[]   r           = new int[LAYER_COUNT];
        int[]   g           = new int[LAYER_COUNT];
        int[]   b           = new int[LAYER_COUNT];
    }

    private static final float BASE_RADIUS = 1f;

    public BlightedPuddleEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public PuddleRenderState createRenderState() { return new PuddleRenderState(); }

    @Override
    public void extractRenderState(BlightedPuddleEntity e, PuddleRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        float tick = e.tickCount + pt;

        // Fade in / steady / fade out
        float fade;
        if (tick < BlightedPuddleEntity.FADE_TICKS) {
            fade = tick / BlightedPuddleEntity.FADE_TICKS;
        } else if (tick > BlightedPuddleEntity.LIFETIME - BlightedPuddleEntity.FADE_TICKS) {
            fade = (BlightedPuddleEntity.LIFETIME - tick) / BlightedPuddleEntity.FADE_TICKS;
        } else {
            fade = 1.0f;
        }

        float pulse = 0.92f + 0.08f * (float) Math.sin(tick * 0.09f);
        s.alpha = Math.max(0f, Math.min(1f, fade * pulse));
        s.scale = Math.min(1f, tick / BlightedPuddleEntity.FADE_TICKS);

        // Seed with entity ID — same puddle always gets the same layer layout,
        // but different puddles look distinct. The rng call ORDER below must never change.
        Random rng = new Random(e.getId());

        for (int i = 0; i < LAYER_COUNT; i++) {
            // Random starting frame offset lets each layer begin mid-cycle
            int frameOffset  = rng.nextInt(FRAME_COUNT);
            s.frame[i]       = (e.tickCount / TICKS_PER_FRAME + frameOffset) % FRAME_COUNT;

            s.radius[i] = BASE_RADIUS * (0.4f + rng.nextFloat() * 1.4f);

            // Each layer rotates at its own random speed and direction
            float rotSpeed   = 0.7f + rng.nextFloat() * 1.5f;
            float rotDir     = rng.nextBoolean() ? 1f : -1f;
            float rotOffset  = rng.nextFloat() * 360f;
            s.rotation[i]    = tick * rotSpeed * rotDir + rotOffset;

            // Per-layer opacity variation
            s.alphaFactor[i] = 0.55f + rng.nextFloat() * 0.45f;
            s.offsetX[i] = (rng.nextFloat() - 0.5f) * 0.8f;
            s.offsetZ[i] = (rng.nextFloat() - 0.5f) * 0.8f;

            // Colour stays in the sickly green family but varies layer to layer
            s.r[i] = 45  + rng.nextInt(60);   // 45  – 104
            s.g[i] = 95  + rng.nextInt(70);   // 95  – 164
            s.b[i] = 15  + rng.nextInt(35);   // 15  – 49
        }
    }

    @Override public boolean affectedByCulling(BlightedPuddleEntity e) { return false; }
    @Override protected float getShadowRadius  (PuddleRenderState s)   { return 0f; }
    @Override protected float getShadowStrength(PuddleRenderState s)   { return 0f; }

    @Override
    public void submit(PuddleRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {
        if (s.alpha <= 0f) return;

        ps.pushPose();
        ps.translate(0, 0.01f, 0); // lift off ground to avoid z-fighting with terrain

        for (int i = 0; i < LAYER_COUNT; i++) {
            float layerY = i * 0.003f; // tiny offset so layers don't z-fight each other
            float radius = s.radius[i] * s.scale;
            int   alpha  = (int)(s.alpha * 200 * s.alphaFactor[i]);

            renderLayer(
                    ps,
                    snc,
                    s.rotation[i],
                    radius,
                    alpha,
                    s.r[i],
                    s.g[i],
                    s.b[i],
                    layerY,
                    s.offsetX[i],
                    s.offsetZ[i],
                    RENDER_TYPES[s.frame[i]]
            );
        }

        ps.popPose();
    }

    private void renderLayer(
            PoseStack ps,
            SubmitNodeCollector snc,
            float rotation,
            float radius,
            int alpha,
            int r, int g, int b,
            float yOffset,
            float offsetX,
            float offsetZ,
            RenderType renderType) {

        ps.pushPose();
        ps.translate(offsetX, yOffset, offsetZ);
        ps.mulPose(Axis.YP.rotationDegrees(rotation));


        snc.submitCustomGeometry(ps, renderType, (pose, v) -> {
            vertex(pose, v, -radius, 0, -radius, 0f, 0f, alpha, r, g, b);
            vertex(pose, v,  radius, 0, -radius, 1f, 0f, alpha, r, g, b);
            vertex(pose, v,  radius, 0,  radius, 1f, 1f, alpha, r, g, b);
            vertex(pose, v, -radius, 0,  radius, 0f, 1f, alpha, r, g, b);

            vertex(pose, v, -radius, 0,  radius, 0f, 1f, alpha, r, g, b);
            vertex(pose, v,  radius, 0,  radius, 1f, 1f, alpha, r, g, b);
            vertex(pose, v,  radius, 0, -radius, 1f, 0f, alpha, r, g, b);
            vertex(pose, v, -radius, 0, -radius, 0f, 0f, alpha, r, g, b);
        });

        ps.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer v,
                               float x, float y, float z,
                               float u, float vv, int alpha,
                               int r, int g, int b) {
        v.addVertex(pose, x, y, z)
                .setColor(r, g, b, alpha)
                .setUv(u, vv)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0f, 1f, 0f);
    }
}