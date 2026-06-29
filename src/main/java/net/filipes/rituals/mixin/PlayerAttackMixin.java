package net.filipes.rituals.mixin;

import net.filipes.rituals.item.custom.TemporalGlassreaverItem;
import net.filipes.rituals.network.TemporalGlassreaverHandler;
import net.filipes.rituals.particle.ModParticles;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {

    private static final Identifier FORCED_CRIT_ID =
            Identifier.fromNamespaceAndPath("rituals", "temporal_glassreaver_forced_crit");

    @Unique private boolean rituals$wasNaturalCrit = false;
    @Unique private boolean rituals$appliedForcedCrit = false;

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttackHead(Entity target, CallbackInfo ci) {
        rituals$wasNaturalCrit = false;
        rituals$appliedForcedCrit = false;

        Player self = (Player)(Object)this;
        if (self.level().isClientSide()) return;
        if (!(self instanceof ServerPlayer sp)) return;
        if (!(sp.getMainHandItem().getItem() instanceof TemporalGlassreaverItem)) return;

        rituals$wasNaturalCrit = self.fallDistance > 0.0F
                && !self.onGround()
                && !self.onClimbable()
                && !self.isInWater()
                && !self.hasEffect(MobEffects.BLINDNESS)
                && !self.isPassenger()
                && target instanceof LivingEntity
                && self.getAttackStrengthScale(0.5F) > 0.9F;

        if (TemporalGlassreaverHandler.isInCritMode(sp.getUUID()) && !rituals$wasNaturalCrit) {
            var attr = sp.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attr != null) {
                attr.addOrUpdateTransientModifier(new AttributeModifier(
                        FORCED_CRIT_ID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                rituals$appliedForcedCrit = true;
            }
        }
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void onAttackTail(Entity target, CallbackInfo ci) {
        Player self = (Player)(Object)this;
        if (self.level().isClientSide()) return;
        if (!(self instanceof ServerPlayer sp)) return;

        var attr = sp.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) attr.removeModifier(FORCED_CRIT_ID);

        if (!(sp.getMainHandItem().getItem() instanceof TemporalGlassreaverItem)) return;
        if (!(target instanceof LivingEntity)) return;

        if (rituals$appliedForcedCrit || (TemporalGlassreaverHandler.isInCritMode(sp.getUUID()) && rituals$wasNaturalCrit)) {
            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ModParticles.GLASSREAVER_CRIT,
                        target.getX(),
                        target.getY(0.5),
                        target.getZ(),
                        16, 0.3, 0.2, 0.3, 0.0
                );

                // --- ENHANCED HIT SOUND EFFECTS ---
                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_STRONG,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);

                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.GLASS_BREAK,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.3f);
            }
        }

        TemporalGlassreaverHandler.recordAttack(sp.getUUID());
        TemporalGlassreaverHandler.onHit(sp, rituals$wasNaturalCrit);
    }

    // --- CLIENT SAFE CRIT MODE CHECK ---
    @Unique
    private boolean rituals$isAbilityActive(Player player) {
        if (!player.level().isClientSide()) {
            return TemporalGlassreaverHandler.isInCritMode(player.getUUID());
        }
        // Client-side fallback: check the synchronized weapon data component
        var stack = player.getMainHandItem();
        if (stack.getItem() instanceof TemporalGlassreaverItem) {
            var currentData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            return currentData != null && currentData.strings().contains("crit");
        }
        return false;
    }

    @Inject(method = "crit", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaCritParticles(Entity target, CallbackInfo ci) {
        if (rituals$isAbilityActive((Player)(Object)this)) {
            ci.cancel();
        }
    }

    @Inject(method = "magicCrit", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaMagicCritParticles(Entity target, CallbackInfo ci) {
        if (rituals$isAbilityActive((Player)(Object)this)) {
            ci.cancel();
        }
    }
}