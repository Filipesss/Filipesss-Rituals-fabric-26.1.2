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

public class CinderboltShieldModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "cinderbolt_shield"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/cinderbolt_shield.png");

    private static final float SPIN_DEG_PER_TICK = -6.0f;

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;
    private final ModelPart bone4;

    public CinderboltShieldModel(ModelPart root) {
        this.root  = root;
        this.bone  = root.getChild("bone");
        this.bone2 = root.getChild("bone2");
        this.bone3 = root.getChild("bone3");
        this.bone4 = root.getChild("bone4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        part.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition bone2 = part.addOrReplaceChild("bone2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        addShieldGeometry(bone2);

        PartDefinition bone3 = part.addOrReplaceChild("bone3",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F,
                        0.0F, (float)(2.0 * Math.PI / 3.0), 0.0F));
        addShieldGeometry(bone3);

        PartDefinition bone4 = part.addOrReplaceChild("bone4",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F,
                        0.0F, (float)(-2.0 * Math.PI / 3.0), 0.0F));
        addShieldGeometry(bone4);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addShieldGeometry(PartDefinition parent) {
        parent.addOrReplaceChild("tip_left",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-0.5F, -3.0F, -2.0F, 2.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(17.0F, -11.0F, -7.0F,
                        -0.0678F, 0.4755F, -0.1473F));

        parent.addOrReplaceChild("tip_right",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-0.5F, -3.0F, -3.0F, 2.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(17.0F, -11.0F, 7.0F,
                        0.0678F, -0.4755F, -0.1473F));

        parent.addOrReplaceChild("main_plate",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -10.0F, -7.0F, 5.0F, 10.0F, 14.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(20.0F, -8.0F, 0.0F,
                        0.0F, 0.0F, -0.1309F));

        parent.addOrReplaceChild("trim_bottom",
                CubeListBuilder.create()
                        .texOffs(30, 37)
                        .addBox(-4.0F, -2.0F, -3.0F, 4.0F, 7.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(20.0F, -9.0F, 0.0F,
                        0.0F, 0.0F, -0.3491F));

        parent.addOrReplaceChild("trim_top",
                CubeListBuilder.create()
                        .texOffs(30, 24)
                        .addBox(-4.0F, -3.0F, -4.0F, 5.0F, 5.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(20.0F, -10.0F, 0.0F,
                        0.0F, 0.0F, 0.3491F));

        parent.addOrReplaceChild("back_plate",
                CubeListBuilder.create()
                        .texOffs(0, 24)
                        .addBox(-2.0F, -5.0F, -5.0F, 5.0F, 9.0F, 10.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(19.0F, -4.0F, 0.0F,
                        0.0F, 0.0F, 0.1745F));
    }

    public void render(PoseStack ps, SubmitNodeCollector buffers,
                       int packedLight, float ageInTicks, boolean firstPerson, float currentRadius) {

        bone2.yRot = toRad(SPIN_DEG_PER_TICK * ageInTicks);
        bone3.yRot = toRad(120.0f + SPIN_DEG_PER_TICK * ageInTicks);
        bone4.yRot = toRad(-120.0f + SPIN_DEG_PER_TICK * ageInTicks);

        ps.pushPose();
        ps.translate(0.0f, 0.6f, 0.0f);

        float scale = currentRadius / 1.25f; // 0 at center, 1 at full orbit
        ps.scale(scale, scale, scale);

        // --- PASS 1: Lightning (Only plates/trims, tips and central bone are omitted) ---
        if (!firstPerson) {
            int energyColor = (140 << 24) | (255 << 16) | (120 << 8) | 20; // ARGB format

            for (ModelPart activeBone : new ModelPart[]{bone2, bone3, bone4}) {
                ps.pushPose();
                activeBone.translateAndRotate(ps); // Apply the active bone's position/rotation matrix

                // Submit only the plate/trim sub-elements to the render graph
                buffers.submitModelPart(activeBone.getChild("main_plate"), ps, RenderTypes.lightning(), 15728880, OverlayTexture.NO_OVERLAY, null, energyColor, null);
                buffers.submitModelPart(activeBone.getChild("trim_bottom"), ps, RenderTypes.lightning(), 15728880, OverlayTexture.NO_OVERLAY, null, energyColor, null);
                buffers.submitModelPart(activeBone.getChild("trim_top"), ps, RenderTypes.lightning(), 15728880, OverlayTexture.NO_OVERLAY, null, energyColor, null);
                buffers.submitModelPart(activeBone.getChild("back_plate"), ps, RenderTypes.lightning(), 15728880, OverlayTexture.NO_OVERLAY, null, energyColor, null);

                ps.popPose();
            }
        }

        // --- PASS 2: Solid Textures (Bones 2, 3, and 4 are rendered completely including their tips) ---
        if (!firstPerson) {
            int mainColor = (255 << 24) | (255 << 16) | (255 << 8) | 255;

            buffers.submitModelPart(bone2, ps, RenderTypes.entityTranslucent(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, null, mainColor, null);
            buffers.submitModelPart(bone3, ps, RenderTypes.entityTranslucent(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, null, mainColor, null);
            buffers.submitModelPart(bone4, ps, RenderTypes.entityTranslucent(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, null, mainColor, null);
        } else {
            int ghostColor = (100 << 24) | (255 << 16) | (255 << 8) | 255;

            buffers.submitModelPart(bone2, ps, RenderTypes.eyes(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, null, ghostColor, null);
            buffers.submitModelPart(bone3, ps, RenderTypes.eyes(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, null, ghostColor, null);
            buffers.submitModelPart(bone4, ps, RenderTypes.eyes(TEXTURE), 15728880, OverlayTexture.NO_OVERLAY, null, ghostColor, null);
        }

        ps.popPose();
    }

    private static float toRad(float deg) {
        return deg * ((float) Math.PI / 180.0f);
    }
}