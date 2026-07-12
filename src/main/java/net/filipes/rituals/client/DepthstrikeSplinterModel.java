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

public class DepthstrikeSplinterModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "depthstrike_splinter"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/depthstrike_splinter.png");

    private final ModelPart root;
    private final ModelPart bone;

    public DepthstrikeSplinterModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh     = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        PartDefinition bone = partRoot.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(8, 14)
                        .addBox(-1.0F, 0.0F, -1.001F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 0)
                        .addBox(1.0F, 1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 8)
                        .addBox(-1.0F, -2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F,
                        0.0F, 0.0F, -0.7854F));

        bone.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(10, 8)
                        .addBox(-1.0F, 0.0F, -1.001F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.65F, 4.175F, 0.0F,
                        0.0F, 0.0F, 0.3054F));

        bone.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(0, 13)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.025F, 1.775F, 0.0F,
                        0.0F, 0.0F, -0.2182F));

        bone.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                        .texOffs(16, 3)
                        .addBox(-2.0F, 1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F,
                        0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    public void render(PoseStack poseStack, SubmitNodeCollector buffers, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0, -1.0, 0.0);

        int color = (255 << 24) | (255 << 16) | (255 << 8) | 255;

        buffers.submitModelPart(
                root, poseStack, RenderTypes.entityCutout(TEXTURE),
                packedLight, OverlayTexture.NO_OVERLAY,
                null, color, null
        );

        poseStack.popPose();
    }
}