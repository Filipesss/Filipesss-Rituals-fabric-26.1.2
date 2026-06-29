package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class ShadeshatterSpellModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "shadeshatter_spell"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/shadeshatter_spell.png");

    private static final float DURATION = 120f;

    private static final float[] KF = { 0f, 40f, 80f, 120f };

    private static final float[][] MAIN_KF = {
            {   0.0f,     0.0f,   0.0f },
            {   0.0f,  -360.0f,   5.0f },
            {   0.0f,  -720.0f,  10.0f },
            {   0.0f, -1080.0f,  15.0f },
    };

    private static final float[][] RING_KF = {
            {   0.0f,     0.0f,   0.0f },
            {   0.0f,  -720.0f, -17.5f },
            {   0.0f, -1440.0f, -35.0f },
            {   0.0f, -2160.0f, -52.5f },
    };

    private final ModelPart root;
    private final ModelPart main;
    private final ModelPart ring;

    public ShadeshatterSpellModel(ModelPart root) {
        this.root = root;
        this.main = root.getChild("main");
        this.ring = root.getChild("ring");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh     = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        partRoot.addOrReplaceChild("main",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        PartDefinition ringPart = partRoot.addOrReplaceChild("ring",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        ringPart.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-7.0F, 0.0F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F,
                        -0.0886F, -0.1739F, 0.0154F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack poseStack, SubmitNodeCollector buffers,
                       int packedLight, float ageInTicks) {

        float t = Math.min(ageInTicks, DURATION);

        float[] m = sampleKeyframes(t, MAIN_KF);
        float[] r = sampleKeyframes(t, RING_KF);

        main.xRot = toRad(m[0]); main.yRot = toRad(m[1]); main.zRot = toRad(m[2]);
        ring.xRot = toRad(r[0]); ring.yRot = toRad(r[1]); ring.zRot = toRad(r[2]);

        poseStack.pushPose();
        poseStack.translate(0.0, -1.15, 0.0);

        int color = (255 << 24) | (255 << 16) | (255 << 8) | 255;

        buffers.submitModelPart(
                root, poseStack, RenderTypes.entityTranslucent(TEXTURE),
                packedLight, OverlayTexture.NO_OVERLAY,
                null, color, null
        );

        poseStack.popPose();
    }


    private static float[] sampleKeyframes(float t, float[][] frames) {
        for (int i = 0; i < KF.length - 1; i++) {
            if (t <= KF[i + 1]) {
                float w = (t - KF[i]) / (KF[i + 1] - KF[i]);
                return new float[]{
                        lerp(frames[i][0], frames[i + 1][0], w),
                        lerp(frames[i][1], frames[i + 1][1], w),
                        lerp(frames[i][2], frames[i + 1][2], w),
                };
            }
        }
        float[] last = frames[frames.length - 1];
        return new float[]{ last[0], last[1], last[2] };
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.min(1f, Math.max(0f, t));
    }

    private static float toRad(float deg) {
        return deg * ((float) Math.PI / 180f);
    }
}