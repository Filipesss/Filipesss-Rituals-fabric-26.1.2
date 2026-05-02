package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.custom.SpiralStabEntity;
import net.filipes.rituals.entity.custom.SpiralStabEntity.StabData;
import net.filipes.rituals.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

public class SpiralStabEntityRenderer extends EntityRenderer<SpiralStabEntity, SpiralStabEntityRenderer.StabRenderState> {

    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState itemRenderState = new ItemStackRenderState();

    public static class StabRenderState extends EntityRenderState {
        public float[] progresses = new float[SpiralStabEntity.STAB_COUNT];
        public float[] originX    = new float[SpiralStabEntity.STAB_COUNT];
        public float[] originY    = new float[SpiralStabEntity.STAB_COUNT];
        public float[] originZ    = new float[SpiralStabEntity.STAB_COUNT];
        public float[] angles     = new float[SpiralStabEntity.STAB_COUNT];
        public float[] randomScale = new float[SpiralStabEntity.STAB_COUNT];
        public float[] randomYaw   = new float[SpiralStabEntity.STAB_COUNT];
        public float[] randomLift  = new float[SpiralStabEntity.STAB_COUNT];
        public float[] randomTilt = new float[SpiralStabEntity.STAB_COUNT];
        public int     stabCount;
        public float   glowAlpha;
        public int     stage;
    }

    public SpiralStabEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemModelResolver = ctx.getItemModelResolver();
    }

    @Override public StabRenderState createRenderState() { return new StabRenderState(); }

    @Override
    public void extractRenderState(SpiralStabEntity e, StabRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        s.stabCount = e.stabs.size();
        for (int i = 0; i < s.stabCount; i++) {
            StabData d = e.stabs.get(i);
            s.progresses[i] = e.getStabProgress(d, pt);
            s.originX[i]    = d.originX - (float) s.x;
            Level level = Minecraft.getInstance().level;
            float surfaceY = d.originY;

            if (level != null) {
                surfaceY = getSurfaceY(level, d.originX, d.originZ);
            }

            s.originY[i] = surfaceY - (float) s.y;
            s.originZ[i]    = d.originZ - (float) s.z;
            s.angles[i]     = d.angle;

            long seed = 31L * e.getId() + i * 1009L;
            java.util.Random rand = new java.util.Random(seed);

            s.randomScale[i] = 0.7f + rand.nextFloat() * 0.7f; // 0.7x–1.4x

            s.randomYaw[i] =
                    (rand.nextFloat() - 0.5f) * 70f * Mth.DEG_TO_RAD; // -35° to +35°

            s.randomLift[i] =
                    (rand.nextFloat() - 0.5f) * 0.8f;
            s.randomTilt[i] =
                    (rand.nextFloat() - 0.5f) * 70f * Mth.DEG_TO_RAD;
        }

        float sum = 0; int cnt = 0;
        for (int i = 0; i < s.stabCount; i++) {
            if (s.progresses[i] >= 0) { sum += s.progresses[i]; cnt++; }
        }
        s.glowAlpha = cnt > 0 ? (sum / cnt) * 0.55f : 0f;

        int ownerId = e.getOwnerId();
        net.minecraft.world.entity.LivingEntity owner = null;
        if (Minecraft.getInstance().level != null) {
            net.minecraft.world.entity.Entity found = Minecraft.getInstance().level.getEntity(ownerId);
            if (found instanceof net.minecraft.world.entity.LivingEntity living) {
                owner = living;
            }
        }
        s.stage = owner != null
                ? ModDataComponents.getStage(owner.getMainHandItem())
                : 1;
    }

    @Override public boolean affectedByCulling(SpiralStabEntity e) { return false; }
    @Override protected float getShadowRadius  (StabRenderState s) { return 0f; }
    @Override protected float getShadowStrength(StabRenderState s) { return 0f; }

    @Override
    public void submit(StabRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {


        ItemStack stack = ModDataComponents.withStage(new ItemStack(ModItems.PHARATHORN), s.stage);

        for (int i = 0; i < s.stabCount; i++) {
            float progress = s.progresses[i];
            if (progress < 0.001f) continue;

            ps.pushPose();
            ps.translate(s.originX[i], s.originY[i], s.originZ[i]);

            float emergeOffset = 1.6f * (1f - progress);
            float baseLift = 0.9f + s.randomLift[i];
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

    private static void drawGroundRing(PoseStack.Pose pose, VertexConsumer v,
                                       float radius, float alpha) {
        int a = (int)(alpha * 200);
        if (a < 2) return;
        int segments = 48;
        float innerR = radius * 0.75f;
        float yFloor = 0.02f;

        for (int i = 0; i < segments; i++) {
            double a0 = i       * Math.PI * 2.0 / segments;
            double a1 = (i + 1) * Math.PI * 2.0 / segments;
            float x0i = (float)(Math.cos(a0) * innerR),  z0i = (float)(Math.sin(a0) * innerR);
            float x0o = (float)(Math.cos(a0) * radius),  z0o = (float)(Math.sin(a0) * radius);
            float x1i = (float)(Math.cos(a1) * innerR),  z1i = (float)(Math.sin(a1) * innerR);
            float x1o = (float)(Math.cos(a1) * radius),  z1o = (float)(Math.sin(a1) * radius);

            bv(pose, v, x0o, yFloor, z0o, 180, 120, 255, a);
            bv(pose, v, x0i, yFloor, z0i, 180, 120, 255, a);
            bv(pose, v, x1i, yFloor, z1i, 180, 120, 255, a);
            bv(pose, v, x1o, yFloor, z1o, 180, 120, 255, a);
            bv(pose, v, x1o, yFloor, z1o, 180, 120, 255, a);
            bv(pose, v, x1i, yFloor, z1i, 180, 120, 255, a);
            bv(pose, v, x0i, yFloor, z0i, 180, 120, 255, a);
            bv(pose, v, x0o, yFloor, z0o, 180, 120, 255, a);
        }
    }

    private static void bv(PoseStack.Pose pose, VertexConsumer v,
                           float x, float y, float z, int r, int g, int b, int a) {
        v.addVertex(pose, x, y, z).setColor(r, g, b, a);
    }

    private static float getSurfaceY(Level level, double worldX, double worldZ) {
        int x = Mth.floor(worldX);
        int z = Mth.floor(worldZ);
        int topY = level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                x, z
        ) - 1;

        BlockPos pos = new BlockPos(x, topY, z);
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);

        float localX = (float)(worldX - x);
        float localZ = (float)(worldZ - z);

        final float[] maxY = {0f};
        shape.forAllBoxes((minX, minY, minZ, maxX, boxMaxY, maxZ) -> {
            if (localX >= minX && localX <= maxX && localZ >= minZ && localZ <= maxZ) {
                maxY[0] = Math.max(maxY[0], (float)boxMaxY);
            }
        });

        return pos.getY() + maxY[0];
    }
}