package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class CinderboltShieldModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "cinderbolt_shield"), "main");

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/cinderbolt_shield.png");

    private static final Identifier WHITE_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/white.png");


    private static final float SPIN_DEG_PER_TICK = -6.0f;

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;
    private final ModelPart bone4;
    private final ModelPart tipLeft2, tipLeft3, tipLeft4;
    private final ModelPart tipRight2, tipRight3, tipRight4;


    public CinderboltShieldModel(ModelPart root) {
        this.root  = root;
        this.bone  = root.getChild("bone");
        this.bone2 = root.getChild("bone2");
        this.bone3 = root.getChild("bone3");
        this.bone4 = root.getChild("bone4");
        this.tipLeft2  = bone2.getChild("tip_left");
        this.tipLeft3  = bone3.getChild("tip_left");
        this.tipLeft4  = bone4.getChild("tip_left");
        this.tipRight2 = bone2.getChild("tip_right");
        this.tipRight3 = bone3.getChild("tip_right");
        this.tipRight4 = bone4.getChild("tip_right");
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

    public void render(PoseStack ps, MultiBufferSource buffers,
                       int packedLight, float ageInTicks, boolean firstPerson, float currentRadius) {

        bone2.yRot = toRad(SPIN_DEG_PER_TICK * ageInTicks);
        bone3.yRot = toRad(120.0f + SPIN_DEG_PER_TICK * ageInTicks);
        bone4.yRot = toRad(-120.0f + SPIN_DEG_PER_TICK * ageInTicks);

        ps.pushPose();
        ps.translate(0.0f, 0.6f, 0.0f);

        float scale = currentRadius / 1.25f; // 0 at center, 1 at full orbit
        ps.scale(scale, scale, scale);
        bone.visible = false;
        tipLeft2.visible = false; tipLeft3.visible = false; tipLeft4.visible = false;
        tipRight2.visible = false; tipRight3.visible = false; tipRight4.visible = false;

        if (!firstPerson) {
            VertexConsumer fillVc = new ForcedColorConsumer(
                    buffers.getBuffer(RenderTypes.lightning()),
                    255, 120, 20, 140);
            root.render(ps, fillVc, 15728880, OverlayTexture.NO_OVERLAY);
        }

        tipLeft2.visible = true; tipLeft3.visible = true; tipLeft4.visible = true;
        tipRight2.visible = true; tipRight3.visible = true; tipRight4.visible = true;

        if (!firstPerson) {
            root.render(ps, buffers.getBuffer(RenderTypes.entityTranslucent(TEXTURE)),
                    15728880, OverlayTexture.NO_OVERLAY);
        } else {
            // Almost fully transparent, no glow, just a ghost hint
            VertexConsumer ghostVc = new ForcedColorConsumer(
                    buffers.getBuffer(RenderTypes.eyes(TEXTURE)),
                    255, 255, 255, 100);
            root.render(ps, ghostVc, 15728880, OverlayTexture.NO_OVERLAY);
        }

        bone.visible = true;
        ps.popPose();
    }
    private static final class ForcedColorConsumer implements VertexConsumer {
        private final VertexConsumer inner;
        private final int r, g, b, a;

        ForcedColorConsumer(VertexConsumer inner, int r, int g, int b, int a) {
            this.inner = inner;
            this.r = r; this.g = g; this.b = b; this.a = a;
        }

        @Override public VertexConsumer addVertex(float x, float y, float z)  { inner.addVertex(x, y, z); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a)   { inner.setColor(this.r, this.g, this.b, this.a); return this; }
        @Override public VertexConsumer setColor(int argb)                     { inner.setColor(this.a << 24 | this.r << 16 | this.g << 8 | this.b); return this; }
        @Override public VertexConsumer setUv(float u, float v)                { inner.setUv(u, v); return this; }
        @Override public VertexConsumer setUv1(int u, int v)                   { inner.setUv1(u, v); return this; }
        @Override public VertexConsumer setUv2(int u, int v)                   { inner.setUv2(u, v); return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z)   { inner.setNormal(x, y, z); return this; }
        @Override public VertexConsumer setLineWidth(float width)              { inner.setLineWidth(width); return this; }
    }

    private static float toRad(float deg) {
        return deg * ((float) Math.PI / 180.0f);
    }
}