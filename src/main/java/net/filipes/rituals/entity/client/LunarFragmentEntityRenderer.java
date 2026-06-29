package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.filipes.rituals.client.LunarFragmentModel;
import net.filipes.rituals.entity.custom.LunarFragmentEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;

public class LunarFragmentEntityRenderer
        extends EntityRenderer<LunarFragmentEntity, LunarFragmentEntityRenderer.LunarFragmentRenderState> {

    private static final float ORBIT_RADIUS = 1.1f;
    private static final float ORBIT_SPEED  = 0.04f;
    private static final float ORBIT_HEIGHT = 1.1f;
    private static final int   INTRO_TICKS  = 15;

    private final LunarFragmentModel model;

    public static class LunarFragmentRenderState extends EntityRenderState {
        public float   ageInTicks;
        public int     slot;
        public boolean launched;
        public float   ownerOffsetX, ownerOffsetY, ownerOffsetZ;
    }

    public LunarFragmentEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new LunarFragmentModel(ctx.bakeLayer(LunarFragmentModel.LAYER));
    }

    @Override public LunarFragmentRenderState createRenderState() { return new LunarFragmentRenderState(); }

    @Override
    public void extractRenderState(LunarFragmentEntity entity,
                                   LunarFragmentRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
        state.slot       = entity.getSlot();
        state.launched   = entity.isLaunched();


        if (!entity.isLaunched() && entity.owner != null) {
            LivingEntity owner = entity.owner;

            double ownerX = owner.xo + (owner.getX() - owner.xo) * partialTick;
            double ownerY = owner.yo + (owner.getY() - owner.yo) * partialTick;
            double ownerZ = owner.zo + (owner.getZ() - owner.zo) * partialTick;

            double entityX = entity.xo + (entity.getX() - entity.xo) * partialTick;
            double entityY = entity.yo + (entity.getY() - entity.yo) * partialTick;
            double entityZ = entity.zo + (entity.getZ() - entity.zo) * partialTick;

            state.ownerOffsetX = (float)(ownerX - entityX);
            state.ownerOffsetY = (float)(ownerY - entityY);
            state.ownerOffsetZ = (float)(ownerZ - entityZ);
        } else {
            state.ownerOffsetX = 0;
            state.ownerOffsetY = 0;
            state.ownerOffsetZ = 0;
        }
    }

    @Override
    public boolean shouldRender(LunarFragmentEntity entity, Frustum frustum,
                                double x, double y, double z) { return true; }

    @Override
    public void submit(LunarFragmentRenderState state, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {
        if (state.launched) {
            ps.pushPose();
            ps.mulPose(Axis.YP.rotationDegrees(state.ageInTicks * 18f));
            model.render(ps, snc, 15728880, state.ageInTicks);
            ps.popPose();
        } else {
            float t = state.ageInTicks;
            float slotOffset = state.slot * (float)(Math.PI * 2.0 / 4);

            float angle = t * ORBIT_SPEED + slotOffset + (float)Math.sin(t * 0.031f + slotOffset) * 0.35f;
            float r = ORBIT_RADIUS + (float)Math.sin(t * 0.027f + slotOffset * 1.3f) * 0.25f;
            float bob = (float)Math.sin(t * 0.07f + slotOffset * 0.9f) * 0.35f;

            float offsetX = (float)Math.cos(angle) * r;
            float offsetY = ORBIT_HEIGHT + bob;
            float offsetZ = (float)Math.sin(angle) * r;

            if (t < INTRO_TICKS) {
                float introP = t / (float) INTRO_TICKS;
                float eased = 1.0f - (float) Math.pow(1.0f - introP, 3.0f);
                offsetX *= eased;
                offsetY *= eased;
                offsetZ *= eased;
            }

            ps.pushPose();
            ps.translate(state.ownerOffsetX + offsetX, state.ownerOffsetY + offsetY, state.ownerOffsetZ + offsetZ);
            ps.mulPose(Axis.YP.rotationDegrees(t * 3f));
            model.render(ps, snc, 15728880, t);
            ps.popPose();
        }
    }

    @Override protected float getShadowRadius  (LunarFragmentRenderState s) { return 0.2f; }
    @Override protected float getShadowStrength(LunarFragmentRenderState s) { return 0.3f; }
}