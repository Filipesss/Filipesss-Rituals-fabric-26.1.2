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

        // Translated directly from the Blockbench Yarn export to Mojmap conventions
        partRoot.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-4.0F, -11.0F, -4.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0,  0).addBox(-2.0F, -18.0F, -3.0F, 7.0F, 18.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * Renders the model. The renderer is responsible for the yOffset translation
     * and Y-rotation before calling this; this method only handles the internal
     * flip needed to match Blockbench's Y-down convention.
     */
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f)); // flip from Blockbench Y-down to render Y-up
        poseStack.translate(0.0, -1.5, 0.0);               // seat the pivot at ground level

        VertexConsumer vc = bufferSource.getBuffer(RenderTypes.entityTranslucent(TEXTURE));
        bbMain.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}