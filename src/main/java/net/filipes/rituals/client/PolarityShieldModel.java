package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
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

        // Base anchor bone translated from Blockbench pivot point
        PartDefinition bone = part.addOrReplaceChild("bone",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 13.0F, 2.0F));

        // Upper section angled block (cube_r1)
        bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-7.0F, -11.0F, -2.0F, 14.0F, 13.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -1.0F, 2.2F, 0.3054F, 0.0F, 0.0F));

        // Lower section angled block (cube_r2)
        bone.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(0, 17)
                        .addBox(-6.0F, -11.0F, -2.0F, 12.0F, 13.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, -0.3054F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack ps, MultiBufferSource buffers, int packedLight,
                       float ageInTicks, boolean isRed, boolean firstPerson) {

        Identifier activeTexture = isRed ? RED_TEXTURE : BLUE_TEXTURE;

        ps.pushPose();

        // Minor baseline positioning offset adjust
        ps.translate(0.0f, 0.25f, 0.0f);

        // --- GLOW PASSTHROUGH LAYER ---
        // Generates an energy aura using the lighting render type layer
        if (!firstPerson) {
            int r = isRed ? 255 : 0;
            int g = isRed ? 30  : 160;
            int b = isRed ? 30  : 255;

            VertexConsumer energyVc = new ForcedColorConsumer(
                    buffers.getBuffer(RenderTypes.lightning()),
                    r, g, b, 160
            );
            root.render(ps, energyVc, 15728880, OverlayTexture.NO_OVERLAY);
        }

        // --- BASE TEXTURE RENDER LAYERS ---
        if (!firstPerson) {
            // Standard scannable third-person transparency profile
            root.render(ps, buffers.getBuffer(RenderTypes.entityTranslucent(activeTexture)),
                    packedLight, OverlayTexture.NO_OVERLAY);
        } else {
            // Soft translucent presentation view inside first-person perspective
            VertexConsumer ghostVc = new ForcedColorConsumer(
                    buffers.getBuffer(RenderTypes.eyes(activeTexture)),
                    255, 255, 255, 90
            );
            root.render(ps, ghostVc, 15728880, OverlayTexture.NO_OVERLAY);
        }

        ps.popPose();
    }

    // Custom pipeline interceptor to force specific color channel alpha inputs over models
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
}