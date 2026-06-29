package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class PulseBlasterGunModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_gun"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/item/pulse_blaster.png");

    private final ModelPart bb_main;
    private final ModelPart cylinder;

    public PulseBlasterGunModel(ModelPart root) {
        this.cylinder = root.getChild("cylinder");
        this.bb_main  = root.getChild("bb_main");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition cylinder = partDefinition.addOrReplaceChild("cylinder", CubeListBuilder.create()
                        .texOffs(16, 31).addBox(-1.5F, -0.375F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 8).addBox(-2.0F, -0.975F, -0.5F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        cylinder.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-2.0F, -1.0F, -0.5F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        cylinder.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(46, 28).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 7.525F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bb_main = partDefinition.addOrReplaceChild("bb_main", CubeListBuilder.create()
                        .texOffs(0, 31).addBox(-2.0F, -13.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.0F, -12.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 46).addBox(-1.0F, -11.6F, -3.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 13).addBox(-1.0F, -11.6F, 2.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 23).addBox(-1.0F, -12.6F, -3.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 46).addBox(-3.4F, -12.3F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 17).addBox(-3.0F, -8.0F, 2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 19).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(38, 40).addBox(2.0F, -8.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 21).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(10, 41).addBox(2.0F, -2.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 38).addBox(-3.0F, -2.0F, 2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(42, 30).addBox(-3.0F, -2.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(20, 41).addBox(2.5F, -11.6F, 0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 41).addBox(2.5F, -11.6F, -1.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(46, 50).addBox(3.0F, -8.0F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(46, 45).addBox(2.8F, 2.0F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 7).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(42, 35).addBox(-3.0F, 5.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 0).addBox(-3.0F, 5.0F, -3.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 2).addBox(-3.0F, 5.0F, 2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(46, 8).addBox(2.0F, 5.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(10, 38).addBox(1.9F, 5.5F, -0.45F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(12, 51).addBox(-2.9F, 5.5F, -0.45F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(46, 13).addBox(-3.0F, -11.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 40).addBox(-3.0F, -11.0F, 2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 47).addBox(2.8F, 0.0F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                        .texOffs(28, 40).addBox(2.0F, -1.0F, -2.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3F, 2.0F, 0.5F, 0.0F, -1.5708F, 0.0F));

        bb_main.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                        .texOffs(46, 23).addBox(2.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 6).addBox(-3.0F, -1.0F, 2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(10, 46).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 4).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        bb_main.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                        .texOffs(24, 8).addBox(2.0F, -1.0F, -2.0F, 0.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3F, 2.0F, -4.5F, 0.0F, -1.5708F, 0.0F));

        bb_main.addOrReplaceChild("cube_r6", CubeListBuilder.create()
                        .texOffs(18, 14).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.5F, 1.5F, -2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addOrReplaceChild("cube_r7", CubeListBuilder.create()
                        .texOffs(0, 14).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.5F, 1.5F, 2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5F, 1.5F, -2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addOrReplaceChild("cube_r9", CubeListBuilder.create()
                        .texOffs(16, 23).addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5F, 1.5F, 2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addOrReplaceChild("cube_r10", CubeListBuilder.create()
                        .texOffs(8, 51).addBox(-1.0F, -6.0F, -1.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.5F, -3.0F, 0.7F, 0.0F, -0.7854F, 0.0F));

        bb_main.addOrReplaceChild("cube_r11", CubeListBuilder.create()
                        .texOffs(38, 45).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.5F, -3.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

        bb_main.addOrReplaceChild("cube_r12", CubeListBuilder.create()
                        .texOffs(32, 23).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5F, -6.5F, 2.0F, -1.5708F, 0.7854F, 0.0F));

        bb_main.addOrReplaceChild("cube_r13", CubeListBuilder.create()
                        .texOffs(28, 31).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5F, -6.5F, -2.0F, -1.5708F, 0.7854F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public void render(PoseStack poseStack, SubmitNodeCollector buffers,
                       int light, int overlay, float cylinderAngle, boolean glowCylinder) {
        this.cylinder.yRot = cylinderAngle;

        int mainColor = (255 << 24) | (255 << 16) | (255 << 8) | 255;

        buffers.submitModelPart(
                this.bb_main, poseStack, RenderTypes.entityCutout(TEXTURE),
                light, overlay,
                null, mainColor, null
        );

        int cylinderLight = glowCylinder ? 15728880 : light;

        buffers.submitModelPart(
                this.cylinder, poseStack, RenderTypes.entityCutout(TEXTURE),
                cylinderLight, overlay,
                null, mainColor, null
        );
    }
}