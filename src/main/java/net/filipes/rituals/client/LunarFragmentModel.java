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

public class LunarFragmentModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "lunar_fragment"), "main");

    private static final Identifier TEXTURE_1 =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/lunar_fragment_1.png");
    private static final Identifier TEXTURE_2 =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/lunar_fragment_2.png");

    private static final int TEXTURE_SWITCH_INTERVAL = 5;

    private final ModelPart bone;
    private final ModelPart bbMain;

    public LunarFragmentModel(ModelPart root) {
        this.bone   = root.getChild("bone");
        this.bbMain = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition  mesh = new MeshDefinition();
        PartDefinition  part = mesh.getRoot();

        PartDefinition bone = part.addOrReplaceChild("bone",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 5.6F, 0.0F));

        bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(6, 11).addBox(-0.5F,  0.825F, -1.425F, 1, 2, 2, CubeDeformation.NONE)
                        .texOffs(0, 11).addBox(-0.5F, -2.175F, -1.425F, 1, 2, 2, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -0.6F, 0.0F, 0.7854F, 0, 0));

        bone.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(0, 7).addBox(-1.0F, -0.375F, -3.225F, 2, 1, 3, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 1.1F, 0.0F, 0.7854F, 0, 0));

        bone.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(10, 7).addBox(-1.0F, -0.6F, 0.0F, 2, 1, 3, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -1.4F, 0.0F, 0.7854F, 0, 0));

        bone.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, -0.6F, -3.0F, 2, 1, 6, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0, 0));

        part.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(0,  0).addBox(-1.5F, -13.7F,  2.5F, 3, 2, 0, CubeDeformation.NONE)
                        .texOffs(12, 11).addBox( 1.5F, -13.7F, -2.5F, 0, 2, 5, CubeDeformation.NONE)
                        .texOffs(12, 11).addBox(-1.5F, -13.7F, -2.5F, 0, 2, 5, CubeDeformation.NONE)
                        .texOffs(0,  0).addBox(-1.5F, -13.7F, -2.5F, 3, 2, 0, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    public void render(PoseStack ps, SubmitNodeCollector buffers, int packedLight, float ageInTicks) {
        bone.yRot   = ageInTicks * 0.1F;
        bbMain.yRot = ageInTicks * 0.1F;

        int frame = ((int) ageInTicks / TEXTURE_SWITCH_INTERVAL) % 2;
        Identifier texture = (frame == 0) ? TEXTURE_1 : TEXTURE_2;

        int mainColor = (255 << 24) | (255 << 16) | (255 << 8) | 255;

        // Separately submit the specific model parts tracking animation parameters
        buffers.submitModelPart(
                bone, ps, RenderTypes.entityTranslucent(texture),
                packedLight, OverlayTexture.NO_OVERLAY,
                null, mainColor, null
        );

        buffers.submitModelPart(
                bbMain, ps, RenderTypes.entityTranslucent(texture),
                packedLight, OverlayTexture.NO_OVERLAY,
                null, mainColor, null
        );
    }
}