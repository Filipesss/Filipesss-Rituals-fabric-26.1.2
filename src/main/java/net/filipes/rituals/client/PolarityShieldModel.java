package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType; // Kept your environment's specific package path
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class PolarityShieldModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "polarity_shield"), "main");

    private static final Identifier BLUE_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/polarity_shield_blue.png");

    private static final Identifier RED_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/polarity_shield_red.png");

    private final ModelPart root;
    private final ModelPart bone;

    public PolarityShieldModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition bone = part.addOrReplaceChild("bone",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 13.0F, 2.0F));

        bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-7.0F, -11.0F, -2.0F, 14.0F, 13.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -1.0F, 2.2F, 0.3054F, 0.0F, 0.0F));

        bone.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(0, 17)
                        .addBox(-6.0F, -11.0F, -2.0F, 12.0F, 13.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, -0.3054F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack ps, SubmitNodeCollector buffers, int packedLight,
                       float ageInTicks, boolean isRed, boolean firstPerson, float alpha) {

        Identifier activeTexture = isRed ? RED_TEXTURE : BLUE_TEXTURE;

        ps.pushPose();
        ps.translate(0.0f, 0.25f, 0.0f);

        if (!firstPerson) {
            int r = isRed ? 255 : 0;
            int g = isRed ? 30  : 160;
            int b = isRed ? 30  : 255;
            int dynamicAlpha = Math.round(160 * alpha);

            int energyColor = (dynamicAlpha << 24) | (r << 16) | (g << 8) | b;

            buffers.submitModelPart(
                    root, ps, RenderTypes.lightning(),
                    15728880, OverlayTexture.NO_OVERLAY,
                    null, energyColor, null
            );
        }

        if (!firstPerson) {
            int baseAlpha = Math.round(255 * alpha);
            // Pack into full white tint with dynamic opacity
            int mainColor = (baseAlpha << 24) | (255 << 16) | (255 << 8) | 255;

            buffers.submitModelPart(
                    root, ps, RenderTypes.entityTranslucent(activeTexture),
                    packedLight, OverlayTexture.NO_OVERLAY,
                    null, mainColor, null
            );
        } else {
            int firstPersonAlpha = Math.round(90 * alpha);
            int ghostColor = (firstPersonAlpha << 24) | (255 << 16) | (255 << 8) | 255;

            buffers.submitModelPart(
                    root, ps, RenderTypes.eyes(activeTexture),
                    15728880, OverlayTexture.NO_OVERLAY,
                    null, ghostColor, null
            );
        }

        ps.popPose();
    }
}