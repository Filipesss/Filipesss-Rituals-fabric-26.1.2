package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class TemporalRecallModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "temporal_recall"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/temporal_recall.png");

    private final ModelPart root;
    private final ModelPart main;
    private final ModelPart visuals;

    public TemporalRecallModel(ModelPart root) {
        this.root = root;
        this.main = root.getChild("main");
        this.visuals = root.getChild("visuals");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        part.addOrReplaceChild("main", CubeListBuilder.create()
                        .texOffs(56, 54).addBox(-1.0F, -12.0F, -3.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE)
                        .texOffs(0, 70).addBox(-5.0F, -12.0F, -3.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE)
                        .texOffs(16, 70).addBox(-9.0F, -24.0F, -3.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE)
                        .texOffs(32, 70).addBox(3.0F, -24.0F, -3.0F, 4.0F, 12.0F, 4.0F, CubeDeformation.NONE)
                        .texOffs(32, 54).addBox(-5.0F, -24.0F, -3.0F, 8.0F, 12.0F, 4.0F, CubeDeformation.NONE)
                        .texOffs(0, 54).addBox(-5.0F, -32.0F, -5.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offset(1.0F, 24.0F, 1.0F));

        part.addOrReplaceChild("visuals", CubeListBuilder.create()
                        .texOffs(0, 36).addBox(-10.0F, -6.0F, -10.0F, 18.0F, 0.0F, 18.0F, CubeDeformation.NONE)
                        .texOffs(0, 18).addBox(-10.0F, -3.0F, -10.0F, 18.0F, 0.0F, 18.0F, CubeDeformation.NONE)
                        .texOffs(0, 0).addBox(-10.0F, -1.0F, -10.0F, 18.0F, 0.0F, 18.0F, CubeDeformation.NONE),
                PartPose.offset(1.0F, 23.0F, 1.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    public void render(PoseStack ps, SubmitNodeCollector buffers, int packedLight, float ageInTicks, Identifier activeTexture, float alpha) {

        this.visuals.yRot = ageInTicks * 0.065F;
        this.main.y = 24.0F + (float) Math.sin(ageInTicks * 0.08F) * 0.6F;

        ps.pushPose();

        int baseAlpha = Math.round(255 * alpha);
        int renderColor = (baseAlpha << 24) | (255 << 16) | (255 << 8) | 255;

        buffers.submitModelPart(
                root, ps, RenderTypes.entityTranslucent(activeTexture),
                packedLight, OverlayTexture.NO_OVERLAY,
                null, renderColor, null
        );

        ps.popPose();
    }
}