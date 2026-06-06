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
import org.lwjgl.opengl.GL11;

public class VortexProjectileModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("rituals", "vortex_projectile"), "main");

    // Adjust the filename if yours ends in _0.png or similar
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/vortex_projectile_0.png");

    private final ModelPart outline; // 6x6 outer cube
    private final ModelPart main;    // 4x4 inner cube

    public VortexProjectileModel(ModelPart root) {
        this.outline = root.getChild("outline");
        this.main    = root.getChild("main");
    }

    // Register this in your client initializer:
    //   EntityModelLayerRegistry.registerModelLayer(VortexProjectileModel.LAYER, VortexProjectileModel::createBodyLayer);
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh     = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        // 6x6x6 outer cube — UVs start at (0, 0) on the 32x32 texture
        partRoot.addOrReplaceChild("outline",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 4x4x4 inner cube — UVs start at (0, 12) on the 32x32 texture
        partRoot.addOrReplaceChild("main",
                CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                       int packedLight, float ageInTicks) {

        poseStack.pushPose();

        // Standard entity model flip (model Y is downward) + centering.
        // PartPose.offset(0, 24, 0) puts parts 1.5 blocks below the reference after the flip,
        // so we translate +1.5 to bring the cube back to the entity's origin.
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.translate(0.0, 1.5, 0.0);

        float scale = computeScale(ageInTicks);
        poseStack.scale(scale, scale, scale);

        // IMPORTANT: must use a render type that has culling ENABLED.
        // entityTranslucent uses NO_CULL and would break the GL_FRONT trick below.
        // entitySolid keeps GL_CULL_FACE on, which is what we need.
        var renderType = RenderTypes.entitySolid(TEXTURE);

        // ── Pass 1: inner cube ───────────────────────────────────────────────
        // Rendered with the default GL_BACK culling: only outward faces visible.
        VertexConsumer mainVc = bufferSource.getBuffer(renderType);
        this.main.render(poseStack, mainVc, packedLight, OverlayTexture.NO_OVERLAY);

        // Flush immediately so the draw call fires before we touch GL state.
        // If we skip this, the inner cube would draw with GL_FRONT active too.
        bufferSource.endBatch(renderType);

        // ── Pass 2: outer cube (outline effect) ──────────────────────────────
        // GL_FRONT culling: hides outward-facing faces, shows inward-facing ones.
        // Result: the outer cube is invisible where it overlaps the inner cube
        // (depth-tested out), but its back faces peek out at the edges → outline.
        // GlStateManager in this version only tracks cull on/off, not which face.
        // GL11.glCullFace() is safe to call directly — nothing to desync.
        // entitySolid only calls _enableCull() (never glCullFace), so GL_FRONT
        // persists through the draw call inside endBatch.
        GL11.glCullFace(GL11.GL_FRONT);

        VertexConsumer outlineVc = bufferSource.getBuffer(renderType);
        this.outline.render(poseStack, outlineVc, packedLight, OverlayTexture.NO_OVERLAY);
        bufferSource.endBatch(renderType);

        GL11.glCullFace(GL11.GL_BACK); // restore OpenGL default

        poseStack.popPose();
    }

    // ── Scale animation ──────────────────────────────────────────────────────
    // Manually mirrors VortexProjectileAnimation.SCALE_PULSE so we don't need
    // HierarchicalModel or AnimationState on the entity.
    // Duration: 1.0 second = 20 ticks.
    private static float computeScale(float ageInTicks) {
        float t = Math.min(1.0f, ageInTicks / 20.0f); // clamp to [0, 1]

        if (t < 0.25f)   return lerp(1.0f, 0.5f, t              / 0.25f);
        if (t < 0.375f)  return lerp(0.5f, 0.7f, (t - 0.25f)   / 0.125f);
        if (t < 0.5f)    return lerp(0.7f, 0.5f, (t - 0.375f)  / 0.125f);
        if (t < 0.6667f) return lerp(0.5f, 1.5f, (t - 0.5f)    / 0.1667f);
        if (t < 0.8333f) return lerp(1.5f, 1.0f, (t - 0.6667f) / 0.1666f);
        return              lerp(1.0f, 0.2f, (t - 0.8333f)      / 0.1667f);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.min(1.0f, Math.max(0.0f, t));
    }
}