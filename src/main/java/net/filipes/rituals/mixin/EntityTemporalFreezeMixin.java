package net.filipes.rituals.mixin;

import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.util.TemporalFreezeRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityTemporalFreezeMixin {

    private boolean rituals$isHandledElsewhere() {
        Object self = this;
        return self instanceof LivingEntity
                || self instanceof Projectile
                || self instanceof ItemEntity
                || self instanceof PrimedTnt
                || self instanceof SparkEntity;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void rituals$slowStart(CallbackInfo ci) {
        if (rituals$isHandledElsewhere()) return;
        Entity self = (Entity) (Object) this;
        if (TemporalFreezeRegistry.isSlowed(self.getId())) {
            Vec3 v0 = self.getDeltaMovement();
            TemporalFreezeRegistry.storePreVelocity(self.getId(), v0);
            self.setDeltaMovement(v0.scale(TemporalFreezeRegistry.SLOW_FACTOR));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void rituals$slowEnd(CallbackInfo ci) {
        if (rituals$isHandledElsewhere()) return;
        Entity self = (Entity) (Object) this;
        Vec3 v0 = TemporalFreezeRegistry.takePreVelocity(self.getId());
        if (v0 != null) {
            Vec3 v1 = self.getDeltaMovement();
            self.setDeltaMovement(
                    TemporalFreezeRegistry.computeNextTrueVelocity(v0, v1, TemporalFreezeRegistry.SLOW_FACTOR));
        }
    }
}