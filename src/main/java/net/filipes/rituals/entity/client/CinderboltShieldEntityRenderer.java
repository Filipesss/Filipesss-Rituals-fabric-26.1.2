package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.client.CinderboltShieldModel;
import net.filipes.rituals.entity.custom.CinderboltShieldEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;

public class CinderboltShieldEntityRenderer
        extends EntityRenderer<CinderboltShieldEntity, CinderboltShieldEntityRenderer.ShieldRenderState> {

    private final CinderboltShieldModel model;

    public static class ShieldRenderState extends EntityRenderState {
        public float ageInTicks;
        public float ownerOffsetX, ownerOffsetY, ownerOffsetZ;
        public boolean isFirstPerson;
        public float currentRadius;
    }

    public CinderboltShieldEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new CinderboltShieldModel(
                ctx.bakeLayer(CinderboltShieldModel.LAYER));
    }

    @Override public ShieldRenderState createRenderState() { return new ShieldRenderState(); }

    @Override
    public void extractRenderState(CinderboltShieldEntity entity,
                                   ShieldRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
        state.currentRadius = entity.prevClientRadius + (entity.clientRadius - entity.prevClientRadius) * partialTick;

        Minecraft mc = Minecraft.getInstance();
        state.isFirstPerson = entity.owner != null
                && entity.owner == mc.player
                && mc.options.getCameraType() == net.minecraft.client.CameraType.FIRST_PERSON;

        if (entity.owner != null) {
            LivingEntity owner = entity.owner;

            // Owner's smoothly interpolated world position
            double ownerX = owner.xo + (owner.getX() - owner.xo) * partialTick;
            double ownerY = owner.yo + (owner.getY() - owner.yo) * partialTick;
            double ownerZ = owner.zo + (owner.getZ() - owner.zo) * partialTick;

            // Entity's interpolated world position (what base class used for camera offset)
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
    public boolean shouldRender(CinderboltShieldEntity entity,
                                Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(ShieldRenderState state, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {
        MultiBufferSource buffers = Minecraft.getInstance()
                .renderBuffers().bufferSource();

        ps.pushPose();
        ps.translate(state.ownerOffsetX, state.ownerOffsetY, state.ownerOffsetZ);
        model.render(ps, buffers, 15728880, state.ageInTicks, state.isFirstPerson, state.currentRadius);
        ps.popPose();
    }

    @Override protected float getShadowRadius  (ShieldRenderState s) { return 0f; }
    @Override protected float getShadowStrength(ShieldRenderState s) { return 0f; }

}