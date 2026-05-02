package net.filipes.rituals.mixin.client;

import net.filipes.rituals.item.ModItems;
import net.filipes.rituals.network.DoubleJumpPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Mixin(LocalPlayer.class)
public class ClientPlayerMixin {

    @Unique
    private boolean rituals$wasJumpPressed = false;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void rituals$onAiStep(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        boolean jumpPressed = mc.options.keyJump.isDown();
        boolean risingEdge = jumpPressed && !rituals$wasJumpPressed;

        if (risingEdge
                && !self.onGround()
                && !self.isInWater()
                && !self.isInLava()
                && self.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ROSEGOLD_BOOTS)) {

            ClientPlayNetworking.send(new DoubleJumpPayload());
        }

        rituals$wasJumpPressed = jumpPressed;
    }
}