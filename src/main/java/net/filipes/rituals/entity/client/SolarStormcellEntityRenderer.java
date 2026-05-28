package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.client.SolarStormcellModel;
import net.filipes.rituals.entity.custom.SolarStormcellEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class SolarStormcellEntityRenderer
        extends EntityRenderer<SolarStormcellEntity, SolarStormcellEntityRenderer.SolarStormcellRenderState> {

    private final SolarStormcellModel model;

    public static class SolarStormcellRenderState extends EntityRenderState {
        public float ageInTicks;
    }

    public SolarStormcellEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new SolarStormcellModel(ctx.bakeLayer(SolarStormcellModel.LAYER));
    }

    @Override public SolarStormcellRenderState createRenderState() { return new SolarStormcellRenderState(); }

    @Override
    public void extractRenderState(SolarStormcellEntity entity,
                                   SolarStormcellRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public boolean shouldRender(SolarStormcellEntity entity, Frustum frustum,
                                double x, double y, double z) { return true; }

    @Override
    public void submit(SolarStormcellRenderState state, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {
        MultiBufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        ps.pushPose();
        model.render(ps, buffers, 15728880, state.ageInTicks);
        ps.popPose();
    }

    @Override protected float getShadowRadius  (SolarStormcellRenderState s) { return 0.2f; }
    @Override protected float getShadowStrength(SolarStormcellRenderState s) { return 0.3f; }
}