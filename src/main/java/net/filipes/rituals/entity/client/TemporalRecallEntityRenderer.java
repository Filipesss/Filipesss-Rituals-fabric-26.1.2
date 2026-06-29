package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.client.TemporalRecallModel;
import net.filipes.rituals.entity.custom.TemporalRecallEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class TemporalRecallEntityRenderer
        extends EntityRenderer<TemporalRecallEntity, TemporalRecallEntityRenderer.RecallRenderState> {

    private final TemporalRecallModel model;

    private static final Identifier TEXTURE_FRAME_1 =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/temporal_recall_0.png");
    private static final Identifier TEXTURE_FRAME_2 =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/temporal_recall_1.png");
    private static final Identifier TEXTURE_FRAME_3 =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/temporal_recall_2.png");

    public static class RecallRenderState extends EntityRenderState {
        public float ageInTicks;
        public Identifier activeTexture;
    }

    public TemporalRecallEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new TemporalRecallModel(ctx.bakeLayer(TemporalRecallModel.LAYER));
    }

    @Override
    public RecallRenderState createRenderState() {
        return new RecallRenderState();
    }

    @Override
    public void extractRenderState(TemporalRecallEntity entity, RecallRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.ageInTicks = entity.tickCount + partialTick;

        int ticksPerFrame = 6;
        int frameIndex = (entity.tickCount / ticksPerFrame) % 3;

        state.activeTexture = switch (frameIndex) {
            case 0 -> TEXTURE_FRAME_1;
            case 1 -> TEXTURE_FRAME_2;
            default -> TEXTURE_FRAME_3;
        };
    }

    @Override
    public boolean shouldRender(TemporalRecallEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(RecallRenderState state, PoseStack ps, SubmitNodeCollector snc, CameraRenderState cam) {
        ps.pushPose();
        float scale = 0.96F;
        ps.scale(-scale, -scale, scale);
        ps.translate(0.0F, -1.5F, 0.0F);
        model.render(ps, snc, 15728880, state.ageInTicks, state.activeTexture, 1.0f);
        ps.popPose();
    }

    @Override protected float getShadowRadius  (RecallRenderState s) { return 0f; }
    @Override protected float getShadowStrength(RecallRenderState s) { return 0f; }
}