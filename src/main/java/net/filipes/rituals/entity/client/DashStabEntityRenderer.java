package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.DashStabEntity;
import net.filipes.rituals.entity.custom.DashStabEntity.StabData;
import net.filipes.rituals.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

public class DashStabEntityRenderer extends EntityRenderer<DashStabEntity, DashStabEntityRenderer.DashStabRenderState> {

    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState itemRenderState = new ItemStackRenderState();

    public static class DashStabRenderState extends EntityRenderState {
        public float[] progresses  = new float[DashStabEntity.STAB_COUNT];
        public float[] originX     = new float[DashStabEntity.STAB_COUNT];
        public float[] originY     = new float[DashStabEntity.STAB_COUNT];
        public float[] originZ     = new float[DashStabEntity.STAB_COUNT];
        public float[] angles      = new float[DashStabEntity.STAB_COUNT];
        public float[] randomScale = new float[DashStabEntity.STAB_COUNT];
        public float[] randomYaw   = new float[DashStabEntity.STAB_COUNT];
        public float[] randomLift  = new float[DashStabEntity.STAB_COUNT];
        public float[] randomTilt  = new float[DashStabEntity.STAB_COUNT];
        public int stabCount;
        public int stage;
        // Trail
        public float trailEndX, trailEndZ;
        public float trailAlpha;
        public long  trailSeed;
    }

    public DashStabEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemModelResolver = ctx.getItemModelResolver();
    }

    @Override public DashStabRenderState createRenderState() { return new DashStabRenderState(); }

    @Override
    public void extractRenderState(DashStabEntity e, DashStabRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        s.stabCount = e.stabs.size();
        Level level = Minecraft.getInstance().level;

        for (int i = 0; i < s.stabCount; i++) {
            StabData d = e.stabs.get(i);
            s.progresses[i] = e.getStabProgress(d, pt);
            s.originX[i]    = d.originX - (float) s.x;
            s.originZ[i]    = d.originZ - (float) s.z;
            s.angles[i]     = d.angle;

            float surfaceY = d.originY;
            if (level != null) surfaceY = getSurfaceY(level, d.originX, d.originZ);
            s.originY[i] = surfaceY - (float) s.y;

            long seed = 31L * e.getId() + i * 1009L;
            java.util.Random rand = new java.util.Random(seed);
            s.randomScale[i] = 0.75f + rand.nextFloat() * 0.55f;
            s.randomYaw[i]   = (rand.nextFloat() - 0.5f) * 40f * Mth.DEG_TO_RAD;
            s.randomLift[i]  = (rand.nextFloat() - 0.5f) * 0.5f;
            s.randomTilt[i]  = (rand.nextFloat() - 0.5f) * 50f * Mth.DEG_TO_RAD;
        }

        s.stage = e.getOwnerStage();
        s.trailEndX = e.getEndX() - (float) s.x;
        s.trailEndZ = e.getEndZ() - (float) s.z;

        float age = e.tickCount - 1 + pt;
        float FADE_IN = 3f, HOLD = 8f, FADE_OUT = 10f;
        if      (age < FADE_IN)              s.trailAlpha = age / FADE_IN;
        else if (age < FADE_IN + HOLD)       s.trailAlpha = 1f;
        else                                 s.trailAlpha = 1f - ((age - FADE_IN - HOLD) / FADE_OUT);
        s.trailAlpha = Mth.clamp(s.trailAlpha, 0f, 1f);

        s.trailSeed = e.getId() * 0x9E3779B97F4A7C15L;
    }

    @Override public boolean affectedByCulling(DashStabEntity e)       { return false; }
    @Override protected float getShadowRadius  (DashStabRenderState s) { return 0f; }
    @Override protected float getShadowStrength(DashStabRenderState s) { return 0f; }

    @Override
    public void submit(DashStabRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

        if (s.trailAlpha > 0.01f) drawTrail(s, ps, snc);

        ItemStack stack = ModDataComponents.withStage(new ItemStack(ModItems.PHARATHORN), s.stage);

        for (int i = 0; i < s.stabCount; i++) {
            float progress = s.progresses[i];
            if (progress < 0.001f) continue;

            ps.pushPose();
            ps.translate(s.originX[i], s.originY[i], s.originZ[i]);

            float emergeOffset = 1.6f * (1f - progress);
            float baseLift     = 0.9f + s.randomLift[i];
            ps.translate(0f, -emergeOffset + baseLift, 0f);

            ps.mulPose(new Quaternionf().rotationY(-s.angles[i] + s.randomYaw[i]));
            ps.mulPose(new Quaternionf().rotationZ(s.randomTilt[i]));
            ps.mulPose(new Quaternionf().rotationX(-90f * Mth.DEG_TO_RAD));
            ps.mulPose(new Quaternionf().rotationX(90f * Mth.DEG_TO_RAD));
            ps.scale(1.3f * s.randomScale[i], 1.3f * s.randomScale[i], 1.3f * s.randomScale[i]);

            itemRenderState.clear();
            itemModelResolver.updateForTopItem(
                    itemRenderState, stack, ItemDisplayContext.FIXED,
                    Minecraft.getInstance().level, null, i);
            itemRenderState.submit(ps, snc, LightCoordsUtil.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, 0);

            ps.popPose();
        }
    }

    private static void drawTrail(DashStabRenderState s, PoseStack ps, SubmitNodeCollector snc) {
        Vec3 from = new Vec3(0, 0.9, 0);
        Vec3 to   = new Vec3(s.trailEndX, 0.9, s.trailEndZ);
        Vec3 raw  = to.subtract(from);
        if (raw.lengthSqr() < 1e-6) return;

        Vec3  dir    = raw.normalize();
        float len    = (float) raw.length();
        Vec3  helper = Math.abs(dir.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3  right  = dir.cross(helper).normalize();
        Vec3  up     = right.cross(dir).normalize();

        int baseAlpha = (int)(s.trailAlpha * 220);

        snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                (pose, v) -> drawTrailBolt(pose, v, from, dir, right, up, len,
                        0.12f, 255, 240, 160, baseAlpha, s.trailSeed));

        snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                (pose, v) -> drawTrailBolt(pose, v, from, dir, right, up, len,
                        0.20f, 255, 140, 0, (int)(baseAlpha * 0.75f), s.trailSeed ^ 0xDEADBEEFL));

        snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                (pose, v) -> drawTrailBolt(pose, v, from, dir, right, up, len,
                        0.35f, 180, 80, 0, (int)(baseAlpha * 0.4f), s.trailSeed ^ 0xCAFEBABEL));

        for (int i = 0; i < 4; i++) {
            long  seed        = s.trailSeed ^ (i * 0x6C62272E07BB0142L);
            float tOffset     = trailHash(seed) * 0.5f + 0.5f;
            float sparkLen    = 0.6f + (trailHash(seed + 1L) * 0.5f + 0.5f) * 1.2f;
            float angle       = trailHash(seed + 2L) * (float) Math.PI;
            Vec3  sparkDir    = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vec3  sparkOrigin = from.add(dir.scale(len * tOffset));
            long  sFinal      = seed;
            snc.submitCustomGeometry(ps, RenderTypes.lightning(),
                    (pose, v) -> drawTrailBolt(pose, v, sparkOrigin, sparkDir,
                            right, up, sparkLen,
                            0.07f, 255, 200, 80, (int)(baseAlpha * 0.7f), sFinal));
        }
    }

    private static void drawTrailBolt(PoseStack.Pose pose, VertexConsumer v,
                                      Vec3 origin, Vec3 dir, Vec3 right, Vec3 up,
                                      float length, float width,
                                      int r, int g, int b, int al, long seed) {
        if (length < 0.05f || al < 4) return;
        int knots = Math.max(1, (int)(length / 1.5f));

        Vec3[] pts = new Vec3[knots + 2];
        pts[0]         = origin;
        pts[knots + 1] = origin.add(dir.scale(length));

        for (int i = 1; i <= knots; i++) {
            float t      = i / (float)(knots + 1);
            float env    = (float) Math.sin(t * Math.PI);
            float maxOff = Math.min(length * 0.15f, 0.5f);
            float offR   = trailHash(seed + i * 997L)  * env * maxOff;
            float offU   = trailHash(seed + i * 1009L) * env * maxOff;
            pts[i] = origin.add(dir.scale(length * t))
                    .add(right.scale(offR))
                    .add(up.scale(offU));
        }

        for (int i = 0; i < pts.length - 1; i++) {
            trailVShape(pose, v, pts[i], pts[i + 1], right, up, width, r, g, b, al);
        }
    }

    private static void trailVShape(PoseStack.Pose pose, VertexConsumer v,
                                    Vec3 a, Vec3 b, Vec3 right, Vec3 up, float w,
                                    int r, int g, int bl, int al) {
        Vec3 leftWing  = right.scale(-w).add(up.scale(w));
        Vec3 rightWing = right.scale( w).add(up.scale(w));

        trailBv(pose, v, a,                r, g, bl, al);
        trailBv(pose, v, a.add(leftWing),  r, g, bl, al);
        trailBv(pose, v, b.add(leftWing),  r, g, bl, al);
        trailBv(pose, v, b,                r, g, bl, al);

        trailBv(pose, v, b,                r, g, bl, al);
        trailBv(pose, v, b.add(leftWing),  r, g, bl, al);
        trailBv(pose, v, a.add(leftWing),  r, g, bl, al);
        trailBv(pose, v, a,                r, g, bl, al);

        trailBv(pose, v, a,                r, g, bl, al);
        trailBv(pose, v, a.add(rightWing), r, g, bl, al);
        trailBv(pose, v, b.add(rightWing), r, g, bl, al);
        trailBv(pose, v, b,                r, g, bl, al);

        trailBv(pose, v, b,                r, g, bl, al);
        trailBv(pose, v, b.add(rightWing), r, g, bl, al);
        trailBv(pose, v, a.add(rightWing), r, g, bl, al);
        trailBv(pose, v, a,                r, g, bl, al);
    }

    private static void trailBv(PoseStack.Pose pose, VertexConsumer v,
                                Vec3 p, int r, int g, int b, int a) {
        v.addVertex(pose, (float) p.x, (float) p.y, (float) p.z).setColor(r, g, b, a);
    }

    private static float trailHash(long seed) {
        seed ^= (seed >>> 30);
        seed *= 0xBF58476D1CE4E5B9L;
        seed ^= (seed >>> 27);
        seed *= 0x94D049BB133111EBL;
        seed ^= (seed >>> 31);
        return (seed & Long.MAX_VALUE) / (float) Long.MAX_VALUE * 2f - 1f;
    }

    private static float getSurfaceY(Level level, double worldX, double worldZ) {
        int x = Mth.floor(worldX);
        int z = Mth.floor(worldZ);
        int topY = level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        BlockPos pos = new BlockPos(x, topY, z);
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);
        float localX = (float)(worldX - x);
        float localZ = (float)(worldZ - z);
        final float[] maxY = {0f};
        shape.forAllBoxes((minX, minY, minZ, maxX, boxMaxY, maxZ) -> {
            if (localX >= minX && localX <= maxX && localZ >= minZ && localZ <= maxZ)
                maxY[0] = Math.max(maxY[0], (float) boxMaxY);
        });
        return pos.getY() + maxY[0];
    }
}