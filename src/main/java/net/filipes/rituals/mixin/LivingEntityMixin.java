package net.filipes.rituals.mixin;

import net.filipes.rituals.effect.ModStatusEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float rituals$modifyIncomingDamage(float amount) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.hasEffect(ModStatusEffects.SUNBLESSED)) {
            amount *= 0.90f;
        }
        if (self.hasEffect(ModStatusEffects.MOONSHINE)) {
            amount *= 0.85f;
        }
        return amount;
    }
    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float rituals$modifyHealAmount(float amount) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.hasEffect(ModStatusEffects.BLIGHTED)) {
            amount *= 0.5f;
        }
        return amount;
    }
}