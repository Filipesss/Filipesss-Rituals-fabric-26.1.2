package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.filipes.rituals.client.DepthstrikeGroundModel;
import net.filipes.rituals.entity.custom.DepthstrikeGroundEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class DepthstrikeGroundEntityRenderer
        extends EntityRenderer<DepthstrikeGroundEntity, DepthstrikeGroundEntityRenderer.GroundStrikeRenderState> {

    private final DepthstrikeGroundModel model;

    public static class GroundStrikeRenderState extends EntityRenderState {
        public float ageInTicks;
        public float visualScale;
        public int   delayTicks;
        public float yRot;
    }

    public DepthstrikeGroundEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new DepthstrikeGroundModel(
                context.bakeLayer(DepthstrikeGroundModel.LAYER));
    }

    @Override
    public GroundStrikeRenderState createRenderState() {
        return new GroundStrikeRenderState();
    }

    @Override
    public void extractRenderState(DepthstrikeGroundEntity entity,
                                   GroundStrikeRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks  = entity.tickCount + partialTick;
        state.visualScale = entity.getVisualScale();
        state.delayTicks  = entity.getDelayTicks();
        state.yRot        = entity.getYRot();
    }

    @Override
    public boolean shouldRender(DepthstrikeGroundEntity entity,
                                Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(GroundStrikeRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {

        if (state.ageInTicks < state.delayTicks) return;

        float animTick = state.ageInTicks - state.delayTicks;
        final float BURIED_DEPTH = 1.5f;

        float yOffset;
        if (animTick < 17f) {
            float t = animTick / 17f;
            float eased = 1f - (float) Math.pow(1f - t, 3);
            yOffset = -BURIED_DEPTH * (1f - eased);
        } else if (animTick < 35f) {
            yOffset = (float) Math.sin((animTick - 17f) * 0.8f) * 0.05f;
        } else {
            float t = Math.min(1f, (animTick - 35f) / 8f);
            yOffset = -BURIED_DEPTH * (t * t);
        }

        poseStack.pushPose();
        poseStack.translate(0.0, yOffset, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.scale(state.visualScale, state.visualScale, state.visualScale);

        // Pass collector safely through the transformation stack
        model.render(poseStack, collector, 15728880, animTick);

        poseStack.popPose();
    }

    @Override
    protected float getShadowRadius(GroundStrikeRenderState state) { return 0.4f * state.visualScale; }

    @Override
    protected float getShadowStrength(GroundStrikeRenderState state) { return 0.5f; }
}