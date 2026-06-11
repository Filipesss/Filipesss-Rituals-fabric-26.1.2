package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.filipes.rituals.client.PolarityShieldModel;
import net.filipes.rituals.entity.custom.PolarityShieldEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;

public class PolarityShieldEntityRenderer
        extends EntityRenderer<PolarityShieldEntity, PolarityShieldEntityRenderer.ShieldRenderState> {

    private final PolarityShieldModel model;

    public static class ShieldRenderState extends EntityRenderState {
        public float ageInTicks;
        public float ownerOffsetX, ownerOffsetY, ownerOffsetZ;
        public boolean isFirstPerson;
        public boolean isRed;
        public float yaw, pitch;
    }

    public PolarityShieldEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new PolarityShieldModel(ctx.bakeLayer(PolarityShieldModel.LAYER));
    }

    @Override
    public ShieldRenderState createRenderState() { return new ShieldRenderState(); }

    @Override
    public void extractRenderState(PolarityShieldEntity entity, ShieldRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
        state.isRed = entity.isRed();
        state.yaw = entity.getViewYRot(partialTick);
        state.pitch = entity.getViewXRot(partialTick);

        Minecraft mc = Minecraft.getInstance();
        state.isFirstPerson = entity.owner != null
                && entity.owner == mc.player
                && mc.options.getCameraType() == net.minecraft.client.CameraType.FIRST_PERSON;

        if (entity.owner != null) {
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
    public boolean shouldRender(PolarityShieldEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(ShieldRenderState state, PoseStack ps, SubmitNodeCollector snc, CameraRenderState cam) {
        MultiBufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        ps.pushPose();
        // Translate tracking anchors directly back to the active entity location matrix
        ps.translate(state.ownerOffsetX, state.ownerOffsetY, state.ownerOffsetZ);

        // Orient model base rotation tracking according to player look values
        ps.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        ps.mulPose(Axis.XP.rotationDegrees(state.pitch));

        // Note: Blockbench model assets are inverted by default along the Y/Z axis layers
        ps.mulPose(Axis.ZP.rotationDegrees(180.0F));
        ps.translate(0.0D, -1.5D, 0.0D);

        model.render(ps, buffers, 15728880, state.ageInTicks, state.isRed, state.isFirstPerson);
        ps.popPose();
    }

    @Override protected float getShadowRadius(ShieldRenderState s) { return 0f; }
    @Override protected float getShadowStrength(ShieldRenderState s) { return 0f; }
}