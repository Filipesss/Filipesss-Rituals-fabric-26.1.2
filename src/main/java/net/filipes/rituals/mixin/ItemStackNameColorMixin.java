package net.filipes.rituals.mixin;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackNameColorMixin {

    @Inject(method = "getStyledHoverName", at = @At("RETURN"), cancellable = true)
    private void rituals$applyCustomNameColor(CallbackInfoReturnable<Component> cir) {
        ItemStack self = (ItemStack) (Object) this;

        if (self.getItem() instanceof RitualsTooltipStyle style) {
            Component recolored = cir.getReturnValue().copy()
                    .withStyle(s -> s.withColor(TextColor.fromRgb(style.getNameColor())));
            cir.setReturnValue(recolored);
        }
    }
}