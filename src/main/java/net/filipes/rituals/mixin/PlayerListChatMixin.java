package net.filipes.rituals.mixin;

import net.filipes.rituals.config.RitualConfig;
import net.filipes.rituals.util.InvisibleNameHider;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListChatMixin {

    @Inject(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void rituals$hideInvisibleChatName(
            PlayerChatMessage chatMessage,
            ServerPlayer sender,
            ChatType.Bound boundChatType,
            CallbackInfo ci
    ) {
        if (!RitualConfig.HIDE_INVISIBLE_PLAYER_NAMES || !sender.isInvisible()) {
            return;
        }

        PlayerList self = (PlayerList) (Object) this;
        Component garbled = InvisibleNameHider.garbledName();
        Component content = chatMessage.decoratedContent();

        Component fullLine = Component.translatable(
                "chat.type.text",
                garbled,
                content
        );

        for (ServerPlayer viewer : self.getPlayers()) {
            viewer.sendSystemMessage(fullLine);
        }

        ci.cancel();
    }
}