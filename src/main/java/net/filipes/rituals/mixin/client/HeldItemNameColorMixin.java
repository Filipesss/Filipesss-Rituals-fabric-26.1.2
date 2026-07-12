package net.filipes.rituals.mixin.client;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Hud.class)
public class HeldItemNameColorMixin {

    @Redirect(
            method = "extractSelectedItemName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component rituals$useStyledHoverName(ItemStack stack) {
        return stack.getStyledHoverName();
    }
}