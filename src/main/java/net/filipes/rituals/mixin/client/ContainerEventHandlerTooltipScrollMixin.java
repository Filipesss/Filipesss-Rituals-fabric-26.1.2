package net.filipes.rituals.mixin.client;

import net.filipes.rituals.client.tooltip.TooltipScrollHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ContainerEventHandler.mouseScrolled(...) is a default interface method, and Screen never
 * overrides it (confirmed against the decompiled Screen/AbstractContainerEventHandler sources)
 * - so this default IS what actually runs when the player scrolls while a Screen is open.
 *
 * Injecting at HEAD and cancelling before the default's own child-routing logic runs means we
 * intercept the scroll regardless of which child widget the mouse happens to be over, as long
 * as an oversized tooltip is currently showing.
 */
@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerTooltipScrollMixin {

    @Inject(method = "mouseScrolled(DDDD)Z", at = @At("HEAD"), cancellable = true)
    private void rituals$onMouseScrolled(double x, double y, double scrollX, double scrollY,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (TooltipScrollHandler.isScrollable() && TooltipScrollHandler.handleScroll(scrollY)) {
            cir.setReturnValue(true);
        }
    }
}