package net.filipes.rituals.mixin.client;

import net.filipes.rituals.client.ReverseControlsHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityTurnMixin {

    @Unique private boolean rituals$reversingTurn = false;

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void rituals$reverseTurn(double yaw, double pitch, CallbackInfo ci) {
        if (rituals$reversingTurn) return;
        if (!((Object) this instanceof LocalPlayer lp)) return;
        if (!ReverseControlsHandler.isActive()) return;

        rituals$reversingTurn = true;
        lp.turn(-yaw, -pitch);
        rituals$reversingTurn = false;
        ci.cancel();
    }
}