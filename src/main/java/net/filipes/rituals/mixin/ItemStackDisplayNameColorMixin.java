package net.filipes.rituals.mixin;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackDisplayNameColorMixin {

    @Redirect(
            method = "getDisplayName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component rituals$useStyledHoverNameInDisplayName(ItemStack stack) {
        return stack.getStyledHoverName();
    }
}