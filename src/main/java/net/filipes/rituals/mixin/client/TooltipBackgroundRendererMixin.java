package net.filipes.rituals.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.filipes.rituals.util.TooltipStyleHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TooltipRenderUtil.class)
public class TooltipBackgroundRendererMixin {

    @Redirect(
            method = "extractTooltipBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;" +
                            "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;" +
                            "Lnet/minecraft/resources/Identifier;IIII)V"
            )
    )
    private static void redirectTooltipTextures(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            Identifier texture,
            int x, int y, int width, int height) {

        RitualsTooltipStyle style = TooltipStyleHolder.currentStyle;
        if (style != null) {
            if (texture.getPath().contains("background")) {
                // Background
                graphics.fill(x + 9, y + 9, x + width - 9, y + height - 9,
                        style.getTooltipBackgroundColor());
            } else {
                int bx = x + 9;
                int by = y + 9;
                int bw = width - 18;
                int bh = height - 18;

                int topColor = style.getTooltipBorderColorTop();
                int bottomColor = style.getTooltipBorderColorBottom();

                // 1. Top Edge
                graphics.fill(bx, by, bx + bw, by + 1, topColor);

                // 2. Bottom Edge
                graphics.fill(bx, by + bh - 1, bx + bw, by + bh, bottomColor);

                // 3. Left Edge
                graphics.fillGradient(bx, by + 1, bx + 1, by + bh - 1, topColor, bottomColor);

                // 4. Right Edge
                graphics.fillGradient(bx + bw - 1, by + 1, bx + bw, by + bh - 1, topColor, bottomColor);
            }
        } else {
            graphics.blitSprite(pipeline, texture, x, y, width, height);
        }
    }
}