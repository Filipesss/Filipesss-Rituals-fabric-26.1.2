package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.filipes.rituals.client.PharathornGroundSmashModel;
import net.filipes.rituals.entity.custom.PharathornGroundSmashEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class PharathornGroundSmashEntityRenderer
        extends EntityRenderer<PharathornGroundSmashEntity, PharathornGroundSmashEntityRenderer.SmashRenderState> {

    private final PharathornGroundSmashModel model;

    public static class SmashRenderState extends EntityRenderState {
        float ageInTicks;
        float visualScale;
        int   delayTicks;
        float yRot;
    }

    public PharathornGroundSmashEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new PharathornGroundSmashModel(ctx.bakeLayer(PharathornGroundSmashModel.LAYER));
    }

    @Override public SmashRenderState createRenderState() { return new SmashRenderState(); }

    @Override
    public void extractRenderState(PharathornGroundSmashEntity entity, SmashRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks  = entity.tickCount + partialTick;
        state.visualScale = entity.getVisualScale();
        state.delayTicks  = entity.getDelayTicks();
        state.yRot        = entity.getYRot();
    }

    @Override
    public boolean shouldRender(PharathornGroundSmashEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(SmashRenderState state, PoseStack ps,
                       SubmitNodeCollector collector, CameraRenderState cam) {

        float animTick = state.ageInTicks - state.delayTicks;
        if (animTick < 0) return;

        final float BURIED_DEPTH = 1.8f;

        float yOffset;
        if (animTick < PharathornGroundSmashEntity.EMERGE_TICKS) {

            float t     = animTick / (float) PharathornGroundSmashEntity.EMERGE_TICKS;
            float eased = 1f - (float) Math.pow(1f - t, 3);
            yOffset = -BURIED_DEPTH * (1f - eased);
        } else if (animTick < PharathornGroundSmashEntity.EMERGE_TICKS
                + PharathornGroundSmashEntity.HOLD_TICKS) {

            float holdTick   = animTick - PharathornGroundSmashEntity.EMERGE_TICKS;
            float wobblePhase = state.yRot * ((float) Math.PI / 180f);
            yOffset = (float) Math.sin(holdTick * 0.38f + wobblePhase) * 0.045f;
        } else {

            float t = Math.min(1f, (animTick - PharathornGroundSmashEntity.EMERGE_TICKS
                    - PharathornGroundSmashEntity.HOLD_TICKS)
                    / (float) PharathornGroundSmashEntity.RETRACT_TICKS);
            yOffset = -BURIED_DEPTH * (t * t);
        }

        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        ps.pushPose();
        ps.translate(0.0, yOffset, 0.0);
        ps.mulPose(Axis.YP.rotationDegrees(state.yRot)); // random per-spike rotation
        ps.scale(state.visualScale, state.visualScale, state.visualScale);
        model.render(ps, bufferSource, 15728880);
        ps.popPose();
    }

    @Override protected float getShadowRadius  (SmashRenderState s) { return 0.4f * s.visualScale; }
    @Override protected float getShadowStrength(SmashRenderState s) { return 0.5f; }
}