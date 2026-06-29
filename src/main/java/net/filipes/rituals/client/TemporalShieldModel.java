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

public class TemporalShieldModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "temporal_shield"), "main");

    // Single static texture path (Animation logic removed)
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/temporal_shield_0.png");

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart cube_r1;
    private final ModelPart cube_r2;
    private final ModelPart cube_r3;
    private final ModelPart cube_r4;
    private final ModelPart cube_r5;

    public TemporalShieldModel(ModelPart root) {
        this.root    = root;
        this.bone    = root.getChild("bone");
        this.cube_r1 = this.bone.getChild("cube_r1");
        this.cube_r2 = this.bone.getChild("cube_r2");
        this.cube_r3 = this.bone.getChild("cube_r3");
        this.cube_r4 = this.bone.getChild("cube_r4");
        this.cube_r5 = this.bone.getChild("cube_r5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        // Base pivot point of the shield core
        PartDefinition bone = part.addOrReplaceChild("bone",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // Sub-geometry parts attached to the parent 'bone'
        bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(70, 35).addBox(-0.2941F, -0.1704F, -2.0F, 4.0F, 0.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(70, 30).addBox(-0.2941F, -0.1704F, 9.0F, 4.0F, 0.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(2.5F, -4.5F, -6.0F, 0.0F, 0.0F, 0.9599F));

        bone.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(70, 24).addBox(1.0F, -5.0F, 3.0F, 3.0F, 0.0F, 6.0F, CubeDeformation.NONE)
                        .texOffs(56, 69).addBox(1.0F, -5.0F, -9.0F, 3.0F, 0.0F, 6.0F, CubeDeformation.NONE)
                        .texOffs(0, 28).addBox(-1.0F, -10.0F, -8.0F, 3.0F, 10.0F, 16.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(2.5F, -8.5F, 0.0F, 0.0F, 0.0F, -0.2618F));

        bone.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(28, 69).addBox(0.0F, -13.0F, -9.0F, 0.0F, 3.0F, 14.0F, CubeDeformation.NONE)
                        .texOffs(66, 52).addBox(0.0F, -26.0F, -9.0F, 0.0F, 3.0F, 14.0F, CubeDeformation.NONE)
                        .texOffs(42, 0).addBox(-1.0F, -23.0F, -9.0F, 2.0F, 10.0F, 14.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.2F, 9.1F, 15.0F, 0.0F, -0.2618F, 0.0F));

        bone.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                        .texOffs(0, 54).addBox(0.0F, -13.0F, -5.0F, 0.0F, 3.0F, 14.0F, CubeDeformation.NONE)
                        .texOffs(38, 52).addBox(0.0F, -26.0F, -5.0F, 0.0F, 3.0F, 14.0F, CubeDeformation.NONE)
                        .texOffs(38, 28).addBox(-1.0F, -23.0F, -5.0F, 2.0F, 10.0F, 14.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.2F, 9.1F, -15.0F, 0.0F, 0.2618F, 0.0F));

        bone.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, -10.0F, -9.0F, 3.0F, 10.0F, 18.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    public void render(PoseStack ps, SubmitNodeCollector buffers,
                       int packedLight, float ageInTicks, boolean firstPerson, float currentRadius, float damageFlash) {

        ps.pushPose();

        // Base scale modifier (controls overall size)
        float scale = currentRadius / 2.2f;

        // Visual alignment tracking anchor coordinates
        float baselineCorrection = -0.6f;
        float shieldCenterY = 1.0f;

        // Symmetrical Center-Scale execution (Keeps explosion expansion perfectly centered)
        ps.translate(0.0f, baselineCorrection + shieldCenterY, 0.0f);
        ps.scale(scale, scale, scale);
        ps.translate(0.0f, -shieldCenterY, 0.0f);

        // --- SMOOTH RED FLASH TINTING ALGORITHM ---
        // Dynamically scale down Green and Blue components depending on how recently damage was taken
        int r = 255;
        int g = (int) (255 * (1.0f - damageFlash));
        int b = (int) (255 * (1.0f - damageFlash));

        // PACKING COLOR BITS INTO ENCODED ARGB FORMAT
        int dynamicColor = (160 << 24) | (r << 16) | (g << 8) | b;
        int ghostColor   = (90 << 24)  | (r << 16) | (g << 8) | b;

        // --- MAIN TEXTURE PASS ---
        if (!firstPerson) {
            // Third-person: Swapped mainColor for dynamicColor to enable damage flashing
            buffers.submitModelPart(bone, ps, RenderTypes.entityTranslucent(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, null, dynamicColor, null);
        } else {
            // First person HUD perspective: Swapped ghostColor for dynamically shifting variation
            buffers.submitModelPart(bone, ps, RenderTypes.eyes(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, null, ghostColor, null);
        }

        ps.popPose();
    }
}