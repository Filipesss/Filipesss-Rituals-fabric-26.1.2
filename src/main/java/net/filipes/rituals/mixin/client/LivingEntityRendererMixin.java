package net.filipes.rituals.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.client.ShadowguardHudOverlay;
import net.filipes.rituals.item.custom.ShadowguardItem;
import net.filipes.rituals.util.ShadowguardStateAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void rituals$extractState(LivingEntity entity, LivingEntityRenderState state,
                                      float partialTicks, CallbackInfo ci) {
        if (!(state instanceof ShadowguardStateAccess access)) return;
        Minecraft mc = Minecraft.getInstance();
        boolean shadowguard = (entity == mc.player && ShadowguardHudOverlay.isActive()) ||
                ShadowguardItem.isInvisibleFromShadowguard(entity.getUUID());
        access.rituals$setShadowguardInvisible(shadowguard);
        if (shadowguard) {
            state.isInvisible = true;
            state.isInvisibleToPlayer = true;
        }
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void rituals$cancelRender(LivingEntityRenderState state, PoseStack poseStack,
                                      SubmitNodeCollector submitNodeCollector,
                                      CameraRenderState camera, CallbackInfo ci) {
        if (!(state instanceof ShadowguardStateAccess access)) return;
        if (access.rituals$isShadowguardInvisible()) {
            ci.cancel();
        }
    }
}