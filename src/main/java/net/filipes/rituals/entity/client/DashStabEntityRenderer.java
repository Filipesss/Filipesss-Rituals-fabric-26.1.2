package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
    }

    @Override public boolean affectedByCulling(DashStabEntity e)       { return false; }
    @Override protected float getShadowRadius  (DashStabRenderState s) { return 0f; }
    @Override protected float getShadowStrength(DashStabRenderState s) { return 0f; }

    @Override
    public void submit(DashStabRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

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