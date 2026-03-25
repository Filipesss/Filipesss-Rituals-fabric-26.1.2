package net.filipes.rituals.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.filipes.rituals.util.TooltipStyleHolder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TooltipBackgroundRenderer.class)
public class TooltipBackgroundRendererMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"
            )
    )
    private static void redirectTooltipTextures(
            DrawContext ctx,
            RenderPipeline pipeline,
            Identifier texture,
            int x, int y, int width, int height) {

        RitualsTooltipStyle style = TooltipStyleHolder.currentStyle;
        if (style != null) {
            if (texture.getPath().contains("background")) {
                // Trim the 9px transparent texture padding from each side
                ctx.fill(x + 9, y + 9, x + width - 9, y + height - 9, style.getTooltipBackgroundColor());
            } else {
                ctx.drawStrokedRectangle(x + 9, y + 9, width - 18, height - 18, style.getTooltipBorderColor());
            }
        } else {
            // Vanilla items — draw normally
            ctx.drawGuiTexture(pipeline, texture, x, y, width, height);
        }
    }
}