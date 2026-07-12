package net.filipes.rituals.mixin;

import net.filipes.rituals.config.RitualConfig;
import net.filipes.rituals.util.InvisibleNameHider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void rituals$hideInvisibleName(CallbackInfoReturnable<Component> cir) {
        if (!RitualConfig.HIDE_INVISIBLE_PLAYER_NAMES) {
            return;
        }

        Player self = (Player) (Object) this;
        if (InvisibleNameHider.isGeneratingDeathMessage() && self.isInvisible()) {
            cir.setReturnValue(InvisibleNameHider.garbledName());
        }
    }
}