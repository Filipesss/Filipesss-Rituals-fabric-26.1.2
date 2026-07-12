package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.filipes.rituals.client.DepthstrikeSplinterModel;
import net.filipes.rituals.entity.custom.DepthstrikeSplinterEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;

public class DepthstrikeSplinterEntityRenderer
        extends EntityRenderer<DepthstrikeSplinterEntity, DepthstrikeSplinterEntityRenderer.SplinterRenderState> {

    private static final float YAW_OFFSET_DEGREES = 0.0F;
    private static final float PITCH_CORRECTION_DEGREES = -90.0F;
    private final DepthstrikeSplinterModel model;

    public static class SplinterRenderState extends EntityRenderState {
        public float yRot;
        public float xRot;
    }

    public DepthstrikeSplinterEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new DepthstrikeSplinterModel(
                context.bakeLayer(DepthstrikeSplinterModel.LAYER));
    }

    @Override
    public SplinterRenderState createRenderState() {
        return new SplinterRenderState();
    }

    @Override
    public void extractRenderState(DepthstrikeSplinterEntity entity,
                                   SplinterRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
    }

    @Override
    public boolean shouldRender(DepthstrikeSplinterEntity entity,
                                Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(SplinterRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot + YAW_OFFSET_DEGREES));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(PITCH_CORRECTION_DEGREES));

        model.render(poseStack, collector, 15728880);

        poseStack.popPose();
    }

    @Override
    protected float getShadowRadius(SplinterRenderState state) { return 0.15f; }

    @Override
    protected float getShadowStrength(SplinterRenderState state) { return 0.3f; }
}