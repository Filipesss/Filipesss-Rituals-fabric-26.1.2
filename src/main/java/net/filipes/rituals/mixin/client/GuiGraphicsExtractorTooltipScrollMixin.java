package net.filipes.rituals.mixin.client;

import net.filipes.rituals.client.tooltip.ScrollableTooltipPositioner;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * setTooltipForNextFrameInternal is the single private method every tooltip path
 * (item hover, hover-event text, everything) funnels through before being deferred -
 * wrapping the positioner here covers all of them without needing to know every
 * individual caller.
 */
@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorTooltipScrollMixin {

    @ModifyVariable(
            method = "setTooltipForNextFrameInternal",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ClientTooltipPositioner rituals$wrapTooltipPositioner(ClientTooltipPositioner positioner) {
        return new ScrollableTooltipPositioner(positioner);
    }
}