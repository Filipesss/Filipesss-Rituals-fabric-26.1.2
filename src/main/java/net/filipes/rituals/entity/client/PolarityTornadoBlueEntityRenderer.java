package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.client.PolarityTornadoBlueModel;
import net.filipes.rituals.entity.custom.PolarityTornadoBlueEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class PolarityTornadoBlueEntityRenderer
        extends EntityRenderer<PolarityTornadoBlueEntity, PolarityTornadoBlueEntityRenderer.TornadoRenderState> {

    public static class TornadoRenderState extends EntityRenderState {
        public float ageInTicks;
    }

    private final PolarityTornadoBlueModel model;

    public PolarityTornadoBlueEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PolarityTornadoBlueModel(
                context.bakeLayer(PolarityTornadoBlueModel.LAYER));
    }

    @Override
    public TornadoRenderState createRenderState() {
        return new TornadoRenderState();
    }

    @Override
    public void extractRenderState(PolarityTornadoBlueEntity entity,
                                   TornadoRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public boolean shouldRender(PolarityTornadoBlueEntity entity,
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

        model.render(poseStack, bufferSource,
                15728880,
                state.ageInTicks);
    }

    @Override
    protected float getShadowRadius(TornadoRenderState state) {
        return 0.4f;
    }

    @Override
    protected float getShadowStrength(TornadoRenderState state) {
        return 0.5f;
    }
}