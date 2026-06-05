package net.filipes.rituals.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class ShadowguardHudOverlay {

    private static final long DURATION_MS = 3000;
    private static final long FADE_MS = 500;

    private static final Identifier VIGNETTE_TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/misc/basic_vignette.png");

    private static long activeUntil = 0;

    public static void trigger() {
        activeUntil = System.currentTimeMillis() + DURATION_MS;
    }

    public static boolean isActive() {
        return System.currentTimeMillis() < activeUntil;
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("rituals", "shadowguard_hud"),
                new HudElement() {
                    @Override
                    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
                        render(graphics);
                    }
                }
        );
    }

    private static void render(GuiGraphicsExtractor guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        long remaining = activeUntil - System.currentTimeMillis();
        if (remaining <= 0) return;

        float alpha;
        if (remaining < FADE_MS) {
            alpha = (float) remaining / FADE_MS;
        } else if (DURATION_MS - remaining < FADE_MS) {
            alpha = (float) (DURATION_MS - remaining) / FADE_MS;
        } else {
            alpha = 1.0f;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        int a = (int) (alpha * 85) & 0xFF;
        int color = (a << 24) | 0xFA0000;



        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VIGNETTE_TEXTURE,
                0, 0,
                0.0f, 0.0f,
                width, height,
                256, 256,
                256, 256,
                color
        );
        String label = "[INVISIBLE]";
        int textWidth = mc.font.width(label);

        int textAlpha = (int) (alpha * 255) & 0xFF;
        int textColor = (textAlpha << 24) | 0xFA0000;

        guiGraphics.text(
                mc.font,
                label,
                (width - textWidth) / 2,
                height - 52,
                textColor,
                true
        );
    }

}