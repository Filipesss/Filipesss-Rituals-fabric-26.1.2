package net.filipes.rituals.mixin.client;

import net.filipes.rituals.client.ReverseControlsHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityTravelMixin {

    @Unique private boolean rituals$reversingTravel = false;

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void rituals$reverseMovement(Vec3 travelVector, CallbackInfo ci) {
        if (rituals$reversingTravel) return;
        if (!((Object) this instanceof LocalPlayer lp)) return;
        if (!ReverseControlsHandler.isActive()) return;

        rituals$reversingTravel = true;
        lp.travel(new Vec3(-travelVector.x, travelVector.y, -travelVector.z));
        rituals$reversingTravel = false;
        ci.cancel();
    }
}