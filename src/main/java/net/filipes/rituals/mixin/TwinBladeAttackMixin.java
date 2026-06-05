package net.filipes.rituals.mixin;

import net.filipes.rituals.network.TwinBladesHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class TwinBladeAttackMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float rituals$scaleTwinBladeDamage(float amount, ServerLevel level, DamageSource source) {
        Entity attacker = source.getEntity();
        if (!(attacker instanceof Player player)) return amount;
        if (!TwinBladesHandler.isTwinPairEquipped(player)) return amount;

        return amount * TwinBladesHandler.getDamageMultiplier(player);
    }
}