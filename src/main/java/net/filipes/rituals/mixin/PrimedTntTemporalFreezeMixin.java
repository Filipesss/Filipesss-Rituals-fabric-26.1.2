package net.filipes.rituals.mixin;

import net.filipes.rituals.util.TemporalFreezeRegistry;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntTemporalFreezeMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void rituals$fuseHead(CallbackInfo ci) {
        PrimedTnt self = (PrimedTnt) (Object) this;
        if (TemporalFreezeRegistry.isSlowed(self.getId())) {
            TemporalFreezeRegistry.storeTntFuse(self.getId(), self.getFuse());
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void rituals$fuseTail(CallbackInfo ci) {
        PrimedTnt self = (PrimedTnt) (Object) this;
        int storedFuse = TemporalFreezeRegistry.takeTntFuse(self.getId());
        if (storedFuse == -1) return;

        if (!self.isAlive()) {
            // exploded this tick — clean up counter and let it go
            TemporalFreezeRegistry.clearTntCounter(self.getId());
            return;
        }

        // Restore the fuse 2 out of every 3 ticks → countdown at 1/3 speed
        if (!TemporalFreezeRegistry.shouldTntDecrement(self.getId())) {
            self.setFuse(storedFuse);
        }
    }
}