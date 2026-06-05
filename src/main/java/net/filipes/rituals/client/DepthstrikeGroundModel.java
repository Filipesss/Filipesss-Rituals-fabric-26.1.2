package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class DepthstrikeGroundModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "depthstrike_ground"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/depthstrike_ground.png");


    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart bone2;

    public DepthstrikeGroundModel(ModelPart root) {
        this.root  = root;
        this.bone  = root.getChild("bone");
        this.bone2 = root.getChild("bone2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh     = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        PartDefinition bone = partRoot.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs( 0, 24).addBox(-5.0F,   -8.0F, -6.0F, 10.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(30,  0).addBox(-4.0F,  -20.0F, -5.0F,  8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 17).addBox(-3.0F,  -18.0F,  0.0F,  0.0F,  4.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 26).addBox( 3.0F,  -18.0F,  0.0F,  0.0F,  4.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 24.0F, -1.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition bone2 = partRoot.addOrReplaceChild("bone2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, -1.0F));

        bone2.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(40, 35).addBox(0.0F, -2.2514F, -4.1674F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, -13.0F, 7.0F, -0.4549F, -0.1586F, -0.3123F));

        bone2.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(32, 35).addBox(0.0F, -2.2514F, -4.1674F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation( 3.0F, -13.0F, 7.0F, -0.4549F,  0.1586F,  0.3123F));

        bone2.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.999F, -18.0F, 0.0F, 10.0F, 19.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.48F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, float ageInTicks) {

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.translate(0.0, -1.5, 0.0);

        float boneXRad  = lerpKeyframes(ageInTicks);
        float bone2XRad = lerpKeyframesBone2(ageInTicks);

        this.bone.xRot  = 0.3927F + boneXRad;
        this.bone2.xRot = bone2XRad;

        VertexConsumer vc = bufferSource.getBuffer(RenderTypes.entityTranslucent(TEXTURE));
        this.root.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    private static float lerpKeyframes(float t) {
        if (t <= 10f)    return toRad(-20f);
        if (t <= 16.67f) return lerp(toRad(-20f), toRad(  0f), (t - 10f)    / 6.67f);
        if (t <= 20f)    return toRad(0f);
        if (t <= 23.33f) return lerp(toRad(  0f), toRad( 2.5f),(t - 20f)    / 3.33f);
        if (t <= 27.5f)  return lerp(toRad( 2.5f),toRad(-22.5f),(t - 23.33f)/ 4.17f);
        return toRad(-22.5f);
    }

    private static float lerpKeyframesBone2(float t) {
        if (t <= 10f)    return toRad( 30f);
        if (t <= 16.67f) return lerp(toRad( 30f), toRad(  0f), (t - 10f)    / 6.67f);
        if (t <= 20f)    return toRad(0f);
        if (t <= 23.33f) return lerp(toRad(  0f), toRad(-2.5f),(t - 20f)    / 3.33f);
        if (t <= 27.5f)  return lerp(toRad(-2.5f),toRad( 25f), (t - 23.33f) / 4.17f);
        return toRad(25f);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.min(1f, Math.max(0f, t));
    }

    private static float toRad(float deg) {
        return deg * ((float) Math.PI / 180f);
    }
}