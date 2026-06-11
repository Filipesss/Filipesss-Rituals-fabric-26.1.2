package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.client.VortexProjectileModel;
import net.filipes.rituals.entity.custom.VortexProjectileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class VortexProjectileEntityRenderer
        extends EntityRenderer<VortexProjectileEntity, VortexProjectileEntityRenderer.VortexProjectileRenderState> {

    // No bakeLayer needed — model is fully manual, no ModelPart / texture.
    // Register with: EntityRendererRegistry.register(ModEntities.VORTEX_PROJECTILE, VortexProjectileEntityRenderer::new);
    private final VortexProjectileModel model = new VortexProjectileModel();

    // ── Render state ──────────────────────────────────────────────────────────
    public static class VortexProjectileRenderState extends EntityRenderState {
        public float ageInTicks;
    }

    public VortexProjectileEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // ── State plumbing ────────────────────────────────────────────────────────
    @Override
    public VortexProjectileRenderState createRenderState() {
        return new VortexProjectileRenderState();
    }

    @Override
    public void extractRenderState(VortexProjectileEntity entity,
                                   VortexProjectileRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public boolean shouldRender(VortexProjectileEntity entity,
                                Frustum frustum, double x, double y, double z) {
        return true;
    }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void submit(VortexProjectileRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {

        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();

        poseStack.pushPose();
        // No light param — debugFilledBox and lines use POSITION_COLOR,
        // which has no lightmap slot and always renders at full brightness.
        model.render(poseStack, bufferSource, state.ageInTicks);
        poseStack.popPose();
    }

    // ── Shadow ────────────────────────────────────────────────────────────────
    @Override
    protected float getShadowRadius(VortexProjectileRenderState state) { return 0.0f; }

    @Override
    protected float getShadowStrength(VortexProjectileRenderState state) { return 0.0f; }
}