package net.filipes.rituals.mixin;

import net.filipes.rituals.config.RitualConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class MaceDamageMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float rituals$capMaceDamage(float amount, ServerLevel level, DamageSource source) {
        if (!RitualConfig.MACE_DAMAGE_CAP_ENABLED) return amount;
        if (source.is(DamageTypes.MACE_SMASH)) {
            return Math.min(amount, RitualConfig.MAX_MACE_DAMAGE);
        }
        return amount;
    }
}