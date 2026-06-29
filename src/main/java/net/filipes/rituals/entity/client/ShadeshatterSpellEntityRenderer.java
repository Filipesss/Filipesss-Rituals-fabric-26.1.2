package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.client.ShadeshatterSpellModel;
import net.filipes.rituals.entity.custom.ShadeshatterSpellEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class ShadeshatterSpellEntityRenderer
        extends EntityRenderer<ShadeshatterSpellEntity, ShadeshatterSpellEntityRenderer.SpellRenderState> {

    private final ShadeshatterSpellModel model;

    public static class SpellRenderState extends EntityRenderState {
        public float ageInTicks;
    }

    public ShadeshatterSpellEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ShadeshatterSpellModel(
                context.bakeLayer(ShadeshatterSpellModel.LAYER));
    }

    @Override
    public SpellRenderState createRenderState() {
        return new SpellRenderState();
    }

    @Override
    public void extractRenderState(ShadeshatterSpellEntity entity,
                                   SpellRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public boolean shouldRender(ShadeshatterSpellEntity entity,
                                Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(SpellRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        model.render(poseStack, collector, 15728880, state.ageInTicks);

        poseStack.popPose();
    }

    @Override
    protected float getShadowRadius(SpellRenderState state)   { return 0.35f; }

    @Override
    protected float getShadowStrength(SpellRenderState state) { return 0.4f; }
}