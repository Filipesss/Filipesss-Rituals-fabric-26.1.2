package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class PharathornGroundSmashModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "pharathorn_ground_smash"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/pharathorn_ground_smash.png");

    private final ModelPart bbMain;

    public PharathornGroundSmashModel(ModelPart root) {
        this.bbMain = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh     = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        partRoot.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-4.0F, -11.0F, -4.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0,  0).addBox(-2.0F, -18.0F, -3.0F, 7.0F, 18.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack poseStack, SubmitNodeCollector buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.translate(0.0, -1.5, 0.0);

        int mainColor = (255 << 24) | (255 << 16) | (255 << 8) | 255;

        buffers.submitModelPart(
                bbMain, poseStack, RenderTypes.entityTranslucent(TEXTURE),
                packedLight, OverlayTexture.NO_OVERLAY,
                null, mainColor, null
        );

        poseStack.popPose();
    }
}