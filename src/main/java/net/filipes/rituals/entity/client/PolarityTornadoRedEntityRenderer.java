package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.client.PolarityTornadoRedModel;
import net.filipes.rituals.entity.custom.PolarityTornadoRedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class PolarityTornadoRedEntityRenderer
        extends EntityRenderer<PolarityTornadoRedEntity, PolarityTornadoRedEntityRenderer.TornadoRenderState> {

    private static final int TRAIL_LENGTH = 60;
    private static final int STRIP_COUNT = 5;
    private static final float MAX_SPREAD = 0.55f;
    private static final float STRIP_HALF_HEIGHT = 0.22f;
    private static final float TRAIL_Y_OFFSET = 0.6f;
    private static final double MOVEMENT_THRESHOLD_SQ = 1e-5;
    private static final int TRAIL_R = 80, TRAIL_G = 140, TRAIL_B = 255, TRAIL_A = 210;

    private final Map<Integer, TornadoTrailManager> entityTrails        = new HashMap<>();
    private final Map<Integer, Vec3>                lastEntityPositions = new HashMap<>();

    private final PolarityTornadoRedModel model;

    public static class TornadoRenderState extends EntityRenderState {
        public float ageInTicks;
        public float visualScale;
        public TornadoTrailManager.Entry[] trail;
    }

    public PolarityTornadoRedEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PolarityTornadoRedModel(
                context.bakeLayer(PolarityTornadoRedModel.LAYER));
    }

    @Override
    public TornadoRenderState createRenderState() {
        return new TornadoRenderState();
    }

    @Override
    public void extractRenderState(PolarityTornadoRedEntity entity,
                                   TornadoRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        float age   = entity.tickCount + partialTick;
        float scale = entity.getVisualScale();
        state.ageInTicks  = age;
        state.visualScale = scale;

        int  id         = entity.getId();
        Vec3 currentPos = entity.position();

        boolean isMoving = false;
        if (lastEntityPositions.containsKey(id)) {
            isMoving = currentPos.distanceToSqr(lastEntityPositions.get(id))
                    > MOVEMENT_THRESHOLD_SQ;
        }
        lastEntityPositions.put(id, currentPos);

        TornadoTrailManager manager =
                entityTrails.computeIfAbsent(id, i -> new TornadoTrailManager(TRAIL_LENGTH));

        manager.push(currentPos, isMoving);
        state.trail = manager.getEntries();
    }

    @Override
    public boolean shouldRender(PolarityTornadoRedEntity entity,
                                Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(TornadoRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector collector,
                       CameraRenderState camera) {

        MultiBufferSource bufferSource = Minecraft.getInstance()
                .renderBuffers()
                .bufferSource();

        poseStack.pushPose();
        poseStack.scale(state.visualScale, state.visualScale, state.visualScale);
        model.render(poseStack, bufferSource, 15728880, state.ageInTicks);
        poseStack.popPose();

        TornadoTrailManager.Entry[] trail = state.trail;
        if (trail.length < 2) return;

        boolean anyMoving = false;
        for (TornadoTrailManager.Entry e : trail) {
            if (e.wasMoving()) { anyMoving = true; break; }
        }
        if (!anyMoving) return;

        VertexConsumer vc  = bufferSource.getBuffer(RenderTypes.lightning());
        Vec3     camPos    = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Matrix4f mat       = poseStack.last().pose();
        float    scale     = state.visualScale;

        renderTrailStrips(trail, camPos, mat, vc, scale);
    }

    private void renderTrailStrips(TornadoTrailManager.Entry[] entries,
                                   Vec3 camPos,
                                   Matrix4f mat,
                                   VertexConsumer vc,
                                   float scale) {

        int n = entries.length;

        float[] px = new float[n], py = new float[n], pz = new float[n];
        float yOff = TRAIL_Y_OFFSET * scale;
        for (int i = 0; i < n; i++) {
            Vec3 p = entries[i].pos();
            px[i] = (float)(p.x - camPos.x);
            py[i] = (float)(p.y - camPos.y) + yOff;
            pz[i] = (float)(p.z - camPos.z);
        }

        float[] ldx = new float[n - 1], ldz = new float[n - 1];
        for (int i = 0; i < n - 1; i++) {
            float dx = px[i + 1] - px[i];
            float dz = pz[i + 1] - pz[i];
            float lx = -dz, lz = dx;
            float len = (float) Math.sqrt(lx * lx + lz * lz);
            if (len < 1e-6f) {
                ldx[i] = (i > 0) ? ldx[i - 1] : 1f;
                ldz[i] = (i > 0) ? ldz[i - 1] : 0f;
            } else {
                ldx[i] = lx / len;
                ldz[i] = lz / len;
            }
        }
        for (int i = 1; i < n - 1; i++) {
            float dot = ldx[i] * ldx[i - 1] + ldz[i] * ldz[i - 1];
            if (dot < 0f) { ldx[i] = -ldx[i]; ldz[i] = -ldz[i]; }
        }

        float maxSpread  = MAX_SPREAD        * scale;
        float halfH      = STRIP_HALF_HEIGHT * scale;

        for (int j = 0; j < STRIP_COUNT; j++) {
            float t = STRIP_COUNT == 1 ? 0.5f : (float) j / (STRIP_COUNT - 1);
            float baseLateral = lerp(-maxSpread, maxSpread, t);

            for (int i = 0; i < n - 1; i++) {
                if (!entries[i].wasMoving() && !entries[i + 1].wasMoving()) continue;

                float convA = 1f - (float) i       / (n - 1);
                float convB = 1f - (float)(i + 1)  / (n - 1);

                float lx = ldx[i], lz = ldz[i];

                float laA = baseLateral * convA;
                float laB = baseLateral * convB;

                float ax = px[i]     + lx * laA,  ay = py[i],     az = pz[i]     + lz * laA;
                float bx = px[i + 1] + lx * laB,  by = py[i + 1], bz = pz[i + 1] + lz * laB;

                addVertex(mat, vc, ax, ay + halfH, az);
                addVertex(mat, vc, ax, ay - halfH, az);
                addVertex(mat, vc, bx, by - halfH, bz);
                addVertex(mat, vc, bx, by + halfH, bz);

                addVertex(mat, vc, bx, by + halfH, bz);
                addVertex(mat, vc, bx, by - halfH, bz);
                addVertex(mat, vc, ax, ay - halfH, az);
                addVertex(mat, vc, ax, ay + halfH, az);
            }
        }
    }

    private void addVertex(Matrix4f mat, VertexConsumer vc, float x, float y, float z) {
        vc.addVertex(mat, x, y, z)
                .setColor(TRAIL_R, TRAIL_G, TRAIL_B, TRAIL_A);
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    @Override
    protected float getShadowRadius(TornadoRenderState state) {
        return 0.4f * state.visualScale;
    }

    @Override
    protected float getShadowStrength(TornadoRenderState state) {
        return 0.5f;
    }
}