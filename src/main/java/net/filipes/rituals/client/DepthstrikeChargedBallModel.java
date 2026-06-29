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

public class DepthstrikeChargedBallModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "depthstrike_charged_ball"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/depthstrike_charged_ball.png");

    private static final float PERIOD = 45f; // 2.25s × 20 ticks

    private static final float[] KF = { 0f, 5f, 10f, 15f, 20f, 25f, 30f, 35f, 40f, 45f };

    private static final float[][] BONE_KF = {
            {  12.6258f, -26.7426f,  -32.7515f },
            {-243.797f,   -1.5275f,   78.252f  },
            { -81.4491f, -74.2087f,    1.2756f },
            { -70.6f,     42.3599f,  -13.1276f },
            {  41.3205f, -35.1969f,    4.5099f },
            { -19.2082f,  27.8622f,  -59.7288f },
            {  18.1151f, -44.3719f,   84.5223f },
            { -80.8405f, -30.8844f,  223.8676f },
            {   4.7105f, -40.5621f,   84.6448f },
            {  12.6258f, -26.7426f,  -32.7515f },
    };

    private static final float[][] BONE2_KF = {
            {  54.2063f,  12.0174f,   16.1065f },
            { 114.24f,     3.4519f,  -46.4924f },
            { -41.37f,   -20.3887f,   55.256f  },
            { 150.4223f, -24.8693f,  -18.6209f },
            { -48.3825f, -45.3994f,   79.0098f },
            { 107.9348f, -10.0056f, -117.3174f },
            {  98.3688f,  39.8141f,  -46.1086f },
            {  46.8311f,  10.4996f,   67.6271f },
            { -93.4082f,  32.7472f,  -26.6419f },
            {  54.2063f,  12.0174f,   16.1065f },
    };

    private static final float[][] BONE3_KF = {
            { -38.0758f,  41.6411f,   27.2231f },
            {  43.1191f, -22.5383f,   -5.6358f },
            {  86.3355f,   5.4412f,  -94.0463f },
            { -57.8521f, -28.1317f,  -28.0272f },
            {  12.84f,   -36.6985f,  -13.5346f },
            {-157.7706f, -14.7817f,   -6.6478f },
            {   4.8085f,  41.1137f,  -29.0351f },
            {-167.4431f, -12.1622f, -100.0183f },
            {-107.9015f, -50.7752f, -153.7815f },
            { -38.0758f,  41.6411f,   27.2231f },
    };

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;

    public DepthstrikeChargedBallModel(ModelPart root) {
        this.root  = root;
        this.bone  = root.getChild("bone");
        this.bone2 = root.getChild("bone2");
        this.bone3 = root.getChild("bone3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh     = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        partRoot.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -6.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        partRoot.addOrReplaceChild("bone2",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .addBox(0.0F, -8.0F, -7.0F, 0.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition bone3Part = partRoot.addOrReplaceChild("bone3",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        bone3Part.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(28, 20)
                        .addBox(0.0F, -8.0F, -7.0F, 0.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F,
                        0.0F, (float) -Math.PI / 2f, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack poseStack, SubmitNodeCollector buffers,
                       int packedLight, float ageInTicks) {

        float t = ageInTicks % PERIOD;

        float[] b  = sampleKeyframes(t, BONE_KF);
        float[] b2 = sampleKeyframes(t, BONE2_KF);
        float[] b3 = sampleKeyframes(t, BONE3_KF);

        bone.xRot  = toRad(b[0]);   bone.yRot  = toRad(b[1]);   bone.zRot  = toRad(b[2]);
        bone2.xRot = toRad(b2[0]);  bone2.yRot = toRad(b2[1]);  bone2.zRot = toRad(b2[2]);
        bone3.xRot = toRad(b3[0]);  bone3.yRot = toRad(b3[1]);  bone3.zRot = toRad(b3[2]);

        poseStack.pushPose();
        poseStack.translate(0.0, -0.7, 0.0);

        // Pack standard white color with full opacity (ARGB)
        int mainColor = (255 << 24) | (255 << 16) | (255 << 8) | 255;

        // Submit the entire root directly to the modern render graph
        buffers.submitModelPart(
                root, poseStack, RenderTypes.entityTranslucent(TEXTURE),
                packedLight, OverlayTexture.NO_OVERLAY,
                null, mainColor, null
        );

        poseStack.popPose();
    }

    private static float[] sampleKeyframes(float t, float[][] frames) {
        for (int i = 0; i < KF.length - 1; i++) {
            if (t <= KF[i + 1]) {
                float weight = (t - KF[i]) / (KF[i + 1] - KF[i]);
                return new float[]{
                        lerp(frames[i][0], frames[i + 1][0], weight),
                        lerp(frames[i][1], frames[i + 1][1], weight),
                        lerp(frames[i][2], frames[i + 1][2], weight),
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