package net.filipes.rituals.mixin;

import net.filipes.rituals.item.custom.TemporalGlassreaverItem;
import net.filipes.rituals.network.TemporalGlassreaverHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"))
    private void onSwing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        if (hand != InteractionHand.MAIN_HAND) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;
        if (!(self instanceof ServerPlayer sp)) return;
        if (!(sp.getMainHandItem().getItem() instanceof TemporalGlassreaverItem)) return;

        TemporalGlassreaverHandler.recordSwing(sp.getUUID());
    }
}