package net.filipes.rituals.mixin;

import net.filipes.rituals.entity.custom.PharathornMarkTracker;
import net.filipes.rituals.item.custom.PharathornItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class PharathornMarkDamageMixin {

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float rituals$scalePharathornMarkDamage(float amount, ServerLevel level, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!PharathornMarkTracker.isMarked(self.getUUID())) return amount;

        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof Player attacker)) return amount;

        if (attacker.getMainHandItem().getItem() instanceof PharathornItem) {
            return amount * 1.2f;
        }

        return amount;
    }
}