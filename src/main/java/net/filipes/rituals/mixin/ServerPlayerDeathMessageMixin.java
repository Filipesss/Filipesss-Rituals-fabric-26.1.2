package net.filipes.rituals.mixin;

import net.filipes.rituals.Rituals;
import net.filipes.rituals.util.InvisibleNameHider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public class ServerPlayerDeathMessageMixin {

    @Redirect(
            method = "die",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component rituals$wrapDeathMessage(CombatTracker instance) {

        return InvisibleNameHider.wrapDeathMessage(instance::getDeathMessage);
    }
}