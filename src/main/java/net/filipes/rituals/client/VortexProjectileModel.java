package net.filipes.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class VortexProjectileModel {

    private static final float INNER = 4.0f / 32.0f;
    private static final float OUTER = 6.0f / 32.0f;

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                       float ageInTicks) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.translate(0.0, -1.5, 0.0);

        float scale = computeScale(ageInTicks);
        poseStack.scale(scale, scale, scale);

        // Same render layer for both, one flush at the end.
        // White first, black second = black stays visually in front.
        VertexConsumer vc = bufferSource.getBuffer(RenderTypes.debugQuads());

        addBoxQuads(poseStack, vc,
                -OUTER, -OUTER, -OUTER,
                OUTER,  OUTER,  OUTER,
                1.0f, 1.0f, 1.0f, 1.0f); // white behind

        addBoxQuads(poseStack, vc,
                -INNER, -INNER, -INNER,
                INNER,  INNER,  INNER,
                0.0f, 0.0f, 0.0f, 1.0f); // black front

        bufferSource.endBatch(RenderTypes.debugQuads());

        poseStack.popPose();
    }

    private static void addBoxQuads(PoseStack poseStack, VertexConsumer vc,
                                    float x1, float y1, float z1,
                                    float x2, float y2, float z2,
                                    float r, float g, float b, float a) {
        var pose = poseStack.last();

        // Front (-Z)
        quad(vc, pose, r, g, b, a,
                x1, y1, z1,  x2, y1, z1,  x2, y2, z1,  x1, y2, z1);
        // Back (+Z)
        quad(vc, pose, r, g, b, a,
                x2, y1, z2,  x1, y1, z2,  x1, y2, z2,  x2, y2, z2);
        // Left (-X)
        quad(vc, pose, r, g, b, a,
                x1, y1, z2,  x1, y1, z1,  x1, y2, z1,  x1, y2, z2);
        // Right (+X)
        quad(vc, pose, r, g, b, a,
                x2, y1, z1,  x2, y1, z2,  x2, y2, z2,  x2, y2, z1);
        // Top (+Y)
        quad(vc, pose, r, g, b, a,
                x1, y2, z1,  x2, y2, z1,  x2, y2, z2,  x1, y2, z2);
        // Bottom (-Y)
        quad(vc, pose, r, g, b, a,
                x1, y1, z2,  x2, y1, z2,  x2, y1, z1,  x1, y1, z1);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose,
                             float r, float g, float b, float a,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4) {
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a);
        vc.addVertex(pose, x3, y3, z3).setColor(r, g, b, a);
        vc.addVertex(pose, x4, y4, z4).setColor(r, g, b, a);
    }

    private static float computeScale(float ageInTicks) {
        float t = Math.min(1.0f, ageInTicks / 20.0f);
        if (t < 0.25f)   return lerp(1.0f, 0.5f, t / 0.25f);
        if (t < 0.375f)  return lerp(0.5f, 0.7f, (t - 0.25f) / 0.125f);
        if (t < 0.5f)    return lerp(0.7f, 0.5f, (t - 0.375f) / 0.125f);
        if (t < 0.6667f) return lerp(0.5f, 1.5f, (t - 0.5f) / 0.1667f);
        if (t < 0.8333f) return lerp(1.5f, 1.0f, (t - 0.6667f) / 0.1666f);
        return lerp(1.0f, 0.2f, (t - 0.8333f) / 0.1667f);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.min(1.0f, Math.max(0.0f, t));
    }
}