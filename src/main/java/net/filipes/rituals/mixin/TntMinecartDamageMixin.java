package net.filipes.rituals.mixin;

import net.filipes.rituals.config.RitualConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class TntMinecartDamageMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float rituals$capTntMinecartDamage(float amount, ServerLevel level, DamageSource source) {
        if (!RitualConfig.TNT_MINECART_DAMAGE_CAP_ENABLED) return amount;
        if (source.is(DamageTypes.EXPLOSION) && source.getDirectEntity() instanceof MinecartTNT) {
            return Math.min(amount, RitualConfig.MAX_TNT_MINECART_DAMAGE);
        }
        return amount;
    }
}