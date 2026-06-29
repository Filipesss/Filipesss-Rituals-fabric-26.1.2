package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.ShadowguardGrappleEntity;
import net.filipes.rituals.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ShadowguardGrappleEntityRenderer
        extends EntityRenderer<ShadowguardGrappleEntity, ShadowguardGrappleRenderState> {

    private static final Identifier CHAIN_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/depthstrike_grapple_chain.png");

    public ShadowguardGrappleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ShadowguardGrappleRenderState createRenderState() {
        return new ShadowguardGrappleRenderState();
    }

    @Override
    public void extractRenderState(ShadowguardGrappleEntity entity,
                                   ShadowguardGrappleRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
        state.hooked     = entity.isHooked();
        state.entityPos  = entity.position();
        state.yRot = entity.getYRot();

        ItemStack stack = new ItemStack(ModItems.SHADOWGUARD);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                List.of(6f), List.of(), List.of(), List.of()));
        state.stack = stack;

        Entity owner = entity.getOwner();
        state.ownerPos = (owner != null)
                ? owner.position().add(0, owner.getBbHeight() * 0.5, 0)
                : entity.position();

        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                state.itemRenderState, state.stack,
                ItemDisplayContext.FIXED,
                null, null, entity.getId());
    }

    @Override
    public boolean shouldRender(ShadowguardGrappleEntity entity,
                                Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(ShadowguardGrappleRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {

        renderChain(state, poseStack, collector);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.hooked ? 0f : state.ageInTicks * 54f));
        poseStack.scale(1.5f, 1.5f, 1.5f);

        state.itemRenderState.submit(poseStack, collector,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    private void renderChain(ShadowguardGrappleRenderState state,
                             PoseStack poseStack,
                             SubmitNodeCollector collector) {
        Vec3 fromEntity = state.ownerPos.subtract(state.entityPos);
        double length = fromEntity.length();
        if (length < 0.1) return;

        int segments = Math.max(6, (int)(length * 1.2));
        float hw = 0.12f;

        for (int i = 0; i < segments; i++) {
            float t0 = (float) i       / segments;
            float t1 = (float)(i + 1)  / segments;

            float sag0 = (float)(Math.sin(t0 * Math.PI) * length * 0.13);
            float sag1 = (float)(Math.sin(t1 * Math.PI) * length * 0.13);

            float x0 = (float)(fromEntity.x * t0);
            float y0 = (float)(fromEntity.y * t0) - sag0;
            float z0 = (float)(fromEntity.z * t0);

            float x1 = (float)(fromEntity.x * t1);
            float y1 = (float)(fromEntity.y * t1) - sag1;
            float z1 = (float)(fromEntity.z * t1);

            int light = state.lightCoords;

            poseStack.pushPose();
            poseStack.translate((x0 + x1) * 0.5f, (y0 + y1) * 0.5f, (z0 + z1) * 0.5f);

            Vec3 dir  = new Vec3(x1 - x0, y1 - y0, z1 - z0).normalize();
            Vec3 up   = new Vec3(0, 1, 0);
            Vec3 side = dir.cross(up).normalize();
            if (side.lengthSqr() < 0.001) side = new Vec3(1, 0, 0);
            Vec3 perp = side.cross(dir).normalize();

            org.joml.Matrix4f mat = new org.joml.Matrix4f();
            mat.set(
                    (float)side.x, (float)side.y, (float)side.z, 0,
                    (float)perp.x, (float)perp.y, (float)perp.z, 0,
                    (float)dir.x,  (float)dir.y,  (float)dir.z,  0,
                    0, 0, 0, 1
            );
            poseStack.mulPose(mat);

            float segLen = (float)new Vec3(x1-x0, y1-y0, z1-z0).length() * 0.55f;
            float cos45 = 0.7071f, sin45 = 0.7071f;
            float ax = hw * cos45, ay = hw * sin45;

            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(CHAIN_TEXTURE), (pose, vc) -> {

                vc.addVertex(pose, -ax,  ay, -segLen).setColor(255,255,255,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);
                vc.addVertex(pose,  ax, -ay, -segLen).setColor(255,255,255,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);
                vc.addVertex(pose,  ax, -ay,  segLen).setColor(255,255,255,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);
                vc.addVertex(pose, -ax,  ay,  segLen).setColor(255,255,255,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);

                vc.addVertex(pose, -ax,  ay,  segLen).setColor(255,255,255,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,-1,0);
                vc.addVertex(pose,  ax, -ay,  segLen).setColor(255,255,255,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,-1,0);
                vc.addVertex(pose,  ax, -ay, -segLen).setColor(255,255,255,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,-1,0);
                vc.addVertex(pose, -ax,  ay, -segLen).setColor(255,255,255,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,-1,0);


                vc.addVertex(pose,  ax,  ay, -segLen).setColor(255,255,255,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);
                vc.addVertex(pose, -ax, -ay, -segLen).setColor(255,255,255,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);
                vc.addVertex(pose, -ax, -ay,  segLen).setColor(255,255,255,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);
                vc.addVertex(pose,  ax,  ay,  segLen).setColor(255,255,255,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);

                vc.addVertex(pose,  ax,  ay,  segLen).setColor(255,255,255,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,-1,0);
                vc.addVertex(pose, -ax, -ay,  segLen).setColor(255,255,255,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,-1,0);
                vc.addVertex(pose, -ax, -ay, -segLen).setColor(255,255,255,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,-1,0);
                vc.addVertex(pose,  ax,  ay, -segLen).setColor(255,255,255,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,-1,0);
            });

            poseStack.popPose();
        }
    }
}