package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.ThrownDepthstrikeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ThrownDepthstrikeRenderer extends EntityRenderer<ThrownDepthstrikeEntity, ThrownDepthstrikeRenderState> {

    public ThrownDepthstrikeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ThrownDepthstrikeRenderState createRenderState() {
        return new ThrownDepthstrikeRenderState();
    }

    @Override
    public void extractRenderState(ThrownDepthstrikeEntity entity, ThrownDepthstrikeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.partialTick = partialTick;
        state.stack = entity.getPickupItemStackOrigin();
        state.yRot  = entity.getYRot();
        state.xRot  = entity.getXRot();
        state.yRotO = entity.yRotO;
        state.xRotO = entity.xRotO;



        state.inGround = entity.isThrownInGround();

        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                state.itemRenderState, state.stack,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                entity.level(), entity, entity.getId());
    }

    @Override
    public void submit(ThrownDepthstrikeRenderState state, PoseStack poseStack,
                       SubmitNodeCollector snc, CameraRenderState camera) {

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(state.partialTick, state.yRotO, state.yRot) - 90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(state.partialTick, state.xRotO, state.xRot) + 90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        state.itemRenderState.submit(poseStack, snc, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        super.submit(state, poseStack, snc, camera);
    }

    private static void drawBolt(PoseStack.Pose pose, VertexConsumer v,
                                 Vec3 origin, Vec3 dir, Vec3 right, Vec3 up,
                                 float length, float width,
                                 int r, int g, int b, int al,
                                 float age, long seed,
                                 float revealFraction, float jaggedFactor) {
        if (length < 0.05f || al < 4 || revealFraction <= 0f) return;

        int timeSlot = (int) age;
        int knots    = Math.max(3, (int)(length / 1.2f));
        int total    = knots + 2;

        Vec3[] pts = new Vec3[total];
        pts[0]         = origin;
        pts[total - 1] = origin.add(dir.scale(length));

        for (int i = 1; i <= knots; i++) {
            float t      = i / (float)(knots + 1);
            float env    = (float) Math.sin(t * Math.PI);
            float maxOff = Math.min(length * 0.50f, 1.4f) * jaggedFactor;
            float offR   = hash(seed + i * 997L  + timeSlot * 7919L) * env * maxOff;
            float offU   = hash(seed + i * 1009L + timeSlot * 6271L) * env * maxOff;
            pts[i] = origin
                    .add(dir.scale(length * t))
                    .add(right.scale(offR))
                    .add(up.scale(offU));
        }

        float half = width * 0.5f;
        float wing = (float)(width * Math.sqrt(3.0) / 2.0);
        Vec3  p0   = up.scale(half);
        Vec3  p1   = up.scale(-half).add(right.scale( wing));
        Vec3  p2   = up.scale(-half).add(right.scale(-wing));

        float segCount = revealFraction * (total - 1);
        int   fullSegs = (int) segCount;
        float partialT = segCount - fullSegs;

        for (int i = 0; i < fullSegs && i < total - 1; i++) {
            vSide(pose, v, pts[i], pts[i + 1], p0, p1, r, g, b, al);
            vSide(pose, v, pts[i], pts[i + 1], p0, p2, r, g, b, al);
        }

        if (fullSegs < total - 1 && partialT > 0.001f) {
            Vec3 from = pts[fullSegs];
            Vec3 to   = pts[fullSegs + 1];
            Vec3 tip  = new Vec3(
                    from.x + (to.x - from.x) * partialT,
                    from.y + (to.y - from.y) * partialT,
                    from.z + (to.z - from.z) * partialT
            );
            vSide(pose, v, from, tip, p0, p1, r, g, b, al);
            vSide(pose, v, from, tip, p0, p2, r, g, b, al);
        }
    }

    private static void vSide(PoseStack.Pose pose, VertexConsumer v,
                              Vec3 a, Vec3 b, Vec3 off0, Vec3 off1,
                              int r, int g, int bl, int al) {
        Vec3 a0 = a.add(off0), a1 = a.add(off1);
        Vec3 b0 = b.add(off0), b1 = b.add(off1);
        bv(pose, v, a0, r, g, bl, al); bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al); bv(pose, v, b0, r, g, bl, al);
        bv(pose, v, b0, r, g, bl, al); bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al); bv(pose, v, a0, r, g, bl, al);
    }

    private static void bv(PoseStack.Pose pose, VertexConsumer v,
                           Vec3 p, int r, int g, int b, int a) {
        v.addVertex(pose, (float)p.x, (float)p.y, (float)p.z).setColor(r, g, b, a);
    }

    private static float hash(long seed) {
        seed ^= (seed >>> 30); seed *= 0xBF58476D1CE4E5B9L;
        seed ^= (seed >>> 27); seed *= 0x94D049BB133111EBL;
        seed ^= (seed >>> 31);
        return (seed & Long.MAX_VALUE) / (float) Long.MAX_VALUE * 2f - 1f;
    }
}