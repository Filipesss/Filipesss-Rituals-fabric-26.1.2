package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.client.PolarityTornadoRedModel;
import net.filipes.rituals.entity.custom.PolarityTornadoRedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class PolarityTornadoRedEntityRenderer
        extends EntityRenderer<PolarityTornadoRedEntity, PolarityTornadoRedEntityRenderer.TornadoRenderState> {

    private final PolarityTornadoRedModel model;

    public static class TornadoRenderState extends EntityRenderState {
        public float ageInTicks;
        public float visualScale;
    }

    public PolarityTornadoRedEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PolarityTornadoRedModel(
                context.bakeLayer(PolarityTornadoRedModel.LAYER));
    }

    @Override
    public TornadoRenderState createRenderState() {
        return new TornadoRenderState();
    }

    @Override
    public void extractRenderState(PolarityTornadoRedEntity entity,
                                   TornadoRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks  = entity.tickCount + partialTick;
        state.visualScale = entity.getVisualScale();
    }

    @Override
    public boolean shouldRender(PolarityTornadoRedEntity entity,
                                Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(TornadoRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector collector,
                       CameraRenderState camera) {

        MultiBufferSource bufferSource = Minecraft.getInstance()
                .renderBuffers()
                .bufferSource();

        poseStack.pushPose();
        poseStack.scale(state.visualScale, state.visualScale, state.visualScale);
        model.render(poseStack, bufferSource, 15728880, state.ageInTicks);
        poseStack.popPose();
    }

    @Override
    protected float getShadowRadius(TornadoRenderState state) {
        return 0.4f * state.visualScale;
    }

    @Override
    protected float getShadowStrength(TornadoRenderState state) {
        return 0.5f;
    }
}