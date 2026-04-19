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

public class PolarityTornadoBlueModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "polarity_tornado_blue"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/polarity_tornado_blue.png");

    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;

    public PolarityTornadoBlueModel(ModelPart root) {
        this.bone  = root.getChild("bone");
        this.bone2 = root.getChild("bone2");
        this.bone3 = root.getChild("bone3");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(48, 18).addBox(-3.0F, -4.0F, -3.0F, 0.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 28).addBox( 3.0F, -4.0F, -3.0F, 0.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(52, 52).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 52).addBox(-3.0F, -4.0F,  3.0F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 12).addBox(-3.0F,  0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        partDefinition.addOrReplaceChild("bone2", CubeListBuilder.create()
                        .texOffs(0,  12).addBox(-5.0F,  0.0F, -5.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(48,  0).addBox(-5.0F, -6.0F,  5.0F, 10.0F, 6.0F,  0.0F, new CubeDeformation(0.0F))
                        .texOffs(48,  6).addBox(-5.0F, -6.0F, -5.0F, 10.0F, 6.0F,  0.0F, new CubeDeformation(0.0F))
                        .texOffs(0,  40).addBox(-5.0F, -6.0F, -5.0F,  0.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(20, 40).addBox( 5.0F, -6.0F, -5.0F,  0.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 20.0F, 0.0F));

        partDefinition.addOrReplaceChild("bone3", CubeListBuilder.create()
                        .texOffs(0,   0).addBox(-6.0F,  0.0F, -6.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0,  22).addBox(-6.0F, -6.0F, -6.0F,  0.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 22).addBox( 6.0F, -6.0F, -6.0F,  0.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 40).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F,  0.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 46).addBox(-6.0F, -6.0F,  6.0F, 12.0F, 6.0F,  0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource,
                       int light, float ageInTicks) {

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.translate(0.0, -1.5, 0.0);

        this.bone.yRot  = ageInTicks * 0.45f;
        this.bone2.yRot = ageInTicks * 0.50f;
        this.bone3.yRot = ageInTicks * 0.55f;

        VertexConsumer consumer = bufferSource.getBuffer(
                RenderTypes.entityCutout(TEXTURE));

        this.bone.render(poseStack,  consumer, light, OverlayTexture.NO_OVERLAY);
        this.bone2.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
        this.bone3.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}