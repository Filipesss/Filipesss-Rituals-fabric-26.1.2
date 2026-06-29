package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.filipes.rituals.client.PolarityShieldModel;
import net.filipes.rituals.entity.custom.PolarityShieldEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PolarityShieldEntityRenderer
        extends EntityRenderer<PolarityShieldEntity, PolarityShieldEntityRenderer.ShieldRenderState> {

    private final PolarityShieldModel model;

    public static class ShieldRenderState extends EntityRenderState {
        public float ageInTicks;
        public boolean isFirstPerson;
        public boolean isRed;
        public float yaw, pitch;
        public double correctionDx, correctionDy, correctionDz;
        public int actionState; // Add this
        public int deathTicks;  // Add this
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
        state.actionState = entity.getActionState();
        state.deathTicks = entity.deathTicks;

        Minecraft mc = Minecraft.getInstance();
        state.isFirstPerson = entity.owner != null
                && entity.owner == mc.player
                && mc.options.getCameraType() == net.minecraft.client.CameraType.FIRST_PERSON;

        // ONLY kill positioning corrections if we hit an enemy (State 2)
        if (state.actionState == 2) {
            state.correctionDx = 0;
            state.correctionDy = 0;
            state.correctionDz = 0;
        } else if (entity.owner != null) {
            // Keep tracking perfectly smoothly through active and miss-fading stages
            Vec3 look = new Vec3(entity.owner.getLookAngle().x, 0, entity.owner.getLookAngle().z).normalize();
            Vec3 right = new Vec3(-look.z, 0, look.x);
            Vec3 dashDir = entity.isRed() ? right : right.scale(-1);
            Vec3 offset = dashDir.scale(1.1).add(look.scale(0.4));

            double desiredX = Mth.lerp(partialTick, entity.owner.xo, entity.owner.getX()) + offset.x;
            double desiredY = Mth.lerp(partialTick, entity.owner.yo, entity.owner.getY()) + 0.3;
            double desiredZ = Mth.lerp(partialTick, entity.owner.zo, entity.owner.getZ()) + offset.z;

            state.correctionDx = desiredX - Mth.lerp(partialTick, entity.xo, entity.getX());
            state.correctionDy = desiredY - Mth.lerp(partialTick, entity.yo, entity.getY());
            state.correctionDz = desiredZ - Mth.lerp(partialTick, entity.zo, entity.getZ());
        }
    }
    @Override
    public boolean shouldRender(PolarityShieldEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(ShieldRenderState state, PoseStack ps, SubmitNodeCollector snc, CameraRenderState cam) {
        ps.pushPose();
        ps.translate(state.correctionDx, state.correctionDy, state.correctionDz);
        ps.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        ps.mulPose(Axis.XP.rotationDegrees(state.pitch));
        ps.mulPose(Axis.ZP.rotationDegrees(180.0F));

        float scale = 1.0F;
        float alpha = 1.0F;

        if (state.actionState == 1) {
            float progress = Mth.clamp((state.deathTicks + state.ageInTicks % 1.0F) / 7.0F, 0.0F, 1.0F);
            scale = 1.0F + (progress * 0.6F);
            alpha = 1.0F - progress;
        } else if (state.actionState == 2) {
            float progress = Mth.clamp((state.deathTicks + state.ageInTicks % 1.0F) / 10.0F, 0.0F, 1.0F);
            scale = 1.0F + (progress * 0.6F);
            alpha = 1.0F - progress;
        }

        double dynamicY = -1.85D - ((scale - 1.0F) * 0.7D);
        ps.translate(0.0D, dynamicY, 0.0D);
        ps.scale(scale, scale, scale);

        model.render(ps, snc, 15728880, state.ageInTicks, state.isRed, state.isFirstPerson, alpha);
        ps.popPose();
    }

    @Override protected float getShadowRadius(ShieldRenderState s) { return 0f; }
    @Override protected float getShadowStrength(ShieldRenderState s) { return 0f; }
}