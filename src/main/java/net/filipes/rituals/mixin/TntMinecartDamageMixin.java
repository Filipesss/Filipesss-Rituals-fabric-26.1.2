package net.filipes.rituals.mixin;

import net.filipes.rituals.config.RitualConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class TntMinecartDamageMixin {

    @Inject(
            method = "getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F",
            at = @At("RETURN"),
            cancellable = true
    )
    private void rituals$capFinalTntMinecartDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (!RitualConfig.TNT_MINECART_DAMAGE_CAP_ENABLED) return;

        if (source.is(DamageTypes.EXPLOSION) && source.getDirectEntity() instanceof MinecartTNT) {
            float postDefenseDamage = cir.getReturnValueF();

            if (postDefenseDamage > RitualConfig.MAX_TNT_MINECART_DAMAGE) {
                cir.setReturnValue(RitualConfig.MAX_TNT_MINECART_DAMAGE);
            }
        }
    }
}