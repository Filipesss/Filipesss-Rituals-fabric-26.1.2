package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis; // Added for rotations
import net.filipes.rituals.client.TemporalShieldModel;
import net.filipes.rituals.entity.custom.TemporalShieldEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;

public class TemporalShieldEntityRenderer
        extends EntityRenderer<TemporalShieldEntity, TemporalShieldEntityRenderer.ShieldRenderState> {

    private final TemporalShieldModel model;

    public static class ShieldRenderState extends EntityRenderState {
        public float ageInTicks;
        public float ownerOffsetX, ownerOffsetY, ownerOffsetZ;
        public boolean isFirstPerson;
        public float currentRadius;
        public float yaw;
        public float pitch;
        public float damageFlashProgress;
    }

    public TemporalShieldEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new TemporalShieldModel(
                ctx.bakeLayer(TemporalShieldModel.LAYER));
    }

    @Override
    public ShieldRenderState createRenderState() {
        return new ShieldRenderState();
    }

    @Override
    public void extractRenderState(TemporalShieldEntity entity,
                                   ShieldRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.ageInTicks = entity.tickCount + partialTick;
        state.currentRadius = entity.prevClientRadius + (entity.clientRadius - entity.prevClientRadius) * partialTick;

        state.damageFlashProgress = entity.getDamageFlash() / 15.0f;

        state.yaw = entity.getYRot();
        state.pitch = entity.getXRot();

        Minecraft mc = Minecraft.getInstance();
        state.isFirstPerson = entity.owner != null
                && entity.owner == mc.player
                && mc.options.getCameraType() == net.minecraft.client.CameraType.FIRST_PERSON;

        if (entity.owner != null) {
            state.ownerOffsetX = 0;
            state.ownerOffsetY = 0;
            state.ownerOffsetZ = 0;
        }
    }

    @Override
    public boolean shouldRender(TemporalShieldEntity entity,
                                Frustum frustum, double x, double y, double z) {

        return true;
    }

    @Override
    public void submit(ShieldRenderState state, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {
        ps.pushPose();
        ps.translate(state.ownerOffsetX, state.ownerOffsetY, state.ownerOffsetZ);

        ps.mulPose(Axis.YP.rotationDegrees(270.0F - state.yaw));

        ps.mulPose(Axis.XP.rotationDegrees(-state.pitch));

        model.render(ps, snc, 15728880, state.ageInTicks, state.isFirstPerson, state.currentRadius, state.damageFlashProgress);

        ps.popPose();
    }

    @Override protected float getShadowRadius  (ShieldRenderState s) { return 0f; }
    @Override protected float getShadowStrength(ShieldRenderState s) { return 0f; }
}