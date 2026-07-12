package net.filipes.rituals.mixin.client;

import net.filipes.rituals.client.tooltip.TooltipScrollHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * AbstractContainerScreen overrides ContainerEventHandler's default mouseScrolled entirely
 * (for bundle scrolling etc.) and never calls super - so the ContainerEventHandler interface
 * mixin never fires for inventory/container screens. This targets the real override directly.
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenTooltipScrollMixin {

    @Inject(method = "mouseScrolled(DDDD)Z", at = @At("HEAD"), cancellable = true)
    private void rituals$onMouseScrolled(double x, double y, double scrollX, double scrollY,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (TooltipScrollHandler.isScrollable() && TooltipScrollHandler.handleScroll(scrollY)) {
            cir.setReturnValue(true);
        }
    }
}