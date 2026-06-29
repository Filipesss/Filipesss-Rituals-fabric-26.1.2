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

public class SolarStormcellModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "solar_stormcell"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/solar_stormcell.png");

    private final ModelPart bbMain;

    public SolarStormcellModel(ModelPart root) {
        this.bbMain = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition bbMain = part.addOrReplaceChild("bb_main",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        bbMain.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(0, 7).addBox( 0.0F, -1.0F, -3.0F, 2, 1, 2, CubeDeformation.NONE)
                        .texOffs(0, 4).addBox( 0.0F,  0.0F,  1.0F, 2, 1, 2, CubeDeformation.NONE)
                        .texOffs(0, 0).addBox( 0.0F, -1.0F, -1.0F, 2, 2, 2, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, -0.1745F, 0, 0));

        bbMain.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(6, 12).addBox(0.0F, 0.6586F, -1.4382F, 1, 1, 2, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.5F, -0.1F, 0.0F, 2.2253F, 0, 0));

        bbMain.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(0, 10).addBox(0.0F, 0.6586F, -1.3382F, 1, 1, 2, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.5F, -0.1F, 0.0F, 0.6545F, 0, 0));

        bbMain.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                        .texOffs(8, 9).addBox(0.0F, 0.6586F, -1.3382F, 1, 1, 2, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.5F, -0.1F, 0.0F, -2.4871F, 0, 0));

        bbMain.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                        .texOffs(8, 3).addBox(0.0F,  0.0F,  1.0F, 2, 1, 2, CubeDeformation.NONE)
                        .texOffs(8, 0).addBox(0.0F, -1.0F, -3.0F, 2, 1, 2, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, -1.7453F, 0, 0));

        bbMain.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                        .texOffs(8, 6).addBox(0.0F, -1.0F, -1.1F, 1, 1, 2, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.5F, 0.8F, -1.4F, -0.9163F, 0, 0));

        return LayerDefinition.create(mesh, 32, 32);
    }

    public void render(PoseStack ps, SubmitNodeCollector buffers, int packedLight, float ageInTicks) {
        bbMain.yRot = ageInTicks * 0.1F;
        bbMain.zRot = ageInTicks * 0.1F;

        int mainColor = (255 << 24) | (255 << 16) | (255 << 8) | 255;

        buffers.submitModelPart(
                bbMain, ps, RenderTypes.entityTranslucent(TEXTURE),
                packedLight, OverlayTexture.NO_OVERLAY,
                null, mainColor, null
        );
    }
}