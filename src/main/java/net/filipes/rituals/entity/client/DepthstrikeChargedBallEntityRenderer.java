package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.client.DepthstrikeChargedBallModel;
import net.filipes.rituals.entity.custom.DepthstrikeChargedBallEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class DepthstrikeChargedBallEntityRenderer
        extends EntityRenderer<DepthstrikeChargedBallEntity, DepthstrikeChargedBallEntityRenderer.ChargedBallRenderState> {

    private final DepthstrikeChargedBallModel model;

    public static class ChargedBallRenderState extends EntityRenderState {
        public float ageInTicks;
    }

    public DepthstrikeChargedBallEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new DepthstrikeChargedBallModel(
                context.bakeLayer(DepthstrikeChargedBallModel.LAYER));
    }

    @Override
    public ChargedBallRenderState createRenderState() {
        return new ChargedBallRenderState();
    }

    @Override
    public void extractRenderState(DepthstrikeChargedBallEntity entity,
                                   ChargedBallRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public boolean shouldRender(DepthstrikeChargedBallEntity entity,
                                Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(ChargedBallRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        model.render(poseStack, collector, 15728880, state.ageInTicks);

        poseStack.popPose();
    }

    @Override
    protected float getShadowRadius(ChargedBallRenderState state) { return 0.3f; }

    @Override
    protected float getShadowStrength(ChargedBallRenderState state) { return 0.4f; }
}